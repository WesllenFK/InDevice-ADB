#!/data/data/com.termux/files/usr/bin/bash
# adb-discover.sh — Discover ADB wireless targets
# Tries: 1. adb mdns services  2. app NsdManager  3. localhost scan
set -euo pipefail

ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"

init_state
HELP=false
SERVICE_TYPE="pairing"
parse_args "$@"
[ "$HELP" = "true" ] && { msgf "help_discover"; exit 0; }

# Parse service type from remaining args
for arg in "$@"; do
  case "$arg" in
    --connect| -c) SERVICE_TYPE="connect" ;;
    --pair|-p) SERVICE_TYPE="pairing" ;;
  esac
done

MDNS_SERVICE="pairing" && [ "$SERVICE_TYPE" = "connect" ] && MDNS_SERVICE="_adb-tls-connect._tcp" || MDNS_SERVICE="_adb-tls-pairing._tcp"

# --- Attempt 1: adb mdns services ---
targets="$(adb mdns services 2>/dev/null | awk -v srv="$MDNS_SERVICE" '$0 ~ srv { print $(NF-1) ":" $NF }' || true)"
if [ -n "$targets" ]; then
  echo "$targets"
  exit 0
fi

# --- Attempt 2: app NsdManager ---
: > "$DISCOVERED_FILE" 2>/dev/null || true
/system/bin/am broadcast --user 0 -n "$APP_RECEIVER" \
  --es mode discover_adb \
  --es service_type "$SERVICE_TYPE" \
  --es output "$DISCOVERED_FILE" >/dev/null 2>&1 || true

waited=0
while [ "$waited" -lt "$TIMEOUT" ]; do
  if [ -s "$DISCOVERED_FILE" ] 2>/dev/null; then
    target="$(head -1 "$DISCOVERED_FILE" | tr -d '\r\n')"
    if [ -n "$target" ]; then echo "$target"; exit 0; fi
  fi
  sleep 1; waited=$((waited + 1))
done

# --- Attempt 3: localhost scan ---
TMP_PORTS="$(mktemp)"
trap 'rm -f "$TMP_PORTS"' EXIT

python3 -u -c "
import socket, sys
open_ports = []
for p in range(35000, 45001):
    s = socket.socket()
    s.settimeout(0.015)
    try:
        s.connect(('127.0.0.1', p))
        s.close()
        open_ports.append(p)
    except:
        s.close()
if open_ports:
    for p in sorted(open_ports):
        print('%d' % p)
    sys.exit(0)
sys.exit(1)
" 2>/dev/null > "$TMP_PORTS" || exit 1

if [ -s "$TMP_PORTS" ]; then
  while IFS= read -r port; do
    t="127.0.0.1:$port"
    out="$(timeout 4 adb pair "$t" 000000 2>&1 || true)"
    if printf '%s\n' "$out" | grep -qi 'failed to authenticate\|pairing code\|successfully paired'; then
      printf '%s\n' "$t"
      exit 0
    fi
  done < "$TMP_PORTS"
fi

exit 1
