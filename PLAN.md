# PLAN — ADB Parity: Produto Sólido, Escalável e Amigável

## Goal
Reorganizar `adbparity` em um produto bem estruturado, user-friendly e AI-friendly
para Termux + PRoot. Scripts organizados, app Android estável, instalação simples.

---

## Fase 1 — Estrutura de Diretórios e CLI

### 1.1 Novo layout do projeto

```
/root/adbparity/
├── bin/                    # Entry points (PATH)
│   ├── adb-pair            # wrapper → libexec/adb-pair.sh
│   ├── adb-connect         # wrapper → libexec/adb-connect.sh
│   └── adb-reconnect       # wrapper → libexec/adb-reconnect.sh
├── libexec/                # Scripts internos
│   ├── adb-pair.sh         # Main pair flow (unified)
│   ├── adb-connect.sh      # Connect-only flow
│   ├── adb-reconnect.sh    # Reconnect with last target
│   ├── adb-discover.sh     # Discovery (mdns → app → scan)
│   ├── adb-notify.sh       # Send notification via broadcast
│   └── lib/
│       ├── common.sh       # log(), notify(), i18n, utils
│       ├── discover.sh     # Discovery functions
│       ├── connect.sh      # ADB connect/pair functions
│       └── i18n.sh         # Portuguese/English messages
├── app/                    # Android Studio project (adb-notify)
│   └── ... (existing)
├── state/                  # Runtime state dir (~/.adbparity)
├── docs/
│   ├── README.md
│   ├── USAGE.md
│   └── ARCHITECTURE.md
├── install.sh              # Install/link scripts
├── uninstall.sh            # Remove all traces
└── PLAN.md                 # (this file)
```

**State dir**: `~/.adbparity/` (unified, replaces `~/.adb-pair-notify/` + `~/.adb-connect-notify/`)

### 1.2 CLI padrão

```
adb-pair                  # Flow completo (ping → connect → pair → connect)
adb-pair --help           # Ajuda
adb-pair --verbose        # Modo detalhado
adb-pair --json           # Output JSON (AI-friendly)
adb-pair --lang en        # Forçar inglês
adb-pair --no-notify      # Sem notificação (só terminal)

adb-connect               # Conectar a alvo (com descoberta)
adb-reconnect             # Reconectar ao último alvo
```

### 1.3 common.sh — lib compartilhada

- `log()` — loga com timestamp + nível (INFO/WARN/ERROR/DEBUG)
- `notify()` — envia broadcast para o app
- `notify_clear()` — remove notificação por ID
- `die()` — erro + saída + notificação
- `msg()` — output i18n bilingual
- `discover_connect()` — descobre alvo connect
- `discover_pair()` — descobre alvo pair + solicita código
- `do_connect()` — adb connect + verificação
- `do_pair()` — adb pair + verificação
- `ping_app()` — verifica se app está online

### 1.4 i18n.sh — mensagens bilingues

```bash
msg_pair_start="pt:Pareando...|en:Pairing..."
msg_pair_ok="pt:Pareado com sucesso|en:Pair successful"
msg_pair_fail="pt:Falha no pareamento|en:Pair failed"
```

Função `msg()` lê `ADBPARITY_LANG` (pt|en), default pt.

### 1.5 Migração de estado

- Na primeira execução, migra `~/.adb-pair-notify/` e `~/.adb-connect-notify/` para `~/.adbparity/`
- Scripts antigos viram stubs que apontam para o novo

---

## Fase 2 — Script Unificado (adb-pair.sh)

### 2.1 Fluxo consolidado

Baseado no `adb-pair-notify.sh` (171 linhas, $HOME):

```
1. ping_app()          → verifica app online
2. discover_connect()  → tenta descobrir alvo connect
3a. Se achou → do_connect() → se OK, fim
3b. Se falhou → discover_pair() → aguarda código → do_pair()
4. Se pareou → sleep 5 → discover_connect() → do_connect()
5. Se falhou → retry 1x (NsdManager cache stale)
6. Notificação final (sucesso/falha)
```

### 2.2 Argumentos

```
adb-pair [opções]
  --help, -h        Mostra ajuda
  --verbose, -v     Log detalhado no terminal
  --json, -j        Output JSON ao final
  --lang, -l pt|en  Idioma (default: pt)
  --no-notify       Sem notificação Android
  --timeout N       Timeout para descoberta (default 30s)
  --connect-only    Só tenta conectar, não pareia
```

