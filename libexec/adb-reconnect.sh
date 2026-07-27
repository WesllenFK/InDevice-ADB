#!/data/data/com.termux/files/usr/bin/bash
# adb-reconnect.sh — Reconnect to last saved ADB target
set -euo pipefail

ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"

init_state
HELP=false
parse_args "$@"
[ "$HELP" = "true" ] && { msgf "help_reconnect"; exit 0; }

RUN_ID="$(date +%s)"
_START_TS="$(date +%s)"
DURATION=0
_duration() { DURATION="$(echo "$(date +%s) - $_START_TS" | bc)"; }

if [ ! -f "$LAST_TARGET_FILE" ]; then
  msg "reconnect_no_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "" "no_saved_target" "$DURATION"
  exit 1
fi

connect_target="$(cat "$LAST_TARGET_FILE")"
log INFO "reconnecting to $connect_target"

if do_connect "$connect_target"; then
  log INFO "reconnect ok to $connect_target"
  notify "ok-$RUN_ID" "$(msg success)" "$(msgf reconnect_ok) $connect_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "connected" "$connect_target" "reconnect" "$DURATION"
  exit 0
fi

notify "fail-$RUN_ID" "$(msg failure)" "$(msgf reconnect_fail) $connect_target"
_duration; [ "$JSON_OUT" = "true" ] && output_json "error" "$connect_target" "reconnect_failed" "$DURATION"
exit 1
