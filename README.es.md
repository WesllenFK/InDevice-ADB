# InDevice ADB ⋮ ADB Inalámbrico para Termux + PRoot

**🌐 Leer en:** [Português](README.pt.md) · [English](README.md) · [简体中文](README.zh-CN.md)

---

## 📋 Descripción General

**ADB Parity** gestiona todo el flujo de emparejamiento y conexión ADB
inalámbrico desde Termux: descubre dispositivos en la red mediante NsdManager,
solicita el código de 6 dígitos a través de una notificación de Android,
conecta, reconecta y maneja reintentos cuando la caché de NsdManager se
vuelve obsoleta.

---

## ⚡ Inicio Rápido

1. **Instale el APK** — copie `adb-notify_debug.apk` a `/sdcard/` y ábralo en
   el dispositivo para instalarlo. Abra la aplicación una vez para conceder
   permisos de notificación.
2. **Instale los scripts** — `bash install.sh`
3. **Active la Depuración Inalámbrica** en el dispositivo objetivo (Opciones de
   Desarrollador)
4. **Ejecute:** `adb-pair`

[👉 Documentación completa →](docs/USAGE.md)
