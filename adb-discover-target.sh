#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Use `adb-discover` instead.
exec "$(dirname "$(readlink -f "$0")")/bin/adb-discover" "$@"
