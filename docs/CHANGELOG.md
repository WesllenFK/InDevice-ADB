# Changelog

## v1.0.0 (2026-07-26)

- Unified `adb-pair` script with ping → connect → pair → connect flow
- Bilingual output (Portuguese/English) via i18n system
- JSON output mode (`--json`) for AI/scripting integration
- Refactored project structure: `bin/` (entry points) + `libexec/` (implementations)
- State migration to `~/.adbparity/` (replaces legacy `~/.adb-pair-notify/` and `~/.adb-connect-notify/`)
- `common.sh` shared library: logging, notifications, discovery, connect/pair functions
- `i18n.sh` bilingual message dictionary (pt + en)
- `adb-discover` 3-tier discovery: `adb mdns` → app NsdManager → localhost scan
- `adb-connect` quick connect with auto-discovery
- `adb-reconnect` reconnect to last saved target
- CLI argument parsing with `--help`, `--verbose`, `--json`, `--lang`, `--no-notify`, `--timeout`, `--connect-only`
- Standardized exit codes (0 success, 1 error, 2 args, 3 app offline, 4 timeout)
- Android app improvements:
  - `discover_and_pair` mode with NsdManager + RemoteInput notification
  - `discover_adb` mode for connect-type services
  - Scoped storage support via MediaStore API (Android 10+)
  - Debug logging to `/sdcard/Documents/adb-notify/debug/`
  - ReplyReceiver with pair mode and JSON output support
- Install/uninstall via `adb-parity-setup` command
- Manual APK install workaround for XSpaceManager (MIUI)
- Legacy cleanup: old scripts in `/root/adbparity/` preserved as stubs