### 2.3 JSON output mode

```json
{
  "status": "connected",
  "target": "192.168.1.100:37003",
  "mode": "direct",
  "timestamp": 1712345678,
  "duration": 3.2,
  "app_online": true
}
```

---

## Fase 3 — App Android

### 3.1 Melhorias

- Modo `discover_and_pair` atual: usar `RemoteInput.Input` em vez de `RemoteInput.Builder` deprecated em API 34+
- Adicionar activity de configuração (timeout, idioma) via SharedPreferences
- BroadcastReceiver responder com resultado mais detalhado (JSON no reply)
- Tratar notificação de sistema para pareamento automático (sem código manual) — API 35+

### 3.2 Manter retrocompatibilidade

- `writeExternalFile()` API 29+ via MediaStore continua
- `discover_adb` e `discover_and_pair` preservam contratos existentes
- Adicionar `EXTRA_LANG` para notificações em inglês

---

## Fase 4 — Instalação

### 4.1 install.sh

```bash
./install.sh
  --prefix DIR   (default: /data/data/com.termux/files/usr)
  --lang pt|en   (default: pt)
  --no-alias     (não criar aliases no .bashrc)
```

O que faz:
1. Copia `bin/` para `$PREFIX/bin/`
2. Copia `libexec/` para `$PREFIX/lib/adbparity/`
3. Cria state dir `~/.adbparity/`
4. Adiciona `export ADBPARITY_HOME=$PREFIX/lib/adbparity` no `.bashrc`
5. Opcional: cria alias `adb-pair`, `adb-connect`, `adb-reconnect`

### 4.2 uninstall.sh

Remove todos os arquivos, state dir, e limpa `.bashrc`.

---

## Fase 5 — Documentação e AI-Friendliness

### 5.1 Documentos

- `docs/README.md` — visão geral, instalação, uso rápido (bilingue)
- `docs/USAGE.md` — comandos detalhados, exemplos, troubleshooting
- `docs/ARCHITECTURE.md` — diagrama de fluxo, contrato app↔script, estrutura de arquivos

### 5.2 AI-friendly

- Todos os scripts com shebang e `set -euo pipefail`
- Funções documentadas com comentários de uso
- JSON output mode (`--json`) para parsing por LLMs e ferramentas
- Exit codes padronizados:
  - 0: sucesso
  - 1: erro de conexão/pareamento
  - 2: erro de argumento
  - 3: app offline
  - 4: timeout
- Mensagens de erro estruturadas: `[ERROR] código: mensagem`

### 5.3 Man page (opcional)

- `adb-pair.1`, `adb-connect.1` — man pages para sistemas Unix

---

## Fase 6 — Limpeza

### 6.1 Scripts legados a remover

- `/root/adbparity/adb-pair-notify-status.sh` (wrapper)
- `/root/adbparity/adb-pair-notify-clean.sh` (wrapper)
- `/root/adbparity/adb-connect-notify.sh` (separado)
- `/root/adbparity/adb-connect-notify-status.sh` (wrapper)
- `/root/adbparity/adb-connect-notify-clean.sh` (wrapper)
- `/root/adbparity/adb-discover-target.sh` (substituído por `libexec/adb-discover.sh`)
- `/root/adbparity/adb-notify-send.sh` (wrapper)
- `/root/adbparity/adb-notify-clear.sh` (wrapper)
- `/root/adbparity/adb-reconnect.sh` (wrapper)

### 6.2 Manter como stubs (opcional)

Stubs em `/root/adbparity/` que chamam `adb-pair --connect-only` etc.

---

## Ordens de Execução

| Wave | Fases | Descrição |
|------|-------|-----------|
| 1    | 1.1–1.5 | Estrutura, common.sh, i18n.sh, CLI |
| 2    | 2.1–2.2 | Script unificado adb-pair.sh |
| 3    | 3.1–3.2 | App Android (melhorias) |
| 4    | 4.1–4.2 | install.sh / uninstall.sh |
| 5    | 5.1–5.3 | Documentação |
| 6    | 6.1–6.2 | Limpeza de legados |
