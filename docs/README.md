# ADB Parity

**Wireless ADB pairing and connection for Termux + PRoot on Android.**

ADB Parity gerencia todo o fluxo de pareamento e conexao ADB wireless: descobre
dispositivos na rede (via mDNS e NsdManager), solicita codigos de pareamento via
notificacoes, conecta e gerencia estado. Funciona em Termux com PRoot.

ADB Parity handles the full wireless ADB pairing and connection flow: discovers
devices on the network (via mDNS and NsdManager), requests pairing codes through
notifications, connects, and manages state. Runs in Termux under PRoot.

---

## Quick Start

1. **Install** the scripts (see [USAGE.md](USAGE.md))
2. **Install and open** the MiniADBNotify app (`com.miniadbnotify`)
3. **Enable Wireless Debugging** on the target Android device
4. **Run:**

```bash
adb-pair
```

The tool will ping the app, discover targets, and either connect directly or
guide you through pairing.

---

## Commands

| Command | Description |
|---------|-------------|
| `adb-pair` | Full flow: ping app, discover, connect (or pair then connect) |
| `adb-connect` | Quick connect with auto-discovery |
| `adb-reconnect` | Reconnect to last saved target |
| `adb-discover` | Discover targets (mDNS, app NsdManager, localhost scan) |
| `adb-parity-setup` | Project management: install, uninstall, status, apk |

### Common flags

| Flag | Effect |
|------|--------|
| `--help`, `-h` | Show help |
| `--verbose`, `-v` | Detailed logging |
| `--json`, `-j` | JSON output for scripting |
| `--lang pt|en` | Language (default: pt) |
| `--no-notify` | Skip Android notifications |
| `--timeout N` | Discovery timeout in seconds |

---

## Requirements

- **Termux** with **PRoot** (tested on Android 10+)
- **MiniADBNotify** app installed (`com.miniadbnotify`)
- **Wireless Debugging** enabled on target device
- `adb` available (built into Termux or via `pkg install android-tools`)

---

## Example Workflow

```bash
# Full automatic flow
adb-pair

# Just connect (skip pairing)
adb-pair --connect-only

# Quick connect to discovered target
adb-connect

# Reconnect (no discovery, uses saved target)
adb-reconnect

# JSON output for scripting
adb-pair --json --lang en
```

---

## Troubleshooting

| Problem | Likely fix |
|---------|------------|
| App offline error | Install the APK and open the app once |
| Pairing code not showing | Check notification permissions for MiniADBNotify |
| No targets discovered | Verify Wireless Debugging is on and devices are on same network |
| `pm install` blocked | Use manual APK install via file manager (MIUI XSpaceManager) |
| Connection lost | Run `adb-reconnect` to reconnect to the last target |

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Connection/pairing error |
| 2 | Argument error |
| 3 | App offline |
| 4 | Timeout |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ADBPARITY_HOME` | `$PREFIX/lib/adbparity` | Scripts installation path |
| `ADBPARITY_LANG` | `pt` | Language: `pt` or `en` |
