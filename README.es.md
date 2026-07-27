# InDevice ADB ⋮ ADB Inalámbrico para Termux + PRoot

**🌐 Leer en:** [Português](README.pt.md) · [English](README.md) · [简体中文](README.zh-CN.md) · [Русский](README.ru.md)

---

## 📋 Descripción General

**InDevice ADB** gestiona todo el flujo de emparejamiento y conexión ADB
inalámbrico desde Termux: descubre dispositivos en la red mediante NsdManager,
solicita el código de 6 dígitos a través de una notificación de Android,
conecta, reconecta y maneja reintentos cuando la caché de NsdManager se
vuelve obsoleta.

La aplicación Android complementaria (`com.indevice.adb`) detecta la
configuración regional del dispositivo y ofrece su interfaz en **5 idiomas**
(pt, en, es, zh, ru). Los scripts siguen la misma configuración mediante
`--lang`.

---

## ⚡ Inicio Rápido

1. **Instale el APK** — copie `indevice-adb_debug.apk` a `/sdcard/` y ábralo en
   el dispositivo para instalarlo. Abra la aplicación una vez para conceder
   permisos de notificación.
2. **Instale los scripts** — `bash install.sh`
3. **Active la Depuración Inalámbrica** en el dispositivo objetivo (Opciones de
   Desarrollador)
4. **Ejecute:** `adb-pair`

---

## 🔧 Comandos

| Comando | Descripción |
|---------|-------------|
| `adb-pair` | Flujo completo: ping → descubrir → conectar (o emparejar → conectar) |
| `adb-connect` | Conexión rápida con descubrimiento automático |
| `adb-reconnect` | Reconecta al último objetivo guardado |
| `adb-discover` | Descubre objetivos (mDNS → NsdManager → escaneo local) |
| `adb-parity-setup` | Gestiona instalación: install \| uninstall \| status \| apk |

**Banderas comunes:** `--help`, `--verbose`, `--json`, `--lang pt|en|es|zh|ru`,
`--no-notify`, `--timeout N`, `--connect-only`

[👉 Documentación completa →](docs/USAGE.md)
