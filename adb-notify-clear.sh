#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Clear notification by ID.
ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
[ -n "$1" ] && notify_clear "$1" || echo "Usage: adb-notify-clear.sh <notification_id>"
