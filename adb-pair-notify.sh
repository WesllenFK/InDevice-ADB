#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Use `adb-pair` instead.
exec "$(dirname "$(readlink -f "$0")")/bin/adb-pair" "$@"
