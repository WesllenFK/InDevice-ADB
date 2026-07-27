# InDevice ADB ⋮ ADB Wireless para Termux + PRoot

**🇺🇸 Leia em:** [English](README.md) · [Español](README.es.md) · [简体中文](README.zh-CN.md) · [Русский](README.ru.md)

---

## 📋 Visão Geral

**InDevice ADB** gerencia todo o fluxo de pareamento e conexão ADB wireless
diretamente do Termux: descobre dispositivos na rede via NsdManager, solicita o
código de 6 dígitos através de uma notificação Android, conecta, reconecta e
lida com retentativas quando o cache do NsdManager fica obsoleto.

O aplicativo Android complementar (`com.indevice.adb`) detecta o locale do
dispositivo e oferece sua interface em **5 idiomas** (pt, en, es, zh, ru). Os
scripts seguem a mesma configuração de idioma via `--lang`.

---

## ⚡ Comece Aqui

1. **Instale o APK** — copie `indevice-adb_debug.apk` para o `/sdcard/` e abra no
   dispositivo para instalar. Abra o app uma vez para conceder permissões de
   notificação.
2. **Instale os scripts** — `bash install.sh`
3. **Ative a Depuração sem Fio** no dispositivo alvo (Opções do Desenvolvedor)
4. **Execute:** `adb-pair`

Pronto. A ferramenta verifica o app, descobre alvos na rede e conecta ou guia
você pelo pareamento.

---

## 🔧 Comandos

| Comando | Descrição |
|---------|-----------|
| `adb-pair` | Fluxo completo: ping → descobrir → conectar (ou parear → conectar) |
| `adb-connect` | Conexão rápida com descoberta automática |
| `adb-reconnect` | Reconecta ao último alvo salvo |
| `adb-discover` | Descobre alvos (mDNS → NsdManager → scan local) |
| `adb-parity-setup` | Gerencia instalação: install \| uninstall \| status \| apk |

**Flags comuns:** `--help`, `--verbose`, `--json`, `--lang pt|en|es|zh|ru`,
`--no-notify`, `--timeout N`, `--connect-only`

[👉 Documentação completa →](docs/USAGE.md)
