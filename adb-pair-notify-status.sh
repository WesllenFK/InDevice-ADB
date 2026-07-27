#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Use `adb-parity-setup status` instead.
echo "=== ADB Parity Status ==="
echo "State dir: $HOME/.adbparity"
[ -f "$HOME/.adbparity/last-target.txt" ] && echo "Last target: $(cat $HOME/.adbparity/last-target.txt)"
[ -f "$HOME/.adbparity/adbparity.log" ] && echo "--- Log ---" && tail -5 "$HOME/.adbparity/adbparity.log"
