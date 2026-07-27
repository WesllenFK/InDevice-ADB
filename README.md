# InDevice ADB ⋮ Wireless ADB for Termux + PRoot

![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Termux-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

---

## 📋 O que é / What it is

**ADB Parity** gerencia todo o fluxo de pareamento e conexão ADB wireless
diretamente do Termux: descobre dispositivos na rede (NsdManager), solicita o
código de pareamento via notificação no Android, conecta e reconecta.

**ADB Parity** manages the full wireless ADB pairing and connection flow from
Termux: discovers devices on the network (NsdManager), requests the pairing code
through an Android notification, connects, and reconnects.

---

## ⚡ Quick Start / Comece Aqui

1. **Instale o APK** — copie `adb-notify_debug.apk` para o `/sdcard/` e instale
   manualmente (MIUI bloqueia `pm install`). Abra o app uma vez para conceder
   permissões.
2. **Instale os scripts** — `bash install.sh`
3. **Ative a Depuração sem Fio** no dispositivo alvo (Developer Options)
4. **Rode:** `adb-pair`

```
1. **Install the APK** — copy `adb-notify_debug.apk` to `/sdcard/` and install
   manually (MIUI blocks `pm install`). Open the app once to grant permissions.
2. **Install the scripts** — `bash install.sh`
3. **Enable Wireless Debugging** on the target device (Developer Options)
4. **Run:** `adb-pair`
```

---

## 🔧 Comandos / Commands

| Comando / Command | O que faz / What it does |
|-------------------|--------------------------|
| `adb-pair` | Fluxo completo: ping → descobrir → conectar (ou parear → conectar) |
| `adb-connect` | Conexão rápida com descoberta automática |
| `adb-reconnect` | Reconecta ao último alvo salvo |
| `adb-discover` | Descobre alvos (mDNS → NsdManager → scan local) |
| `adb-parity-setup` | Gerencia instalação: install \| uninstall \| status \| apk |

**Flags comuns / Common flags:** `--help`, `--verbose`, `--json`, `--lang pt|en`,
`--no-notify`, `--timeout N`, `--connect-only`

---

## 📦 MiniADBNotify App

App Android companion (`com.miniadbnotify`) que usa `NsdManager` para descobrir
serviços ADB wireless na rede e exibe notificações com campo de texto para o
código de pareamento. A comunicação script → app acontece via `am broadcast`;
app → script via arquivos no `/sdcard/Documents/adb-notify/`.

Companion Android app (`com.miniadbnotify`) that uses `NsdManager` to discover
ADB wireless services on the network and shows notifications with a text input
for the pairing code. Script → app communication uses `am broadcast`; app →
script uses files on `/sdcard/Documents/adb-notify/`.

📁 Código fonte / Source: [`app/`](app/)

---

## 🏗 Arquitetura / Architecture

```
Termux (PRoot) ──am broadcast──► MiniADBNotify (Android)
     │                                  │
     │ adb pair / connect               │ NsdManager / Notificações
     ▼                                  ▼
  Dispositivo alvo ◄── TCP WiFi ── Depuração sem Fio
```

🔧 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — funções do common.sh, protocolo
app↔script, NsdManager caveats, XSpaceManager workaround

---

## 🚧 Problemas Comuns / Common Issues

| Problema / Issue | Solução / Fix |
|------------------|---------------|
| "app offline" | Abra o MiniADBNotify manualmente uma vez para conceder permissões |
| Nenhum alvo descoberto | Verifique WiFi e Depuração sem Fio; tente `adb-discover` |
| Pareamento OK, conexão falha | Espere 10s e tente `adb-connect` (cache do NsdManager) |
| `pm install` bloqueado | Instale o APK manualmente pelo gerenciador de arquivos |

📘 [docs/USAGE.md#FAQ](docs/USAGE.md#FAQ) — perguntas frequentes detalhadas

---

## 📖 Documentação / Documentation

| Documento | Conteúdo |
|-----------|----------|
| 📘 [USAGE.md](docs/USAGE.md) | Instalação completa, guia de comandos, JSON mode, FAQ |
| 🏗️ [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Arquitetura técnica, funções, protocolo app↔script |
| 📋 [CHANGELOG.md](docs/CHANGELOG.md) | Histórico de versões |

---

## 📄 Licença / License

MIT © 2026 WesllenFK
