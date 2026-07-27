# InDevice ADB ⋮ 适用于 Termux + PRoot 的无线 ADB 工具

**🌐 其他语言:** [Português](README.pt.md) · [English](README.md) · [Español](README.es.md)

---

## 📋 概述

**ADB Parity** 管理从 Termux 进行无线 ADB 配对和连接的全部流程：通过
NsdManager 发现网络中的设备，通过 Android 通知请求 6 位配对码，连接，
重连，并在 NsdManager 缓存过期时自动重试。

---

## ⚡ 快速开始

1. **安装 APK** — 将 `adb-notify_debug.apk` 复制到 `/sdcard/`，然后在设备上
   打开安装。打开应用一次以授予通知权限。
2. **安装脚本** — `bash install.sh`
3. **在目标设备上启用无线调试**（开发者选项）
4. **运行：** `adb-pair`

[👉 完整文档 →](docs/USAGE.md)
