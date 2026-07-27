#!/data/data/com.termux/files/usr/bin/bash
# adb-connect.sh — Quick ADB connect with auto-discovery
set -euo pipefail

ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"

init_state
HELP=false
parse_args "$@"
[ "$HELP" = "true" ] && { msgf "help_connect"; exit 0; }

RUN_ID="$(date +%s)"
_START_TS="$(date +%s)"
DURATION=0
_duration() { DURATION="$(echo "$(date +%s) - $_START_TS" | bc)"; }

msg "discovering"
connect_target="$(discover_connect "$TIMEOUT" || true)"

if [ -z "$connect_target" ]; then
  msg "discovered_none"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "" "no_target" "$DURATION"
  exit 1
fi

msg "connecting"
log INFO "connecting to $connect_target"
if do_connect "$connect_target"; then
  log INFO "connected to $connect_target"
  notify "ok-$RUN_ID" "$(msg success)" "$(msgf connect_ok) $connect_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "connected" "$connect_target" "connect" "$DURATION"
  exit 0
fi

notify "fail-$RUN_ID" "$(msg failure)" "$(msgf connect_fail) $connect_target"
_duration; [ "$JSON_OUT" = "true" ] && output_json "error" "$connect_target" "connect_failed" "$DURATION"
exit 1
