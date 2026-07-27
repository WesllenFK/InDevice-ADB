#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Use `adb-connect` instead.
exec "$(dirname "$(readlink -f "$0")")/bin/adb-connect" "$@"
