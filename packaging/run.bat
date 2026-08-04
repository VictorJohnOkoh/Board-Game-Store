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

java --enable-native-access=ALL-UNNAMED ^
  -cp "%APP_HOME%\bgms.jar;%APP_HOME%\lib\*" ^
  CLIbasis.CLIbasis.Main %*

endlocal
pause
