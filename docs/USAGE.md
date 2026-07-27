# USAGE — ADB Parity Usage Guide

## Instalacao / Installation

O projeto e instalado via `adb-parity-setup install`:

```bash
adb-parity-setup install              # Instala no prefix padrao
adb-parity-setup install --prefix /data/data/com.termux/files/usr
adb-parity-setup install --lang en    # Idioma padrao ingles
adb-parity-setup uninstall            # Remove tudo
adb-parity-setup status               # Verifica instalacao
adb-parity-setup apk                  # Gere o APK
```

## First Run Checklist

1. Done a installacao
2. Instale o APK (`adb-notify/app/build/outputs/apk/debug/adb-notify_debug.apk`)
3. Abra o MiniADBNotify no Android (conceda permissoes)
4. Habilite "Depuracao sem Fio" no dispositivo alvo (Developer Options)
5. Execute `adb-pair`

---

## adb-pair — Full Flow

O comando principal. Executa 5 etapas automaticamente:

### Step 1: Ping app
Verifica se o MiniADBNotify esta recebendo broadcasts. Se falhar: exit code 3.

### Step 2: Try direct connect
Descobre alvos do tipo `connect` (ja pareados) via NsdManager. Se achar:
`adb connect` e pronto. Senao, continua.

### Step 3: Pair (unless --connect-only)
Descobre alvos do tipo `pairing` via NsdManager. O app Android mostra uma
notificacao para digitar o codigo de 6 digitos.

```bash
# So conectar, sem parear
adb-pair --connect-only
```

### Step 4: Connect after pairing
Apos parear, espera 5s (NsdManager cache) e tenta conectar.

### Step 5: Retry
Se a conexao falhar, faz uma segunda tentativa (NsdManager cache stale).

### Example output

```
Verificando aplicativo...
Aplicativo online
Descobrindo alvos...
1 alvo encontrado
Conectando...
Conectado com sucesso
```

### JSON mode

```bash
adb-pair --json
# {"status":"connected","target":"192.168.1.42:39815","mode":"direct","timestamp":"2026-07-26T12:00:00Z","duration":2.3}
```

---

## adb-connect vs adb-reconnect

### adb-connect

Quick connect. Descobre alvos do tipo `connect` e tenta conectar. Nao pareia.

```bash
adb-connect
adb-connect --lang en
adb-connect --json
```

### adb-reconnect

Reconecta ao ultimo alvo salvo (`~/.adbparity/last-target.txt`). Sem
descoberta de rede.

```bash
adb-reconnect
```

### When to use each

| Scenario | Command |
|----------|---------|
| First time pairing a device | `adb-pair` |
| Already paired, want to connect | `adb-connect` |
| Disconnected briefly, same target | `adb-reconnect` |
| Scripting/automation | `adb-pair --json` |

---

## Environment Variables

```bash
export ADBPARITY_HOME=/data/data/com.termux/files/usr/lib/adbparity
export ADBPARITY_LANG=en
adb-pair
```

- `ADBPARITY_HOME`: caminho onde os scripts estao instalados
- `ADBPARITY_LANG`: `pt` (padrao) ou `en`

---

## Manual APK Installation

Em dispositivos MIUI/MUI com XSpaceManager, o `pm install` pode ser bloqueado.
Use uma das alternativas:

### Method 1: File Manager
1. Copie o APK para o `/sdcard/`:
   ```bash
   cp adb-notify/app/build/outputs/apk/debug/adb-notify_debug.apk /sdcard/
   ```
2. Abra o Files app no Android, localize o APK e toque para instalar.

### Method 2: adb install from another machine
```bash
adb install adb-notify_debug.apk
```

### Method 3: Termux direct (if allowed)
```bash
adb-parity-setup install /sdcard/adb-notify_debug.apk
```

---

## FAQ

### Q: "app offline" mas o app esta instalado.
Abra o app manualmente pelo menos uma vez para conceder permissoes de
notificacao.

### Q: Nenhum alvo descoberto.
1. Verifique se ambos os dispositivos estao na mesma rede WiFi
2. Confirme que "Depuracao sem Fio" esta ativada (Developer Options)
3. Tente manualmente: `adb pair IP:PORTA`

### Q: O pareamento funciona mas a conexao falha.
Pode ser cache do NsdManager. O script ja tenta novamente apos 5s. Se
persistir, espere 10s e tente `adb-connect`.

### Q: Os scripts antigos (`adb-pair-notify.sh`) ainda funcionam?
Sim, mas serao removidos em versoes futuras. Migre para os novos comandos.

### Q: Como ver o log?
```bash
tail -f ~/.adbparity/adbparity.log
```
