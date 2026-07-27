#!/data/data/com.termux/files/usr/bin/bash
# uninstall.sh — Remove ADB Parity scripts
set -euo pipefail

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
ADBPARITY_HOME="${ADBPARITY_HOME:-$PREFIX/lib/adbparity}"
ADBPARITY_LANG="${ADBPARITY_LANG:-pt}"
FORCE=false

_help() {
  cat <<'EOF'
Usage: uninstall.sh [options]

Options:
  --prefix DIR       Uninstall from custom prefix (default: /data/data/com.termux/files/usr)
  --force, -f        Skip all confirmations
  --help             Show this help
EOF
}

_e() {
  if [ "$ADBPARITY_LANG" = "en" ]; then printf '%s' "$2"; else printf '%s' "$1"; fi
}

_confirm() {
  local prompt="$1"
  if [ "$FORCE" = "true" ]; then return 0; fi
  printf '%s [y/N] ' "$prompt"
  read -r ans
  case "$ans" in y|Y|s|S) return 0 ;; *) return 1 ;; esac
}

while [ $# -gt 0 ]; do
  case "$1" in
    --prefix) PREFIX="$2"; shift 2 ;;
    --force|-f) FORCE=true; shift ;;
    --help|-h) _help; exit 0 ;;
    *) printf 'Unknown option: %s\n' "$1" >&2; exit 2 ;;
  esac
done

printf '%s\n' "$(_e "Desinstalando ADB Parity..." "Uninstalling ADB Parity...")"

if [ -d "$ADBPARITY_HOME" ]; then
  printf '%s...\n' "$(_e "Removendo scripts" "Removing scripts")"
  rm -rf "$ADBPARITY_HOME/libexec" 2>/dev/null || true
  rm -f "$ADBPARITY_HOME/install.sh" "$ADBPARITY_HOME/uninstall.sh" 2>/dev/null || true
  rm -f "$ADBPARITY_HOME/adb-notify_debug.apk" 2>/dev/null || true
  rmdir "$ADBPARITY_HOME" 2>/dev/null || true
fi

printf '%s...\n' "$(_e "Removendo comandos" "Removing commands")"
for f in adb-pair adb-connect adb-reconnect adb-discover adb-parity-setup; do
  [ -f "$PREFIX/bin/$f" ] && rm -f "$PREFIX/bin/$f"
done

if [ -d "$HOME/.adbparity" ]; then
  if _confirm "$(_e "Remover diretório de estado ~/.adbparity?" "Remove state directory ~/.adbparity?")"; then
    rm -rf "$HOME/.adbparity"
    printf '%s\n' "$(_e "Diretório de estado removido." "State directory removed.")"
  fi
fi

BASHRC="$HOME/.bashrc"
if [ -f "$BASHRC" ] && (grep -q 'ADBPARITY_HOME' "$BASHRC" 2>/dev/null || grep -q 'ADBPARITY_LANG' "$BASHRC" 2>/dev/null); then
  TMP="$(mktemp)"
  grep -v '^export ADBPARITY_HOME=' "$BASHRC" | grep -v '^export ADBPARITY_LANG=' > "$TMP"
  mv "$TMP" "$BASHRC"
  printf '%s\n' "$(_e "Variáveis de ambiente removidas do .bashrc." "Environment variables removed from .bashrc.")"
fi

if _confirm "$(_e "Desinstalar o aplicativo Android com.miniadbnotify?" "Uninstall the Android app com.miniadbnotify?")"; then
  /system/bin/pm uninstall com.miniadbnotify 2>/dev/null && \
    printf '%s\n' "$(_e "Aplicativo desinstalado." "App uninstalled.")" || \
    printf '%s\n' "$(_e "Aplicativo não encontrado ou não foi possível desinstalar." "App not found or could not be uninstalled.")"
fi

printf '\n%s\n' "$(_e "ADB Parity desinstalado." "ADB Parity uninstalled.")"
