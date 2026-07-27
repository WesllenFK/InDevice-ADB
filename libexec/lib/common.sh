# shellcheck shell=bash
# common.sh — Shared functions for ADB Parity scripts
# Must be sourced: source common.sh
set -euo pipefail

# ── Default paths ──────────────────────────────────────────────────────────
readonly STATE_DIR="$HOME/.adbparity"
readonly REPLY_FILE="/sdcard/Documents/adb-notify/reply.txt"
readonly DISCOVERED_FILE="/sdcard/Documents/adb-notify/adb-discovered.txt"
readonly PING_FILE="/sdcard/Documents/adb-notify/ping.txt"
readonly DEBUG_DIR="/sdcard/Documents/adb-notify/debug"
readonly APP_PACKAGE="com.miniadbnotify"
readonly APP_RECEIVER="com.miniadbnotify/.NotifyReceiver"
readonly LOG_FILE="$STATE_DIR/adbparity.log"
readonly LAST_TARGET_FILE="$STATE_DIR/last-target.txt"

# ── Logging ────────────────────────────────────────────────────────────────
log() {
  local level="$1" msg="$2"
  printf '[%s] [%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$level" "$msg" | tee -a "$LOG_FILE" 2>/dev/null || true
}

# ── Die with optional notification ─────────────────────────────────────────
die() {
  local msg="$1" code="${2:-1}"
  log "ERROR" "$msg"
  [ "$JSON_OUT" = "true" ] && output_json "error" "" "" 0
  exit "$code"
}

# ── Notifications ──────────────────────────────────────────────────────────
notify() {
  local id="$1" title="$2" content="$3" button="${4:-OK}"
  [ "${NO_NOTIFY:-false}" = "true" ] && return 0
  /system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
    --es mode show \
    --es id "$id" \
    --es title "$title" \
    --es content "$content" \
    --es button "$button" >/dev/null 2>&1 || true
}

notify_clear() {
  local id="$1"
  /system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
    --es mode clear \
    --es id "$id" >/dev/null 2>&1 || true
}

# ── Bilingual output ───────────────────────────────────────────────────────
msg() {
  if declare -F _i18n_msg >/dev/null 2>&1; then _i18n_msg "$@"
  else printf '%s' "$*"
  fi
}

msgf() {
  if declare -F _i18n_msgf >/dev/null 2>&1; then _i18n_msgf "$@"
  else printf '%s' "$*"
  fi
}

# ── ADB operations ─────────────────────────────────────────────────────────
ping_app() {
  local timeout="${1:-5}"
  : > "$PING_FILE" 2>/dev/null || true
  /system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
    --es mode ping >/dev/null 2>&1 || true
  local waited=0
  while [ "$waited" -lt "$timeout" ]; do
    if [ -s "$PING_FILE" ] 2>/dev/null; then return 0; fi
    sleep 1; waited=$((waited + 1))
  done
  return 1
}

discover_connect() {
  local timeout="${1:-30}"
  : > "$DISCOVERED_FILE" 2>/dev/null || true
  /system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
    --es mode discover_adb \
    --es service_type connect \
    --es output "$DISCOVERED_FILE" >/dev/null 2>&1 || true
  local waited=0
  while [ "$waited" -lt "$timeout" ]; do
    if [ -s "$DISCOVERED_FILE" ] 2>/dev/null; then
      TARGET="$(head -1 "$DISCOVERED_FILE" | tr -d '\r\n')"
      if [ -n "$TARGET" ]; then
        printf '%s\n' "$TARGET"
        return 0
      fi
    fi
    sleep 1; waited=$((waited + 1))
  done
  return 1
}

discover_pair() {
  local timeout="${1:-60}"
  TARGET=""; CODE=""
  : > "$REPLY_FILE" 2>/dev/null || true
  /system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
    --es mode discover_and_pair \
    --es output "$REPLY_FILE" >/dev/null 2>&1 || true
  local waited=0
  while [ "$waited" -lt "$timeout" ]; do
    if [ -s "$REPLY_FILE" ] 2>/dev/null; then
      TARGET="$(sed -n '1p' "$REPLY_FILE" | tr -d '\r\n')"
      CODE="$(sed -n '2p' "$REPLY_FILE" | tr -d '\r\n')"
      if [ -n "$TARGET" ] && [ -n "$CODE" ]; then
        printf '%s\n%s\n' "$TARGET" "$CODE"
        return 0
      fi
    fi
    sleep 1; waited=$((waited + 1))
  done
  return 1
}

do_connect() {
  local target="$1"
  local host="${target%%:*}"
  set +e
  timeout 5 adb connect "$target" >/dev/null 2>&1
  sleep 2
  if adb devices 2>/dev/null | grep -q "$host"; then
    set -e; printf '%s\n' "$target" > "$LAST_TARGET_FILE"
    return 0
  fi
  set -e; return 1
}

do_pair() {
  local target="$1" code="$2"
  local out
  set +e
  out="$(printf '%s\n' "$code" | timeout 10 adb pair "$target" 2>&1)"
  local st=$?
  set -e
  printf '%s\n' "$out" >> "$LOG_FILE" 2>/dev/null || true
  [ "$st" -eq 0 ] && return 0
  return 1
}

# ── State management ──────────────────────────────────────────────────────
init_state() {
  local old_pair="$HOME/.adb-pair-notify"
  local old_connect="$HOME/.adb-connect-notify"
  mkdir -p "$STATE_DIR" "$DEBUG_DIR"
  if [ -d "$old_pair" ]; then
    cp -r "$old_pair"/* "$STATE_DIR/" 2>/dev/null || true
    rm -rf "$old_pair" 2>/dev/null || true
  fi
  if [ -d "$old_connect" ]; then
    cp -r "$old_connect"/* "$STATE_DIR/" 2>/dev/null || true
    rm -rf "$old_connect" 2>/dev/null || true
  fi
}

# ── JSON output ────────────────────────────────────────────────────────────
output_json() {
  local status="$1" target="$2" mode="$3" duration="$4"
  printf '{"status":"%s","target":"%s","mode":"%s","timestamp":"%s","duration":%.1f}\n' \
    "$status" "$target" "$mode" "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$duration"
}

# ── Help text ──────────────────────────────────────────────────────────────
print_help() {
  local name="${1:-adb-pair}"
  msgf "help_header" "$name"
}

# ── CLI argument parsing ───────────────────────────────────────────────────
VERBOSE=false; JSON_OUT=false; NO_NOTIFY=false; TIMEOUT=30; CONNECT_ONLY=false
TARGET=""; CODE=""; DURATION=0

parse_args() {
  while [ $# -gt 0 ]; do
    case "$1" in
      -h|--help) HELP=true; return 0 ;;
      -v|--verbose) VERBOSE=true; shift ;;
      -j|--json) JSON_OUT=true; shift ;;
      -l|--lang)
        [ $# -lt 2 ] && { echo "Error: --lang requires pt or en" >&2; exit 2; }
        case "$2" in pt|en) ADBPARITY_LANG="$2"; shift 2 ;; *) echo "Error: invalid lang" >&2; exit 2 ;; esac ;;
      --no-notify) NO_NOTIFY=true; shift ;;
      --timeout)
        [ $# -lt 2 ] && { echo "Error: --timeout requires number" >&2; exit 2; }
        TIMEOUT="$2"; shift 2 ;;
      --connect-only) CONNECT_ONLY=true; shift ;;
      *) printf 'Error: unknown option %s\n' "$1" >&2; exit 2 ;;
    esac
  done
}
