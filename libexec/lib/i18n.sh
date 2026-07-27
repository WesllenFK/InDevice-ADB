# shellcheck shell=bash
# i18n.sh — Bilingual (Portuguese/English) message system for ADB Parity
# Provides _i18n_msg and _i18n_msgf (used via common.sh's msg/msgf wrappers)

ADBPARITY_LANG="${ADBPARITY_LANG:-pt}"

declare -A _MSG
_MSG["pairing"]="pt:Pareando...|en:Pairing..."
_MSG["pair_ok"]="pt:Pareado com sucesso|en:Pair successful"
_MSG["pair_fail"]="pt:Falha ao parear|en:Pair failed"
_MSG["pair_timeout"]="pt:Tempo limite de pareamento excedido|en:Pairing timed out"
_MSG["connecting"]="pt:Conectando...|en:Connecting..."
_MSG["connect_ok"]="pt:Conectado com sucesso|en:Connect successful"
_MSG["connect_fail"]="pt:Falha ao conectar|en:Connect failed"
_MSG["app_online"]="pt:Aplicativo online|en:App online"
_MSG["app_offline"]="pt:Aplicativo offline|en:App offline"
_MSG["app_pinging"]="pt:Verificando aplicativo...|en:Pinging app..."
_MSG["discovering"]="pt:Descobrindo alvos...|en:Discovering targets..."
_MSG["discovered_none"]="pt:Nenhum alvo encontrado|en:No targets found"
_MSG["discovered_one"]="pt:1 alvo encontrado|en:1 target found"
_MSG["discovered_multi"]="pt:%d alvos encontrados|en:%d targets found"
_MSG["target_prompt"]="pt:Digite IP:PORTA do alvo|en:Enter target IP:PORT"
_MSG["code_prompt"]="pt:Digite o codigo de 6 digitos|en:Enter the 6-digit code"
_MSG["code_required"]="pt:Codigo de pareamento necessario|en:Pairing code required"
_MSG["reconnect_ok"]="pt:Reconectado com sucesso|en:Reconnect successful"
_MSG["reconnect_fail"]="pt:Falha ao reconectar|en:Reconnect failed"
_MSG["reconnect_no_target"]="pt:Nenhum alvo salvo para reconexao|en:No saved target to reconnect"
_MSG["success"]="pt:Sucesso|en:Success"
_MSG["failure"]="pt:Falha|en:Failure"
_MSG["error_timeout"]="pt:Tempo limite excedido|en:Timed out"
_MSG["error_app_offline"]="pt:Aplicativo MiniADBNotify offline. Instale o APK.|en:MiniADBNotify app offline. Install the APK."
_MSG["error_arg"]="pt:Argumento invalido: %s|en:Invalid argument: %s"
_MSG["cleaning_state"]="pt:Limpando estado anterior...|en:Cleaning previous state..."
_MSG["state_cleaned"]="pt:Estado limpo com sucesso|en:State cleaned successfully"
_MSG["install_done"]="pt:APK instalado com sucesso|en:APK installed successfully"
_MSG["install_help"]="pt:Use: adb-parity install <caminho-do-apk>|en:Usage: adb-parity install <path-to-apk>"
_MSG["uninstall_done"]="pt:Aplicativo desinstalado com sucesso|en:App uninstalled successfully"
_MSG["help_header"]="pt:Uso: %s [opcoes]\n  -h, --help        Ajuda\n  -v, --verbose     Log detalhado\n  -j, --json        Saida JSON\n  -l, --lang pt|en  Idioma\n  --no-notify       Sem notificacao\n  --timeout N       Timeout (s)\n  --connect-only    So conectar|en:Usage: %s [options]\n  -h, --help        Help\n  -v, --verbose     Verbose log\n  -j, --json        JSON output\n  -l, --lang pt|en  Language\n  --no-notify       No notification\n  --timeout N       Timeout (s)\n  --connect-only    Connect only"
_MSG["help_pair"]="pt:Uso: adb-pair [opcoes]\n  Pareamento ADB wireless completo.\n  --connect-only  So tenta conectar (sem parear)|en:Usage: adb-pair [options]\n  Full ADB wireless pairing flow.\n  --connect-only  Try connecting only (skip pairing)"
_MSG["help_connect"]="pt:Uso: adb-connect [opcoes]\n  Conexao ADB rapida com descoberta automatica.|en:Usage: adb-connect [options]\n  Quick ADB connect with auto-discovery."
_MSG["help_discover"]="pt:Uso: adb-discover [opcoes]\n  Descobre alvos ADB wireless.|en:Usage: adb-discover [options]\n  Discover ADB wireless targets."
_MSG["help_reconnect"]="pt:Uso: adb-reconnect [opcoes]\n  Reconecta ao ultimo alvo ADB salvo.|en:Usage: adb-reconnect [options]\n  Reconnect to last saved ADB target."

_i18n_msg() {
  local key="$1"
  local entry="${_MSG[$key]:-}"
  if [ -z "$entry" ]; then
    printf '???%s???' "$key"
    return
  fi
  local lang="${ADBPARITY_LANG:-pt}"
  local pattern="${entry#*"$lang:"}"
  pattern="${pattern%%|*}"
  printf '%s' "$pattern"
}

_i18n_msgf() {
  local key="$1"
  shift
  local entry="${_MSG[$key]:-}"
  if [ -z "$entry" ]; then
    printf '???%s???' "$key"
    return
  fi
  local lang="${ADBPARITY_LANG:-pt}"
  local pattern="${entry#*"$lang:"}"
  pattern="${pattern%%|*}"
  # shellcheck disable=SC2059
  printf "$pattern" "$@"
}
