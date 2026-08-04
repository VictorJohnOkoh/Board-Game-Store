# JavaFX licence files — provenance

These files are vendored, not generated. They cover the JavaFX binaries shipped in each
platform bundle under `javafx/` (see `.github/workflows/build-app.yml`).

## Why they are vendored

They cannot be extracted from what the build already downloads:

- None of the `org.openjfx` jars contain any licence text. Checked every jar in the
  bundle; the only apparent hit was `IllegalPathStateException.class`, which merely
  contains the substring "legal".
- The openjfx POMs declare no `<licenses>` section — only `<name>` and `<url>`.

Fetching them during CI would add a network dependency and make builds
non-reproducible, so they are committed here instead.

## Source

Fetched from the canonical OpenJFX repository, pinned to an exact commit:

- Repository: https://github.com/openjdk/jfx
- Commit: `f8e39e78b742534bb37a097d5c7e34b687e62c24`
- Files: `LICENSE`, `ADDITIONAL_LICENSE_INFO`, `ASSEMBLY_EXCEPTION`
- Retrieved: 2026-08-05

A UTF-8 BOM introduced by the download step was stripped; the text is otherwise byte-for-byte
as published.

## What they cover

JavaFX is distributed under **GPL v2 with the Classpath Exception**. All three files belong
together: `LICENSE` is the GPLv2 text, and `ADDITIONAL_LICENSE_INFO` and `ASSEMBLY_EXCEPTION`
describe the exceptions that permit linking and redistribution here.

## Updating

The GPLv2 text is stable across JavaFX releases, so this rarely needs touching. If the
JavaFX version in `pom.xml` changes substantially, re-fetch the three files from the tag
matching that release and update the commit hash above.
