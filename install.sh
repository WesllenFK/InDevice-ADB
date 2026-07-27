#!/data/data/com.termux/files/usr/bin/bash
# install.sh — Install ADB Parity scripts
set -euo pipefail

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
ADBPARITY_HOME="${ADBPARITY_HOME:-$PREFIX/lib/adbparity}"
ADBPARITY_LANG="${ADBPARITY_LANG:-pt}"
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
INSTALL_LINKS=true
INSTALL_APK=false

_help() {
  cat <<'EOF'
Usage: install.sh [options]

Options:
  --prefix DIR       Install to custom prefix (default: /data/data/com.termux/files/usr)
  --lang pt|en       Default language (default: pt)
  --no-links         Don't install commands in PREFIX/bin
  --install-apk      Also copy APK to /sdcard for manual install
  --help             Show this help
EOF
}

_e() {
  if [ "$ADBPARITY_LANG" = "en" ]; then printf '%s' "$2"; else printf '%s' "$1"; fi
}

while [ $# -gt 0 ]; do
  case "$1" in
    --prefix) PREFIX="$2"; shift 2 ;;
    --lang) ADBPARITY_LANG="$2"; shift 2 ;;
    --no-links) INSTALL_LINKS=false; shift ;;
    --install-apk) INSTALL_APK=true; shift ;;
    --help|-h) _help; exit 0 ;;
    *) printf 'Unknown option: %s\n' "$1" >&2; exit 2 ;;
  esac
done

if [ ! -d "$PROJECT_ROOT/libexec" ]; then
  printf '%s\n' "$(_e "Erro: install.sh deve ser executado da raiz do projeto ADB Parity." "Error: install.sh must be run from the ADB Parity project root.")" >&2
  exit 1
fi

printf '%s...\n' "$(_e "Copiando scripts" "Copying scripts")"
mkdir -p "$ADBPARITY_HOME/libexec/lib"
cp -r "$PROJECT_ROOT/libexec/"* "$ADBPARITY_HOME/libexec/"
chmod +x "$ADBPARITY_HOME/libexec/"*.sh 2>/dev/null || true
chmod +x "$ADBPARITY_HOME/libexec/lib/"*.sh 2>/dev/null || true

cp "$PROJECT_ROOT/install.sh" "$ADBPARITY_HOME/"
if [ -f "$PROJECT_ROOT/uninstall.sh" ]; then
  cp "$PROJECT_ROOT/uninstall.sh" "$ADBPARITY_HOME/"
fi

if [ "$INSTALL_LINKS" = "true" ]; then
  printf '%s...\n' "$(_e "Instalando comandos" "Installing commands")"
  for f in "$PROJECT_ROOT/bin/adb-"*; do
    [ -f "$f" ] || continue
    cp "$f" "$PREFIX/bin/$(basename "$f")"
    chmod +x "$PREFIX/bin/$(basename "$f")"
  done
fi

mkdir -p "$HOME/.adbparity"

BASHRC="$HOME/.bashrc"
touch "$BASHRC"
for entry in "ADBPARITY_HOME=$ADBPARITY_HOME" "ADBPARITY_LANG=$ADBPARITY_LANG"; do
  var="${entry%%=*}"
  if ! grep -q "export $var=" "$BASHRC" 2>/dev/null; then
    echo "export $entry" >> "$BASHRC"
  fi
done

if [ "$INSTALL_APK" = "true" ]; then
  APK_SRC=""
  if [ -f "$PROJECT_ROOT/adb-notify_debug.apk" ]; then
    APK_SRC="$PROJECT_ROOT/adb-notify_debug.apk"
  elif [ -d "$PROJECT_ROOT/app" ]; then
    APK_SRC="$(find "$PROJECT_ROOT/app" -name '*.apk' 2>/dev/null | head -1)" || true
  fi
  if [ -n "$APK_SRC" ] && [ -f "$APK_SRC" ]; then
    cp "$APK_SRC" /sdcard/adb-notify.apk
    printf '\n%s\n' "$(_e "APK copiado para /sdcard/adb-notify.apk" "APK copied to /sdcard/adb-notify.apk")"
    printf '%s\n' "$(_e "Instale manualmente:" "Install manually:")"
    printf '  pm install /sdcard/adb-notify.apk\n'
  else
    printf '\n%s\n' "$(_e "APK não encontrado." "APK not found.")"
  fi
fi

printf '\n'
printf '%s %s\n' "$(_e "ADB Parity instalado em:" "ADB Parity installed at:")" "$ADBPARITY_HOME"
[ "$INSTALL_LINKS" = "true" ] && printf '%s %s\n' "$(_e "  Comandos:" "  Commands:")" "$PREFIX/bin/adb-*"
printf '%s %s\n' "$(_e "  Scripts:" "  Scripts:")" "$ADBPARITY_HOME/libexec/"
printf '%s %s\n' "$(_e "  Estado:" "  State:")" "$HOME/.adbparity"
printf '%s\n' "$(_e "Recarregue o shell: source ~/.bashrc" "Reload shell: source ~/.bashrc")"
