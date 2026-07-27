# ARCHITECTURE — ADB Parity Technical Architecture

## System Overview

```
┌─────────────┐     am broadcast      ┌──────────────────┐
│  Termux      │ ──────────────────►  │  MiniADBNotify   │
│  (PRoot)     │ ◄────────────────── │  (Android App)    │
│              │   files on /sdcard/  │                   │
│  adb-pair.sh │                      │  NsdManager       │
│  common.sh   │                      │  Notifications    │
└─────────────┘                      └──────────────────┘
       │                                      │
       │ adb pair / adb connect               │ mDNS / NsdManager
       ▼                                      ▼
┌──────────────┐                    ┌──────────────────┐
│  Target       │◄───────────────── │  Wireless Debug   │
│  ADB Device   │    TCP (WiFi)    │  (Android DevOpts) │
└──────────────┘                    └──────────────────┘
```

---

## File Structure

```
adbparity/
├── bin/                          # Entry points on $PATH
│   ├── adb-pair                  #   → libexec/adb-pair.sh
│   ├── adb-connect               #   → libexec/adb-connect.sh
│   ├── adb-reconnect             #   → libexec/adb-reconnect.sh
│   └── adb-discover              #   → libexec/adb-discover.sh
├── libexec/                      # Real implementations
│   ├── adb-pair.sh               # Full pair → connect flow
│   ├── adb-connect.sh            # Quick connect
│   ├── adb-reconnect.sh          # Reconnect to last target
│   ├── adb-discover.sh           # 3-tier discovery
│   └── lib/
│       ├── common.sh             # Shared functions
│       └── i18n.sh               # Bilingual message system
├── adb-notify/                   # Android app project
│   └── app/src/main/java/com/miniadbnotify/
│       ├── MainActivity.java     # Permission requests + UI
│       ├── NotifyReceiver.java   # BroadcastReceiver (ping, discover, notify)
│       └── ReplyReceiver.java    # Captures notification text input
├── state/                        # Runtime state (populated at ~/.adbparity/)
└── docs/                         # Documentation
```

---

## Script Architecture

### bin/ → libexec/ pattern

Each `bin/` entry point sources `common.sh` and `i18n.sh`, then `exec`s the
corresponding `libexec/` script. This keeps PATH entries lightweight and
centralizes library loading.

```bash
# bin/adb-pair
ADBPARITY_HOME="${ADBPARITY_HOME:-/root/adbparity}"
source "$ADBPARITY_HOME/libexec/lib/common.sh"
source "$ADBPARITY_HOME/libexec/lib/i18n.sh"
exec "$ADBPARITY_HOME/libexec/adb-pair.sh" "$@"
```

### Sourcing chain

```
adb-pair (bin)
  ├── common.sh      → log(), notify(), ping_app(), discover_connect(),
  │                     discover_pair(), do_connect(), do_pair(),
  │                     output_json(), print_help(), parse_args()
  ├── i18n.sh         → _i18n_msg(), _i18n_msgf(), _MSG[] dictionary
  └── adb-pair.sh     → step-by-step flow
```

---

## common.sh Functions

### `ping_app [timeout]`
Sends a `ping` broadcast to the app via `am broadcast`. Polls PING_FILE on
`/sdcard/Documents/adb-notify/ping.txt` for up to `timeout` seconds. Returns 0
if app responds.

### `discover_connect [timeout]`
Broadcasts `mode=discover_adb service_type=connect` to the app. App uses
NsdManager to discover `_adb-tls-connect._tcp` services. Result written to
`adb-discovered.txt` on `/sdcard/`. Returns the first discovered `IP:PORT`.

### `discover_pair [timeout]`
Broadcasts `mode=discover_and_pair` to the app. App discovers
`_adb-tls-pairing._tcp` via NsdManager, then shows a notification with a
RemoteInput text field for the 6-digit pairing code. User input is written to
`reply.txt` as `TARGET\nCODE\n`. Returns both target and code.

### `do_connect target`
Runs `adb connect <target>` (5s timeout), verifies presence in `adb devices`.
Saves target to `~/.adbparity/last-target.txt` on success.

