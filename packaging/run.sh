#!/bin/bash
set -e

# Bundle root is one level up from this Scripts folder.
APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# JEP embeds a native CPython. PYTHONHOME tells both JEP's LibraryLocator and Python
# itself where the bundled interpreter lives; the library path lets the dynamic loader
# resolve libpython3.11.so, which libjep.so links against. Both must be set before the
# JVM starts - they cannot be fixed from inside Java.
export PYTHONHOME="$APP_HOME/python"

case "$(uname -s)" in
  Darwin)
    export DYLD_LIBRARY_PATH="$APP_HOME/python/lib:${DYLD_LIBRARY_PATH:-}"
    ;;
  *)
    export LD_LIBRARY_PATH="$APP_HOME/python/lib:${LD_LIBRARY_PATH:-}"
    ;;
esac

exec java --enable-native-access=ALL-UNNAMED \
  -cp "$APP_HOME/bgms.jar:$APP_HOME/lib/*" \
  CLIbasis.CLIbasis.Main "$@"
