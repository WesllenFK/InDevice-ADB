# InDevice ADB ⋮ 适用于 Termux + PRoot 的无线 ADB 工具

**🌐 其他语言:** [Português](README.pt.md) · [English](README.md) · [Español](README.es.md) · [Русский](README.ru.md)

---

## 📋 概述

**InDevice ADB** 管理从 Termux 进行无线 ADB 配对和连接的全部流程：通过
NsdManager 发现网络中的设备，通过 Android 通知请求 6 位配对码，连接，
重连，并在 NsdManager 缓存过期时自动重试。

配套的 Android 应用（`com.indevice.adb`）可自动检测设备语言区域，提供
**5 种语言**的界面（pt、en、es、zh、ru）。Shell 脚本通过 `--lang` 参数
跟随相同的语言设置。

---

## ⚡ 快速开始

1. **安装 APK** — 将 `indevice-adb_debug.apk` 复制到 `/sdcard/`，然后在设备上
   打开安装。打开应用一次以授予通知权限。
2. **安装脚本** — `bash install.sh`
3. **在目标设备上启用无线调试**（开发者选项）
4. **运行：** `adb-pair`

---

## 🔧 命令

| 命令 | 说明 |
|------|------|
| `adb-pair` | 完整流程：ping → 发现 → 连接（或配对 → 连接） |
| `adb-connect` | 快速连接，自动发现 |
| `adb-reconnect` | 重新连接到上次保存的目标 |
| `adb-discover` | 发现目标（mDNS → NsdManager → 本地扫描） |
| `adb-parity-setup` | 管理安装：install \| uninstall \| status \| apk |

**常用参数：** `--help`, `--verbose`, `--json`, `--lang pt|en|es|zh|ru`,
`--no-notify`, `--timeout N`, `--connect-only`

[👉 完整文档 →](docs/USAGE.md)
