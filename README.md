# InDevice ADB ⋮ Wireless ADB for Termux + PRoot

![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Termux-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

**🌐 Read in:** [Português](README.pt.md) · [Español](README.es.md) · [简体中文](README.zh-CN.md)

---

## 📋 Overview

**ADB Parity** manages the full wireless ADB pairing and connection flow from
Termux: discovers devices on the network via NsdManager, requests the 6-digit
pairing code through an Android notification, connects, reconnects, and handles
discovery retries when the NsdManager cache goes stale.

---

## ⚡ Quick Start

1. **Install the APK** — copy `adb-notify_debug.apk` to `/sdcard/` and open it
   on your device to install. Open the app once to grant notification permissions.
2. **Install the scripts** — `bash install.sh`
3. **Enable Wireless Debugging** on the target device (Developer Options)
4. **Run:** `adb-pair`

That's it. The tool pings the app, discovers targets on the network, and
either connects directly or guides you through pairing.

---

## 🔧 Commands

| Command | Description |
|---------|-------------|
| `adb-pair` | Full flow: ping → discover → connect (or pair → connect) |
| `adb-connect` | Quick connect with auto-discovery |
| `adb-reconnect` | Reconnect to the last saved target |
| `adb-discover` | Discover targets (mDNS → NsdManager → localhost scan) |
| `adb-parity-setup` | Manage installation: install \| uninstall \| status \| apk |

**Common flags:** `--help`, `--verbose`, `--json`, `--lang pt|en`,
`--no-notify`, `--timeout N`, `--connect-only`

---

## 📦 MiniADBNotify App

Companion Android app (`com.miniadbnotify`) that uses `NsdManager` to discover
ADB wireless services on the local network and shows expandable notifications
with a text input field for the pairing code.

Script → app communication happens via `am broadcast`; app → script replies
land as files on `/sdcard/Documents/adb-notify/`.

📁 Source: [`app/`](app/)

---

## 🏗 Architecture

```
Termux (PRoot) ──am broadcast──► MiniADBNotify (Android)
     │                                  │
     │ adb pair / connect               │ NsdManager / Notifications
     ▼                                  ▼
  Target device ◄── TCP WiFi ── Wireless Debugging
```

🔧 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — common.sh internals, app↔script
protocol, NsdManager caveats, XSpaceManager workaround

---

## 🚧 Common Issues

| Issue | Fix |
|-------|-----|
| "app offline" | Open MiniADBNotify manually once to grant notification permissions |
| No targets found | Check WiFi and Wireless Debugging on the target device |
| Pairing OK, connect fails | Wait 10s and retry with `adb-connect` (stale NsdManager cache) |
| APK won't install from CLI | Copy to `/sdcard/` and install via file manager |

📘 [docs/USAGE.md#FAQ](docs/USAGE.md#FAQ) — detailed FAQ

---

## 📖 Documentation

| Doc | Contents |
|-----|----------|
| 📘 [USAGE.md](docs/USAGE.md) | Full installation, command guide, JSON mode, FAQ |
| 🏗️ [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Technical architecture, functions, app↔script protocol |
| 📋 [CHANGELOG.md](docs/CHANGELOG.md) | Version history |

---

## 📄 License

MIT © 2026 WesllenFK
