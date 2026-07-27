#!/data/data/com.termux/files/usr/bin/bash
# adb-pair.sh — Wireless ADB pairing & connection (unified flow)
# Adapted from adb-pair-notify.sh — ping → connect → pair → connect
set -euo pipefail

ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"

init_state
HELP=false
parse_args "$@"
[ "$HELP" = "true" ] && { msgf "help_pair"; exit 0; }

RUN_ID="$(date +%s)"
TARGET=""; CODE=""
_START_TS="$(date +%s)"
DURATION=0

_duration() { DURATION="$(echo "$(date +%s) - $_START_TS" | bc)"; }

# --- Step 1: Ping app ---
msg "app_pinging"
ping_app; ping_st=$?
if [ "$ping_st" -eq 2 ]; then
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "" "write_error" "$DURATION"
  exit 5
fi
if [ "$ping_st" -ne 0 ]; then
  msg "app_offline"
  notify "fail-$RUN_ID" "$(msg failure)" "$(msg error_app_offline)"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "offline" "" "" "$DURATION"
  exit 3
fi
msg "app_online"
log INFO "app online"

# --- Step 2: Try direct connect ---
msg "discovering"
connect_target="$(discover_connect "$TIMEOUT" || true)"

if [ -n "$connect_target" ]; then
  log INFO "connect target found: $connect_target"
  msg "discovered_one"
  if do_connect "$connect_target"; then
    log INFO "connected to $connect_target"
    notify "ok-$RUN_ID" "$(msg success)" "$(msgf connect_ok) $connect_target"
    _duration; [ "$JSON_OUT" = "true" ] && output_json "connected" "$connect_target" "direct" "$DURATION"
    exit 0
  fi
  log INFO "direct connect failed on $connect_target"
fi

# --- Step 3: Pair (unless --connect-only) ---
if [ "$CONNECT_ONLY" = "true" ]; then
  log INFO "connect-only mode, no pairing attempted"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "" "no_target" "$DURATION"
  exit 1
fi

msg "discovering"
pair_result="$(discover_pair "$TIMEOUT" || true)"
pair_target="$TARGET"
pair_code="$CODE"

if [ -z "$pair_target" ] || [ -z "$pair_code" ]; then
  log INFO "pair discovery timed out"
  notify "fail-$RUN_ID" "$(msg failure)" "$(msg pair_timeout)"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "" "pair_timeout" "$DURATION"
  exit 4
fi

msg "pairing"
log INFO "pairing $pair_target ..."
if ! do_pair "$pair_target" "$pair_code"; then
  log INFO "pair failed on $pair_target"
  notify "fail-$RUN_ID" "$(msg failure)" "$(msgf pair_fail) $pair_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "error" "$pair_target" "pair_failed" "$DURATION"
  exit 1
fi

msg "pair_ok"
log INFO "pair success on $pair_target"

# --- Step 4: Connect after pairing ---
msg "discovering"
sleep 5
connect_target="$(discover_connect "$TIMEOUT" || true)"

if [ -z "$connect_target" ]; then
  log INFO "connect target not found after pairing"
  notify "ok-$RUN_ID" "$(msg pair_ok)" "$(msgf pair_ok) $(msg pair_timeout)"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "paired" "$pair_target" "paired_no_connect" "$DURATION"
  exit 1
fi

log INFO "connecting to $connect_target ..."
if do_connect "$connect_target"; then
  log INFO "connected to $connect_target"
  notify "ok-$RUN_ID" "$(msg success)" "$(msgf connect_ok) $connect_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "connected" "$connect_target" "paired" "$DURATION"
  exit 0
fi

# --- Step 5: Retry once (stale NsdManager cache) ---
log INFO "connect failed, retrying discovery ..."
sleep 5
connect_target="$(discover_connect "$TIMEOUT" || true)"

if [ -z "$connect_target" ]; then
  log INFO "connect target not found on retry"
  notify "ok-$RUN_ID" "$(msg pair_ok)" "$(msgf pair_ok) $(msg pair_timeout)"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "paired" "$pair_target" "paired_no_connect" "$DURATION"
  exit 1
fi

if do_connect "$connect_target"; then
  log INFO "connected to $connect_target (retry)"
  notify "ok-$RUN_ID" "$(msg success)" "$(msgf connect_ok) $connect_target"
  _duration; [ "$JSON_OUT" = "true" ] && output_json "connected" "$connect_target" "paired_retry" "$DURATION"
  exit 0
fi

log INFO "connect failed after pairing retry"
notify "fail-$RUN_ID" "$(msg failure)" "$(msgf connect_fail) $connect_target"
_duration; [ "$JSON_OUT" = "true" ] && output_json "error" "$connect_target" "connect_failed" "$DURATION"
exit 1
