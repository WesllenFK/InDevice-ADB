#!/data/data/com.termux/files/usr/bin/bash
# [DEPRECATED] Manual notification send.
# Usage: adb-notify-send.sh --id ID --title TITLE --content CONTENT --button BUTTON
ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"

id=""; title=""; content=""; button="OK"
while [ $# -gt 0 ]; do
  case "$1" in
    --id) id="$2"; shift 2 ;;
    --title) title="$2"; shift 2 ;;
    --content) content="$2"; shift 2 ;;
    --button) button="$2"; shift 2 ;;
    *) echo "Unknown: $1"; exit 2 ;;
  esac
done
[ -n "$id" ] && notify "$id" "$title" "$content" "$button"
