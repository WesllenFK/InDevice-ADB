#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Clear notification by ID.
ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
id="${1:-}"
if [ -n "$id" ]; then notify_clear "$id"; else echo "Usage: adb-notify-clear.sh <notification_id>"; fi
