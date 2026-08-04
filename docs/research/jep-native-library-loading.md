# JEP native library loading on Windows — why "Can't find dependent libraries"

Research notes, primary-source. Written 2026-08-03/04 against jep 4.3.1, JDK 25.0.2, Windows 11.

> **Status: diagnosis confirmed, fix implemented on Windows (updated 2026-08-04).**
> The root-cause analysis below held up. Several open questions have since been answered
> empirically — notably **`pip install jep` does work on this machine** (MSVC is present), and
> the **full chain now runs end-to-end**. One new defect was found that this document had only
> listed as unverified: **JEP's `runScript()` does not define `__file__`**.
> See [Resolution](#resolution--what-was-actually-implemented) at the end for what was built.
> Corrections are marked **[CORRECTED 2026-08-04]** inline.

Source shorthand used below:

- `jep-sources!/jep/Foo.java:NN` — line numbers in the **jep 4.3.1 sources jar**,
  `C:\Users\Victor\.m2\repository\org\ninia\jep\4.3.1\jep-4.3.1-sources.jar` (extracted and read locally).
- Everything under **Observed on this machine** was produced by commands run on this box on 2026-08-03/04.

---

## Summary / TL;DR

`jep.dll` is a normal Windows DLL whose import table statically requires **`python311.dll`** (verified by parsing its PE import directory — see below). When `jepDllLoader` calls `MainInterpreter.setJepLibraryPath(...)`, JEP takes the shortcut branch and does a bare `System.load(<abs path>)` (`jep-sources!/jep/MainInterpreter.java:125-126`), skipping the `LibraryLocator` fallback that is the *only* code in JEP that knows how to make `python311.dll` resolvable. Windows then resolves `jep.dll`'s dependencies "as if they were loaded by using only their module names… even if the first DLL was loaded by specifying a full path" ([Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/dlls/dynamic-link-library-search-order)) — so the `temp\jep\` folder that `jep.dll` was copied into is **not** searched for `python311.dll`, and nothing else on this machine provides it either. Hence `UnsatisfiedLinkError: ...jep.dll: Can't find dependent libraries`.

Compounding this: **there is no CPython 3.11 for Windows installed as the project's Python at all** (the `venv/` in the repo is a *Linux/WSL* venv; the only Windows Python on PATH is 3.14). So even a perfect DLL-path fix has nothing correct to point at until a Windows CPython 3.11 is provisioned.

**Recommended fix:** stop hand-copying binaries. Point JEP at a *real* `pip install jep` inside a real Windows CPython 3.11 environment, and before touching JEP, `System.load()` that environment's `python311.dll` first (this is exactly what JEP's own `LibraryLocator.findPythonLibraryWindows()` does — `jep-sources!/jep/LibraryLocator.java:299-313`). Empirically verified below: preloading `python311.dll` makes the *identical* `jep.dll` load successfully. A second, independent requirement that the current design also fails to meet: the embedded interpreter must be able to `import jep` (the Python package) — `Jep.java:216` and `Jep.java:243` do this unconditionally on every interpreter.

---

## How JEP loads its native library

### Resolution order

`MainInterpreter.getMainInterpreter()` is called from every `Jep`/`SharedInterpreter` constructor and calls `initialize()` once per process (`jep-sources!/jep/MainInterpreter.java:96-113`).

`jep-sources!/jep/MainInterpreter.java:124-135`:

```java
protected void initialize() throws Error {
    if (jepLibraryPath != null) {
        System.load(jepLibraryPath);
    } else {
        try {
            System.loadLibrary("jep");
        } catch (UnsatisfiedLinkError e) {
            if (!LibraryLocator.findJepLibrary(pyConfig)) {
                throw e;
            }
        }
    }
    ...
```

So the order is:

1. **`jepLibraryPath` set** (via `MainInterpreter.setJepLibraryPath(String)`, `jep-sources!/jep/MainInterpreter.java:295-302`) → a single, bare `System.load(path)`. **No fallback, no recovery.** This is the branch this project takes.
2. Otherwise `System.loadLibrary("jep")` — resolved against `java.library.path`.
3. Only if (2) throws `UnsatisfiedLinkError` → `LibraryLocator.findJepLibrary(pyConfig)`.

After the library is loaded, `initialize()` builds a `PyConfig` (defaulting to `PyConfig.python()`, `MainInterpreter.java:137-139`) and calls the native `initializePython(...)` on a dedicated daemon thread named `JepMainInterpreter` (`MainInterpreter.java:145-201`), passing `pyConfig.home` (i.e. `PyConfig.setHome`, `jep-sources!/jep/PyConfig.java:149-152`) straight into `Py_Initialize`-equivalent config.

### What `LibraryLocator` actually does

It does **not** shell out to Python. It re-implements, in pure Java, Python's own site-packages path construction and looks for a directory literally named `jep` containing `System.mapLibraryName("jep")` (i.e. `jep.dll` on Windows) — `jep-sources!/jep/LibraryLocator.java:236-268`.

Search order (`LibraryLocator.java:322-333`):

1. `searchPythonPath()` — every entry of the **`PYTHONPATH`** env var (`LibraryLocator.java:100-114`).
2. `searchSitePackages()` — under **`PYTHONHOME`**: `{lib,lib64,Lib}/site-packages`, `{...}/site-python`, and `{...}/pythonX.Y/site-packages` (`LibraryLocator.java:122-152`).
3. `searchUserSitePackages()` — `%APPDATA%\Python\pythonNNN\site-packages`, `~/.local/lib/pythonX.Y/site-packages`, and macOS framework paths (`LibraryLocator.java:161-227`).

`pythonHome` is taken from `PyConfig.home` if set, else the **`PYTHONHOME`** env var, else the **`VIRTUAL_ENV`** env var (`LibraryLocator.java:63-82`).

The crucial part is the recovery inside `searchPackageDir` (`LibraryLocator.java:236-268`) — JEP explicitly anticipates our exact error:

```java
try {
    System.load(libraryFile.getAbsolutePath());
} catch (UnsatisfiedLinkError e) {
    /*
     * This is almost always caused because libpython or
     * pythonXX.dll isn't found, so try to figure out the
     * exact libpython that is needed and look in
     * PYTHONHOME. Otherwise look in PYTHONHOME for
     * pythonXX.dll
     */
    Matcher m = Pattern.compile("libpython[\\w\\.]*").matcher(e.getMessage());
    if (m.find() && findPythonLibrary(m.group(0))) {
        System.load(libraryFile.getAbsolutePath());
    } else if (findPythonLibraryWindows()) {
        System.load(libraryFile.getAbsolutePath());
    } else {
        throw e;
    }
}
```

and `findPythonLibraryWindows()` (`LibraryLocator.java:299-313`) scans `PYTHONHOME` for a file matching `^python\d\d+\.dll$` and `System.load()`s it, then retries `jep.dll`.

**That is the sanctioned technique for fixing this bug, written by the JEP authors.** Calling `setJepLibraryPath` bypasses it entirely.

### `import jep` is also mandatory

Independently of the DLL: every interpreter runs, unconditionally,

- `eval("import jep")` — `jep-sources!/jep/Jep.java:216`
- `exec("from jep import java_import_hook")` / `java_import_hook.setupImporter(...)` — `jep-sources!/jep/Jep.java:243-244` (via `configureInterpreter` → `setupJavaImportHook`, `Jep.java:224`, `Jep.java:236-248`)

So the **`jep` Python package** (`__init__.py`, `java_import_hook.py`, `shared_modules_hook.py`, …) must be on the embedded interpreter's `sys.path`. Copying only the binaries can never satisfy this.

---

## Windows DLL dependency resolution

All quotes from [Microsoft Learn — Dynamic-link library search order](https://learn.microsoft.com/en-us/windows/win32/dlls/dynamic-link-library-search-order) (ms.date 2023-02-08, updated 2025-04-15).

The single most important sentence for this bug:

> "If a DLL has dependencies, then the system searches for the dependent DLLs **as if they were loaded by using only their module names. That's true even if the first DLL was loaded by specifying a full path.**"

So `System.load("C:\...\temp\jep\jep.dll")` gives Windows an absolute path **for `jep.dll` only**. `python311.dll` is then looked up by bare name, and `C:\...\temp\jep\` is *not* in that lookup.

### Standard search order, unpackaged app, safe DLL search mode (the default)

1. DLL redirection
2. API sets
3. SxS manifest redirection
4. **Loaded-module list** — "The system can check to see whether a DLL with the same module name is already loaded into memory (**no matter which folder it was loaded from**)."
5. Known DLLs (`HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\KnownDLLs`)
6. Windows 11 21H2+: the package dependency graph of the process
7. The folder from which **the application** loaded (i.e. the folder of `java.exe`, not of `jep.dll`)
8. The system folder (`System32`)
9. The 16-bit system folder
10. The Windows folder
11. **The current folder**
12. **The directories listed in `PATH`**

Two consequences that fully explain the observed behaviour:

- **Step 4 is why preloading works.** `System.load("...\python311.dll")` puts a module named `python311.dll` in the loaded-module list; the later load of `jep.dll` matches it at step 4, before any directory is searched at all. This is precisely `LibraryLocator.findPythonLibraryWindows()`'s mechanism.
- **Step 12 is why `PATH` works**, and step 11 is why merely `cd`-ing into the folder works (both verified empirically below). Note `PATH` is consulted *last*, and it is the **process environment** `PATH`, which a running JVM cannot change for itself.

### `LOAD_LIBRARY_SEARCH_*` and `AddDllDirectory`

Same page, "Search order using LOAD_LIBRARY_SEARCH flags". With `SetDefaultDllDirectories` / `LoadLibraryEx`, the searched directories are, in order:

1. `LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR` — "The folder that contains the DLL is searched. **This folder is searched only for dependencies of the DLL to be loaded.**"
2. `LOAD_LIBRARY_SEARCH_APPLICATION_DIR`
3. `LOAD_LIBRARY_SEARCH_USER_DIRS` — "Paths explicitly added with the `AddDllDirectory` function or the `SetDllDirectory` function are searched."
4. `LOAD_LIBRARY_SEARCH_SYSTEM32`

`LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR` is exactly the "look next to jep.dll for its dependencies" behaviour the current design assumes it gets for free — but it is **opt-in**, and `java.lang.System.load` does not opt in (verified empirically: test D below fails). `LOAD_WITH_ALTERED_SEARCH_PATH` similarly changes step 7 to "the folder of the executable module that `LoadLibraryEx` is loading", but the JDK does not use it either.

There is **no** pure-Java API for `AddDllDirectory` / `SetDefaultDllDirectories`. Reaching them requires JNI, or the FFM API (`java.lang.foreign`, final since JDK 22 — [JEP 454](https://openjdk.org/jeps/454)), which on JDK 25 requires `--enable-native-access` to avoid warnings/eventual errors.

---

## What JEP officially requires

Sources: the [ninia/jep wiki](https://github.com/ninia/jep/wiki) — [Getting Started](https://github.com/ninia/jep/wiki/Getting-Started), [FAQ](https://github.com/ninia/jep/wiki/FAQ), [Windows](https://github.com/ninia/jep/wiki/Windows).

### Install model

Getting Started lists three install methods, the first being **`pip install jep`**, and then states the three conditions an embedding Java app must meet:

1. > "The jep.jar is accessible to the Java classloaders (typically through the Java classpath)."
2. > "The shared library (jep.so or jep.dll) is accessible by the Java process (typically through `-Djava.library.path` or the environment variable `LD_LIBRARY_PATH`)" — on Windows, `PATH`.
3. > "The jep python files (console.py, java_import_hook.py, version.py, etc) are accessible by Python (typically by placing them in the site-packages/jep directory)."

Condition 3 is the `import jep` requirement seen in `Jep.java:216`. The FAQ is explicit that Maven gives you only half of it:

> "The jars are available through Maven, the native library is not."

i.e. `black.ninia:jep:4.3.1` from Maven Central is **jar only, by design**; the matching native library is expected to come from a `pip install jep` into the very interpreter you embed. There is no official Windows wheel that ships a prebuilt `jep.dll` on PyPI — `pip install jep` on Windows *builds* it (see Compilers, below).

### FAQ: fixing `UnsatisfiedLinkError`

The FAQ's remedies, in its own order of preference:

1. Set **`PYTHONHOME`** — "Jep will try harder to find the native library for you if you set the PYTHONHOME environment variable" (3.8+). This is what feeds `LibraryLocator`.
2. Place the library where Python keeps shared libraries (`python/DLLs` on Windows).
3. Platform env var: `LD_LIBRARY_PATH` (Linux), `DYLD_LIBRARY_PATH` (macOS), **`PATH` (Windows)**.
4. `-Djava.library.path`.
5. `MainInterpreter.setJepLibraryPath(String)` (3.9+).

Note that option 5 — the one this project uses — is listed **last**, and none of options 1–4 are also being applied here. Option 5 alone only tells JEP where `jep.dll` is; it says nothing about `python311.dll`.

### `jep.dll` vs `jep.cp311-win_amd64.pyd` vs `.exp` / `.lib`

From the [Windows wiki page](https://github.com/ninia/jep/wiki/Windows), "Other Insights":

> "The `setup.py build` command should produce a jep.pyd file and a jep.dll file. 'pyd files are very similar to DLLs'. (In Jep they are **identical** but the file extension is very important). If Python were to load the library, we'd probably want a pyd file, but since the library will be loaded from Java, we need a DLL. … The `setup.py install` command should **only install the jep.dll file**. If by chance you somehow end up with both jep.pyd and jep.dll installed, **you must remove the jep.pyd file as it will get loaded first and will not work correctly.**"

and:

> "Running `python setup.py install` will place the files in their appropriate locations: Python's Lib/site-packages/jep directory (jep *.py files, jep jar file, jep.dll); Python's Scripts directory (jep.bat)"

So:

- **`jep.dll`** — the one Java loads. The only one that should be installed.
- **`jep.<abi>.pyd`** — a byte-identical copy with the extension CPython's importer recognises. Shipping it alongside `jep.dll` in the same directory is explicitly called out as harmful.
- **`.lib` / `.exp`** — MSVC import library and exports file, produced by the linker when building a DLL that exports symbols. Build-time artifacts; the loader never reads them ([MS Learn: LIB files as linker input](https://learn.microsoft.com/en-us/cpp/build/reference/dot-lib-files-as-linker-input), [Linker options /IMPLIB](https://learn.microsoft.com/en-us/cpp/build/reference/implib-name-import-library)). `setup.py install` does not install them. Shipping them is dead weight, not a fix.

Also from the same page: building on Windows needs **MSVC 2015/2017/2019** (Community is fine) — relevant if we ever have to produce a matching `jep.dll` for a different Python minor version.

---

## Observed on this machine

All labelled findings below are empirical, from this box, 2026-08-03/04. Nothing here is inferred.

### 1. `jep.dll` really does import `python311.dll` (PE import directory parse)

Parsed the PE headers directly (PowerShell, reading the import descriptor table):

```
src\main\resources\native\windows\jep.dll
  arch=x64 (machine 0x8664), PE32+ (magic 0x20b)
  imports: jvm.dll, python311.dll, KERNEL32.dll, VCRUNTIME140.dll,
           api-ms-win-crt-heap-l1-1-0.dll, api-ms-win-crt-runtime-l1-1-0.dll,
           api-ms-win-crt-stdio-l1-1-0.dll, api-ms-win-crt-convert-l1-1-0.dll,
           api-ms-win-crt-string-l1-1-0.dll
```

- `jvm.dll` — already in the process (loaded by the `java` launcher) → resolves via the loaded-module list, step 4.
- `KERNEL32` + `api-ms-win-crt-*` — system/API sets, always resolvable.
- `VCRUNTIME140.dll` — present in `C:\Windows\System32\vcruntime140.dll`.
- **`python311.dll` — the only unresolvable one.**

### 2. The three Windows files are the same file three times

```
80ab95f2...5def  jep.cp311-win_amd64.dll
80ab95f2...5def  jep.dll
80ab95f2...5def  jep.cp311-win_amd64.pyd
```

Byte-identical (150,528 bytes each), consistent with the wiki's "In Jep they are identical but the file extension is very important". Copying all of them into one directory is exactly the situation the wiki warns against. The `.exp` (4,552 B) and `.lib` (8,396 B) are MSVC link artifacts and are inert at runtime.

Provenance note: these Windows binaries did **not** come from `venv/`, and their origin is not recorded in the repo — see Open Questions.

> **[CORRECTED 2026-08-04] Provenance resolved.** Running `pip install jep==4.3.1` into a clean
> Windows CPython 3.11 produces `site-packages/jep/` containing *exactly* this file set —
> `jep.dll`, `jep.cp311-win_amd64.dll`, `.exp`, `.lib`, `.pyd` — alongside 7 `.py` files and
> `jep-4.3.1.jar`. So these are pip-produced after all; someone copied the binaries out and left
> the Python package behind. `.github/workflows/build-app.yml` does the same thing deliberately
> (`Copy-Item "$jepDir\jep.dll"`), though it copies only one file, so the 5-file set was most
> likely lifted by hand from a local install. Either way the conclusion is unchanged: **binaries
> without the `.py` files are unusable**, and the workflow has the same defect as the local code.

### 3. `venv/` is a **Linux/WSL** venv, not a Windows one

`venv/pyvenv.cfg`:

```
home = /usr/bin
include-system-site-packages = false
version = 3.11.15
executable = /usr/bin/python3.11
command = /usr/bin/python3.11 -m venv /mnt/c/Users/Victor/Documents/Projects/Board-Game-Store/venv
```

It has `bin/`, `lib/`, `lib64/` (POSIX layout), not `Scripts/`, `Lib/`.

**jep *is* pip-installed there** — `venv/lib/python3.11/site-packages/jep/` contains `__init__.py`, `console.py`, `java_import_hook.py`, `jdbc.py`, `redirect_streams.py`, `shared_modules_hook.py`, `version.py`, `jep-4.3.1.jar`, `jep.cpython-311-x86_64-linux-gnu.so`, `libjep.so`. `jep-4.3.1.dist-info/WHEEL` says `Tag: cp311-cp311-linux_x86_64`.

`src/main/resources/native/linux/` is a straight copy of the two `.so` files from that venv (identical sizes, 1,927,064 B). **The Linux natives are pip-produced; the Windows ones are not, and there is no Windows counterpart of this venv anywhere in the repo.**

### 4. There is no project Python 3.11 on Windows

```
where.exe python
  C:\Users\Victor\AppData\Local\hermes\hermes-agent\venv\Scripts\python.exe
  C:\Users\Victor\AppData\Local\Programs\Python\Python314\python.exe
  C:\Users\Victor\AppData\Local\Microsoft\WindowsApps\python.exe
```

`PYTHONHOME`, `PYTHONPATH`, `VIRTUAL_ENV` are all **unset**.

> **[CORRECTED 2026-08-04]** This section originally claimed "PATH contains only
> `...\Programs\Python\Python314\`" and that the Python on PATH is 3.14. That contradicts its own
> `where.exe` output above: the **first** hit wins, and `python --version` actually reports
> **3.11.15** — the `hermes-agent` venv shadows the 3.14 install. This does not change the
> section's conclusion, which stands: that venv belongs to an unrelated tool, so there is still
> **no *project-owned* Python 3.11 on Windows**. It does mean "the Windows Python is 3.14" should
> not be relied on anywhere.

The 3.14 install (`Python314\python314.dll`) is unusable by a cp311 `jep.dll` regardless — CPython's C API is not ABI-compatible across minor versions, and the import is by literal name `python311.dll` anyway.

`python311.dll` exists on the machine in exactly two places, both incidental (neither is "the project's Python"):

| Path | Arch | Notes |
|---|---|---|
| `C:\Users\Victor\AppData\Roaming\uv\python\cpython-3.11.15-windows-x86_64-none\python311.dll` | x64 | Full standalone CPython 3.11.15: `python.exe`, `Lib\`, `DLLs\`, `libs\`, `vcruntime140.dll`, and `Lib\site-packages` with only pip + setuptools (**no jep**). uv-managed. |
| `C:\Users\Victor\.lmstudio\extensions\backends\vendor\_amphibian\cpython3.11-win-x86@6\python311.dll` | x64 (despite the `win-x86` folder name) | LM Studio vendored runtime. Not ours to use. |

The uv one is a legitimate, complete, correctly-architected CPython 3.11 — a viable target for a fix, though owned by uv rather than by this project.

### 5. Reproduction and fix, empirically (JDK 25.0.2, x64)

Test harness: a single-file `LoadTest.java` doing `System.load(...)`, run under `java`. Nothing from the app was executed.

| # | Setup | Result |
|---|---|---|
| A | `System.load(<repo>\native\windows\jep.dll)`, nothing else | **FAIL** — `java.lang.UnsatisfiedLinkError: ...\jep.dll: Can't find dependent libraries` — *byte-for-byte the reported error* |
| B | `System.load(uv...\python311.dll)` **then** `System.load(...\jep.dll)` | **OK — both load** |
| C | prepend the uv python dir to `PATH` in the parent shell, then `System.load(...\jep.dll)` | **OK** |
| D | copy `python311.dll` **into the same folder as** `jep.dll`, `System.load` by absolute path, folder not on `PATH` | **FAIL** — same error. Confirms MS Learn: the loaded DLL's own folder is not searched for its dependencies. |
| E | `System.loadLibrary("jep")` with `-Djava.library.path=<folder containing both jep.dll and python311.dll>` | **FAIL** — same error. `java.library.path` locates `jep.dll` but does nothing for its dependencies. |
| F | as D, but with the process **current directory** set to that folder | **OK** — matches search-order step 11 ("the current folder"). Works, but fragile: any `chdir` or a shortcut with a different "Start in" breaks it. |

Test A is the definitive reproduction; B, C and F are three working mechanisms, corresponding respectively to search-order steps 4, 12 and 11.

Incidental JDK 25 observation, worth acting on separately: every `System.load`/`System.loadLibrary` call printed

```
WARNING: A restricted method in java.lang.System has been called
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```

The app should pass `--enable-native-access=IntelliJ.GUI` (the module in `module-info.java`) or add `Enable-Native-Access: ALL-UNNAMED` to the jar manifest.

### 6. `java.library.path` and env vars are effectively fixed at JVM start

- **`java.library.path`**: the JDK parses it once into `static final` fields. In `jdk.internal.loader.NativeLibraries`:
  ```java
  static final String[] SYS_PATHS  = ClassLoaderHelper.parsePath(StaticProperty.sunBootLibraryPath());
  static final String[] USER_PATHS = ClassLoaderHelper.parsePath(StaticProperty.javaLibraryPath());
  ```
  ([openjdk/jdk `NativeLibraries.java`](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/jdk/internal/loader/NativeLibraries.java) — `LibraryPaths` holder class; `StaticProperty` values are captured at bootstrap). `System.setProperty("java.library.path", ...)` after startup therefore has no effect. Test E shows it would not have helped anyway.
- **`PATH`**: `System.getenv` is documented as read-only ([`java.lang.System.getenv`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/System.html#getenv())); there is no supported way to change the *current process's* environment from Java, and Windows' loader reads the process environment block, not a Java map. So `PATH` must be right **before the JVM starts** (launcher script, or a relaunch) — it cannot be fixed from inside `jepDllLoader`.
- **What *can* be done at runtime**: `System.load()` of an absolute path (loaded-module list, step 4) — this is the only lever a pure-Java in-process fix has. `PyConfig.setHome(...)` / `MainInterpreter.setInitParams(...)` are also runtime-settable, but they affect Python's *own* startup (`PyConfig.home` → `MainInterpreter.java:150-156`) and `LibraryLocator`'s search (`LibraryLocator.java:63-82`); they do **not** influence the Windows DLL loader.

---

## Why the current approach fails

`src/main/java/Bridge/jepDllLoader.java` does, in `load()` (lines 32-42):

1. `copyResourceDir("/native/windows", <appHome>/temp/jep)` — copies **all five** Windows files into one directory (`jepDllLoader.java:38`, `:61-85`).
2. `MainInterpreter.setJepLibraryPath("<appHome>/temp/jep/jep.dll")` (`jepDllLoader.java:39-40`).

Then `JavaPythonBridge`'s static initializer constructs `new SharedInterpreter()` (`JavaPythonBridge.java:28-33`), which reaches `MainInterpreter.initialize()` → the `jepLibraryPath != null` branch → bare `System.load(...)` → **`UnsatisfiedLinkError: Can't find dependent libraries`**.

Four distinct defects, each independently fatal:

1. **The class doc's premise is wrong.** `jepDllLoader.java:17-20` states that jep's dependencies "have to sit next to it". On Windows that is false — Test D proves the sibling directory is not searched, exactly as MS Learn documents. Nothing in `temp\jep\` can satisfy `python311.dll`, and none of the copied files *is* `python311.dll` anyway.
2. **`setJepLibraryPath` disables JEP's own recovery.** `LibraryLocator.searchPackageDir` catches this precise `UnsatisfiedLinkError` and preloads `pythonXX.dll` from `PYTHONHOME` (`LibraryLocator.java:244-262`, `:299-313`). The `jepLibraryPath` branch never runs any of it (`MainInterpreter.java:125-126`).
3. **No Python 3.11 for Windows is provisioned.** Even with perfect DLL plumbing there is no project-owned `python311.dll` + stdlib to load; `PYTHONHOME`/`PYTHONPATH`/`VIRTUAL_ENV` are unset and the Windows Python on PATH is 3.14.
4. **The `jep` Python package is never made importable.** `Jep.java:216` (`import jep`) and `Jep.java:243` (`from jep import java_import_hook`) run for every interpreter. Copying `.dll`/`.pyd`/`.lib`/`.exp` provides no `.py` files. This failure is *hidden* today behind the DLL error and will surface immediately once the DLL loads.

Two lesser problems in the same file:

- Copying `jep.cp311-win_amd64.pyd` next to `jep.dll` is the configuration the JEP wiki says "will get loaded first and will not work correctly" — remove it from the shipped resources.
- `PythonScriptLoader` writes `DatabaseManager.py` into `<appHome>/temp/`, but `DatabaseManager.py` resolves the DB two levels up from `__file__` (`temp/` → `<appHome>` → then `data/StoreData.db`). That happens to work for `<appHome>/temp/DatabaseManager.py` only because of the specific nesting; it is worth re-checking once the interpreter actually starts, since nothing has ever exercised it.

---

## Options, ranked

Deployment goal: a double-clickable jar with a `data/` folder beside it.

### Option 1 — real `pip install jep` into a project-owned Windows CPython 3.11, plus preload

> **[CORRECTED 2026-08-04]** Originally marked "(recommended)". **Option 2 was chosen instead**, because
> the app must ship for Windows, Linux and macOS — a requirement not known when this was written.
> Option 1 remains correct for a single-platform, developer-machine setup. See
> [Resolution](#resolution--what-was-actually-implemented).

**Steps**

1. Provision a Windows CPython 3.11 (x64). Either require the user to have one, or vendor a standalone build (see Option 2 for vendoring). Verify it is 3.11 x64 — the shipped `jep.dll` hard-imports `python311.dll` and CPython's C API is not stable across minor versions.
2. In that interpreter: `pip install jep==4.3.1`. On Windows this **builds** `jep.dll` with MSVC 2015/2017/2019 (per the [Windows wiki](https://github.com/ninia/jep/wiki/Windows)) and installs `jep.dll` + all the `.py` files + `jep-4.3.1.jar` into `<py>\Lib\site-packages\jep\`. This single step satisfies both the DLL and the `import jep` requirement.
3. Delete `src/main/resources/native/**` and all of `jepDllLoader`'s copy logic.
4. At startup, **before** creating any interpreter:
   - `MainInterpreter.setInitParams(PyConfig.python().setHome("<pythonRoot>"))`, and
   - either set `PYTHONHOME=<pythonRoot>` in the launcher and let `System.loadLibrary("jep")` fail through to `LibraryLocator` (JEP then preloads `python311.dll` and finds `site-packages\jep\jep.dll` for you — zero custom code), **or**
   - do it explicitly: `System.load("<pythonRoot>\\python311.dll")` then `MainInterpreter.setJepLibraryPath("<pythonRoot>\\Lib\\site-packages\\jep\\jep.dll")`.

**Tradeoffs.** Correct, matches JEP's documented model, fixes all four defects at once. But it requires a real Python 3.11 on the target machine (with a compiler at install time unless a prebuilt is used), so "double-clickable" needs a first-run setup step. Best combined with Option 2 for a shippable product.

### Option 2 — vendor a full standalone CPython 3.11 next to the jar + launcher script

**Steps**

1. Ship `<appHome>/python/` = a full standalone CPython 3.11 x64 (e.g. an [astral-sh/python-build-standalone](https://github.com/astral-sh/python-build-standalone) distribution — this is exactly what the uv install found on this machine is, and it has `python.exe`, `Lib\`, `DLLs\`, `python311.dll`, `vcruntime140.dll`).
2. Pre-install jep into `<appHome>/python/Lib/site-packages/jep/` at build time (build once on a machine with MSVC, then vendor the result).
3. Ship a `run.cmd` (or an exe launcher / `jpackage` app-image) beside the jar:
   ```bat
   @echo off
   set "PYTHONHOME=%~dp0python"
   set "PATH=%~dp0python;%PATH%"
   java --enable-native-access=IntelliJ.GUI -jar "%~dp0GUI.jar" %*
   ```
   `PATH` then satisfies the loader at search step 12 (Test C), and `PYTHONHOME` drives both `LibraryLocator` and Python's own startup.
4. Layout stays `<appHome>/{GUI.jar, run.cmd, python/, data/}` — the `data/` sibling contract is preserved.

**Tradeoffs.** Fully self-contained and reproducible; no user-side Python. Costs ~30-60 MB and a build-time step. The double-click target becomes `run.cmd`/the `jpackage` launcher rather than the bare `.jar` — acceptable, and `jpackage` makes it a proper `.exe`. **This is the right answer if the goal is genuine shippability.**

Variant: keep the bare jar clickable by having `Main` detect a missing `PYTHONHOME` and **relaunch itself** via `Runtime.exec`/`ProcessBuilder` with the environment set (`ProcessBuilder.environment()` *can* set env for a child, unlike for self). Works, but double-launch has ugly edge cases (splash flicker, exit codes, single-instance, debugger attach) — prefer the launcher script.

### Option 3 — preload `python311.dll` from Java, keep everything else in-process

**Steps.** In `jepDllLoader.load()`, before `setJepLibraryPath`: locate a Windows CPython 3.11 root (config file, env var, `py -3.11 -c "import sys;print(sys.base_prefix)"`, or a vendored `<appHome>/python`), then `System.load(root + "\\python311.dll")`, then point JEP at a real `site-packages\jep\jep.dll`. Also `PyConfig.setHome(root)`.

**Tradeoffs.** Verified to work for the DLL (Test B), needs no launcher script and no environment changes — the loaded-module list (search step 4) does the work, and it is the same trick JEP itself uses (`LibraryLocator.java:299-313`). **But it only solves the DLL half**: you still need the `jep` Python package and a real stdlib, so it is a *component* of Options 1/2, not a standalone fix. Do not pair it with the hand-copied `temp/jep/jep.dll` — see Option 5.

### Option 4 — `AddDllDirectory` / `SetDefaultDllDirectories` from Java

**Steps.** Via the FFM API (`java.lang.foreign`, JDK 22+), call `SetDefaultDllDirectories(LOAD_LIBRARY_SEARCH_DEFAULT_DIRS)` then `AddDllDirectory(L"<pythonRoot>")`, then load jep.

**Tradeoffs.** Would work in principle, but it is strictly more complex than Option 3 for the same outcome, needs `--enable-native-access`, is Windows-only code in a cross-platform loader, and `SetDefaultDllDirectories` changes process-wide loader behaviour (it *removes* the current directory and `PATH` from the default search), which can break unrelated native loads — JavaFX loads several native libraries of its own. **Not recommended.**

### Option 5 (do not do) — keep hand-copying binaries and just add a preload

Preloading `python311.dll` and then `System.load`-ing the hand-copied `temp\jep\jep.dll` will get past the current error, but leaves you with: a `jep.dll` whose build must exactly match the vendored Python; a stray `.pyd` in the same folder that the JEP wiki says must not be there; no `jep` Python package; and no stdlib. It converts a clear error into a later, more confusing one. The binaries under `src/main/resources/native/windows/` should be deleted, not patched around.

### Sanity check to run first (cheap, 2 minutes)

Before implementing anything, confirm the whole chain end-to-end by hand:

```powershell
$py = "C:\Users\Victor\AppData\Roaming\uv\python\cpython-3.11.15-windows-x86_64-none"
# in a scratch copy, NOT the project venv:
& "$py\python.exe" -m pip install jep==4.3.1     # needs MSVC; will fail loudly if absent
$env:PYTHONHOME = $py
$env:PATH = "$py;$env:PATH"
java -cp "<jep jar>;<classes>" --enable-native-access=ALL-UNNAMED <a tiny SharedInterpreter smoke test>
```

If `pip install jep` fails for lack of MSVC, that immediately tells you Option 2 (vendor a prebuilt) is mandatory rather than optional.

---

## Open questions / what I could not verify

- ~~**Where did `src/main/resources/native/windows/*.dll` come from?**~~ **ANSWERED** — pip-produced; see the corrected provenance note in "Observed on this machine" §2.
- ~~**Does `pip install jep==4.3.1` actually succeed on this machine?**~~ **ANSWERED: yes.** The `dumpbin`-absence inference was wrong. `pip install jep==4.3.1` into a copy of the uv CPython 3.11.15 **compiled the extension from source** and produced `jep-4.3.1-cp311-cp311-win_amd64.whl` — so a working MSVC toolchain *is* installed. This makes vendoring a prebuilt a convenience rather than a necessity.
  - Practical wrinkle: a copied uv distribution carries `Lib/EXTERNALLY-MANAGED`, so pip refuses with PEP 668 `externally-managed-environment` until that marker is removed (or `--break-system-packages` is passed). Any vendoring script must handle this.
- ~~**Does the full chain work end-to-end?**~~ **ANSWERED: yes.** With `PYTHONHOME` and `PATH` set by the launcher and **no** `setJepLibraryPath` call, a `SharedInterpreter` starts, `import sqlite3` works, `DatabaseManager.py` runs, and `get_user_details` / `get_admin_products_raw` return real rows from `data/StoreData.db`. The full CLI (`CLIbasis.CLIbasis.Main`) now launches from `Scripts/run.bat` and lists real users.
- ~~**`DatabaseManager.py`'s `__file__`-relative DB resolution** is unverified.~~ **ANSWERED — and it was broken.** See the new finding below.

### NEW FINDING (2026-08-04): `runScript()` does not define `__file__`

`Jep.runScript()` compiles and execs the file without setting `__file__` in the interpreter's
globals — unlike `python script.py`, which always sets it. `DatabaseManager.py`'s `init_paths()`
therefore failed outright:

```
jep.JepException: <class 'NameError'>: name '__file__' is not defined
    at ...DatabaseManager.init_paths(DatabaseManager.py:17)
    at ...DatabaseManager.<module>(DatabaseManager.py:33)
    at jep.Jep.runScript(Jep.java:290)
```

This was latent — hidden behind the DLL error — and would have surfaced the instant the native
library issue was fixed. **Fix:** `interp.set("__file__", scriptPath)` immediately before
`interp.runScript(scriptPath)`. Verified: `_DB_PATH` then resolves correctly and queries return
data. This adds no path-resolution duplication — the bridge already holds `scriptPath` and passes
it to `runScript` on the next line; Python remains the sole owner of deriving `data/StoreData.db`.

### Still open

- **Linux and macOS are unverified.** Everything above was proven on Windows only. `native/mac/`
  was empty and has been deleted along with the rest; macOS also needs the `.dylib` → `.jnilib`
  mapping `LibraryLocator` applies (`LibraryLocator.java:84-92`).
- **`get_user_role` crashes on unknown IDs** — `rows[0][0]` with no empty-result check raises
  `IndexError` rather than returning a permission error. Found incidentally while smoke-testing
  with a wrong user ID. Unrelated to JEP, but it will surface as a confusing `JepException`.
- **macOS** is entirely unaddressed: `src/main/resources/native/mac/` is empty and `jepDllLoader.mainLibraryFileName` returns `libjep.dylib`, while `LibraryLocator` maps `.dylib` → `.jnilib` for macOS (`LibraryLocator.java:84-92`). Out of scope here, but it will break the same way.
- **`jep` 4.3.1 vs Python 3.14**: the METADATA classifiers list 3.10–3.14, so a 3.14 build is nominally supported. If a 3.14 `jep.dll` were built, it would import `python314.dll`, which *is* already on this machine's PATH — potentially a simpler provisioning story than 3.11. Not investigated further; it would require rebuilding the native library and re-doing the Linux side too.

## Resolution — what was actually implemented

Chosen: **Option 2** (vendored standalone CPython + launcher), driven by a constraint that emerged
after this document was written — the app must ship for Windows, Linux and macOS. Since JEP embeds
a *native* CPython, **a single cross-platform jar is impossible**; the jar stays platform-neutral
and the *bundle* becomes platform-specific. Implemented on Windows 2026-08-04:

**Deleted**

- `src/main/java/Bridge/jepDllLoader.java` — its entire purpose was hand-loading the native library.
  Its `setJepLibraryPath` call was the direct cause of the bug (defect 2 above).
- `src/main/resources/native/**` — all 7 binaries, all three platforms.

**Changed**

- `Bridge/PythonScriptLoader.java` — absorbed `tempDir()` / `appHome()` from the deleted loader.
  Behaviour unchanged: `DatabaseManager.py` still lands in `temp/`, a sibling of `data/`.
- `Bridge/JavaPythonBridge.java` — removed the `jepDllLoader.load()` static block entirely. JEP now
  resolves its own native library through `PYTHONHOME` → `LibraryLocator`, which also preloads
  `python311.dll` (`LibraryLocator.java:299-313`). **No custom native-loading code remains.** Added
  `interp.set("__file__", ...)` per the new finding above.
- `Board Game Store/Scripts/run.bat` — sets `PYTHONHOME=%APP_HOME%\python` and prepends
  `%APP_HOME%\python` to `PATH` before invoking `java`. Also switched `-jar` → `-cp`: `bgms.jar`'s
  manifest has no `Class-Path`, so `-jar` could never have resolved `jep-4.3.1.jar`.
- `Board Game Store/Scripts/run.sh` — same, branching `LD_LIBRARY_PATH` / `DYLD_LIBRARY_PATH` on
  `uname`. It had also been referencing a nonexistent `MyProject.jar` without the `..` hop.

**Bundle layout** (`data/` sibling contract preserved):

```
Board Game Store/
├── bgms.jar
├── jep-4.3.1.jar, gson-2.10.1.jar, lib/
├── python/          ← standalone CPython 3.11.15 + jep in site-packages (~73 MB)
├── data/StoreData.db
├── temp/            ← created at runtime; DatabaseManager.py copied here
└── Scripts/run.bat, run.sh
```

**Verified working end-to-end** — `Scripts\run.bat` launches the CLI and lists real users read from
SQLite through JEP.

**Still to do:** rewrite `.github/workflows/build-app.yml` to assemble these bundles per platform
rather than extracting native libraries into jar resources. That workflow also uploads
`target/*-shaded.jar`, which matches nothing (the pom has no shade plugin), and builds natives on
JDK 21 while packaging on JDK 25.

## References

- jep 4.3.1 sources: `C:\Users\Victor\.m2\repository\org\ninia\jep\4.3.1\jep-4.3.1-sources.jar` (`jep/MainInterpreter.java`, `jep/LibraryLocator.java`, `jep/Jep.java`, `jep/PyConfig.java`)
- https://github.com/ninia/jep — repo; https://github.com/ninia/jep/wiki — wiki
- https://github.com/ninia/jep/wiki/Getting-Started
- https://github.com/ninia/jep/wiki/FAQ
- https://github.com/ninia/jep/wiki/Windows
- https://learn.microsoft.com/en-us/windows/win32/dlls/dynamic-link-library-search-order
- https://learn.microsoft.com/en-us/windows/win32/api/libloaderapi/nf-libloaderapi-setdefaultdlldirectories
- https://learn.microsoft.com/en-us/windows/win32/api/libloaderapi/nf-libloaderapi-adddlldirectory
- https://learn.microsoft.com/en-us/cpp/build/reference/dot-lib-files-as-linker-input
- https://docs.python.org/3/c-api/init_config.html (PyConfig, `home`)
- https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/System.html#getenv()
- https://github.com/openjdk/jdk — `jdk.internal.loader.NativeLibraries` (cached `java.library.path`)
- Secondary, pointer only: https://github.com/ninia/jep/issues/517 (same error reported upstream; no maintainer diagnosis in the thread)
