@echo off
setlocal

rem Bundle root is one level up from this Scripts folder.
set "APP_HOME=%~dp0.."

rem JEP embeds a native CPython. PYTHONHOME tells both JEP's LibraryLocator and Python
rem itself where the bundled interpreter lives; PATH lets the Windows loader resolve
rem python311.dll, which jep.dll imports. Both must be set before the JVM starts -
rem they cannot be fixed from inside Java.
set "PYTHONHOME=%APP_HOME%\python"
set "PATH=%APP_HOME%\python;%PATH%"

rem JavaFX ships as real modules with bundled natives, so it goes on the module path,
rem not the classpath. These jars are platform-specific, like jep's native library.
rem Native access is granted to two things: the unnamed module (jep, loaded from the
rem classpath) and javafx.graphics, which loads its own natives and is a named module,
rem so ALL-UNNAMED does not cover it.
java --enable-native-access=ALL-UNNAMED,javafx.graphics ^
  --module-path "%APP_HOME%\javafx" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "%APP_HOME%\bgms.jar;%APP_HOME%\lib\*" ^
  gui.app.MainApp %*

endlocal
pause