### `do_pair target code`
Pipes `code` into `adb pair <target>` (10s timeout). Returns 0 on success.

### `init_state`
Creates `~/.adbparity/` and migrates data from old state dirs
(`~/.adb-pair-notify/`, `~/.adb-connect-notify/`).

### `output_json status target mode duration`
Prints a JSON object:
```json
{"status":"connected","target":"192.168.1.42:39815","mode":"direct","timestamp":"...","duration":2.3}
```

---

## i18n System

Messages are stored in an associative array `_MSG` with key-value pairs:

```bash
_MSG["pair_ok"]="pt:Pareado com sucesso|en:Pair successful"
```

`_i18n_msg(key)` parses the entry based on `ADBPARITY_LANG` (pt|en). The `msg()`
and `msgf()` wrappers in common.sh call through to these.

---

## App ↔ Script Communication Protocol

### Broadcast modes (via `am broadcast`)

| Mode | Extras | Script function |
|------|--------|-----------------|
| `ping` | — | `ping_app()` |
| `discover_adb` | `service_type`=connect\|pairing, `output` | `discover_connect()` |
| `discover_and_pair` | `output` | `discover_pair()` |
| `show` | `id`, `title`, `content`, `button` | `notify()` |
| `clear` | `id` | `notify_clear()` |

### File-based replies

The script and app communicate through files on `/sdcard/Documents/adb-notify/`:

| File | Direction | Format |
|------|-----------|--------|
| `ping.txt` | App → Script | `ok\n` |
| `adb-discovered.txt` | App → Script | `IP:PORT\n` |
| `reply.txt` | App → Script | `IP:PORT\nCODE\n` |
| `reply-last.txt` | App → Script | `text\n` |
| `debug/notify.log` | App → Script (debug) | Free text |

### ReplyReceiver modes

When the user submits text through a notification:
- **pair mode** (`mode_tag=pair`): writes `TARGET\nCODE\n` to `reply.txt`
- **text mode** (default): writes `text\n` to `reply-last.txt`

---

## Discovery Layers

`adb-discover.sh` tries three methods in order:

1. **adb mdns** — `adb mdns services` (fast, but often empty in PRoot)
2. **App NsdManager** — Android app uses NsdManager to discover
   `_adb-tls-connect._tcp` or `_adb-tls-pairing._tcp`
3. **Localhost scan** — Python port scan on 127.0.0.1:35000-45000, then
   `adb pair` each to identify ADB ports

---

## NsdManager Caveats

- **Caching**: NsdManager caches discovered services. After pairing, the
  pairing service stays cached while the connect service may take 5-10s to
  appear. The script waits 5s and retries once.
- **Stale ports**: If the target device reboots or toggles Wireless Debugging
  off/on, the port changes. Discovery must be re-run.
- **Timeout**: NsdManager discovery latches timeout at 30s. The script mirrors
  this default.

---

## XSpaceManager Workaround

MIUI and some Chinese ROMs block `pm install` via XSpaceManager. The
`adb-parity-setup` command handles this:

- `adb-parity-setup apk` — builds and copies the APK to `/sdcard/`
- Manual install via file manager is the recommended fallback
- The APK targets SDK 28 for scoped storage compatibility, but uses
  MediaStore API on Android 10+ for writing to Documents/

---

## State Management

| File | Purpose |
|------|---------|
| `~/.adbparity/adbparity.log` | All script logs |
| `~/.adbparity/last-target.txt` | Last successful connect target |
| `/sdcard/Documents/adb-notify/` | App communication and debug |

State migration from old directories (`~/.adb-pair-notify/`,
`~/.adb-connect-notify/`) happens automatically on first run via `init_state()`.

---

## Exit Codes

Used by all scripts:

| Code | Meaning | Notes |
|------|---------|-------|
| 0 | Success | Connected or action completed |
| 1 | Error | Connection/pairing failure |
| 2 | Argument error | Invalid flag or missing value |
| 3 | App offline | MiniADBNotify not responding to ping |
| 4 | Timeout | Discovery or operation timed out |
