#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Use `adb-reconnect` instead.
exec "$(dirname "$(readlink -f "$0")")/bin/adb-reconnect" "$@"
