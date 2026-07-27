# shellcheck shell=bash
# i18n.sh — 5-language message system for InDevice ADB
# Provides _i18n_msg and _i18n_msgf (used via common.sh's msg/msgf wrappers)
# Languages: pt (Português), en (English), es (Español), zh (中文), ru (Русский)

ADBPARITY_LANG="${ADBPARITY_LANG:-en}"

declare -A _MSG
_MSG["pairing"]="pt:Pareando...|en:Pairing...|es:Emparejando...|zh:正在配对...|ru:Выполняется сопряжение..."
_MSG["pair_ok"]="pt:Pareado com sucesso|en:Pair successful|es:Emparejado exitosamente|zh:配对成功|ru:Сопряжение успешно"
_MSG["pair_fail"]="pt:Falha ao parear|en:Pair failed|es:Error al emparejar|zh:配对失败|ru:Сопряжение не удалось"
_MSG["pair_timeout"]="pt:Tempo limite de pareamento excedido|en:Pairing timed out|es:Tiempo de emparejamiento agotado|zh:配对超时|ru:Время сопряжения истекло"
_MSG["connecting"]="pt:Conectando...|en:Connecting...|es:Conectando...|zh:正在连接...|ru:Подключение..."
_MSG["connect_ok"]="pt:Conectado com sucesso|en:Connect successful|es:Conexión exitosa|zh:连接成功|ru:Подключение успешно"
_MSG["connect_fail"]="pt:Falha ao conectar|en:Connect failed|es:Error al conectar|zh:连接失败|ru:Подключение не удалось"
_MSG["app_online"]="pt:Aplicativo online|en:App online|es:App en línea|zh:应用在线|ru:Приложение в сети"
_MSG["app_offline"]="pt:Aplicativo offline|en:App offline|es:App fuera de línea|zh:应用离线|ru:Приложение не в сети"
_MSG["app_pinging"]="pt:Verificando aplicativo...|en:Pinging app...|es:Verificando app...|zh:正在检测应用...|ru:Проверка приложения..."
_MSG["discovering"]="pt:Descobrindo alvos...|en:Discovering targets...|es:Descubriendo objetivos...|zh:正在发现目标...|ru:Поиск устройств..."
_MSG["discovered_none"]="pt:Nenhum alvo encontrado|en:No targets found|es:Ningún objetivo encontrado|zh:未找到目标|ru:Устройства не найдены"
_MSG["discovered_one"]="pt:1 alvo encontrado|en:1 target found|es:1 objetivo encontrado|zh:找到1个目标|ru:1 устройство найдено"
_MSG["discovered_multi"]="pt:%d alvos encontrados|en:%d targets found|es:%d objetivos encontrados|zh:找到%d个目标|ru:Найдено %d устройств(а)"
_MSG["target_prompt"]="pt:Digite IP:PORTA do alvo|en:Enter target IP:PORT|es:Ingrese IP:PUERTO del objetivo|zh:输入目标 IP:端口|ru:Введите IP:ПОРТ устройства"
_MSG["code_prompt"]="pt:Digite o codigo de 6 digitos|en:Enter the 6-digit code|es:Ingrese el código de 6 dígitos|zh:输入6位代码|ru:Введите 6-значный код"
_MSG["code_required"]="pt:Codigo de pareamento necessario|en:Pairing code required|es:Código de emparejamiento requerido|zh:需要配对代码|ru:Требуется код сопряжения"
_MSG["reconnect_ok"]="pt:Reconectado com sucesso|en:Reconnect successful|es:Reconexión exitosa|zh:重新连接成功|ru:Повторное подключение успешно"
_MSG["reconnect_fail"]="pt:Falha ao reconectar|en:Reconnect failed|es:Error al reconectar|zh:重新连接失败|ru:Повторное подключение не удалось"
_MSG["reconnect_no_target"]="pt:Nenhum alvo salvo para reconexao|en:No saved target to reconnect|es:Sin objetivo guardado para reconectar|zh:没有已保存的目标可供重新连接|ru:Нет сохраненного устройства для подключения"
_MSG["success"]="pt:Sucesso|en:Success|es:Éxito|zh:成功|ru:Успешно"
_MSG["failure"]="pt:Falha|en:Failure|es:Fallo|zh:失败|ru:Ошибка"
_MSG["error_timeout"]="pt:Tempo limite excedido|en:Timed out|es:Tiempo agotado|zh:超时|ru:Время истекло"
_MSG["error_app_offline"]="pt:App InDevice ADB offline. Instale o APK.|en:InDevice ADB app offline. Install the APK.|es:App InDevice ADB fuera de línea. Instale el APK.|zh:InDevice ADB 应用离线。请安装 APK。|ru:Приложение InDevice ADB не в сети. Установите APK."
_MSG["error_arg"]="pt:Argumento invalido: %s|en:Invalid argument: %s|es:Argumento inválido: %s|zh:无效参数: %s|ru:Неверный аргумент: %s"
_MSG["cleaning_state"]="pt:Limpando estado anterior...|en:Cleaning previous state...|es:Limpiando estado anterior...|zh:正在清理之前的状态...|ru:Очистка предыдущего состояния..."
_MSG["state_cleaned"]="pt:Estado limpo com sucesso|en:State cleaned successfully|es:Estado limpiado exitosamente|zh:状态清理成功|ru:Состояние очищено успешно"
_MSG["install_done"]="pt:APK instalado com sucesso|en:APK installed successfully|es:APK instalado exitosamente|zh:APK安装成功|ru:APK установлен успешно"
_MSG["install_help"]="pt:Use: adb-parity install <caminho-do-apk>|en:Usage: adb-parity install <path-to-apk>|es:Uso: adb-parity install <ruta-del-apk>|zh:用法: adb-parity install <apk路径>|ru:Использование: adb-parity install <путь-к-apk>"
_MSG["uninstall_done"]="pt:Aplicativo desinstalado com sucesso|en:App uninstalled successfully|es:App desinstalada exitosamente|zh:应用卸载成功|ru:Приложение удалено успешно"
_MSG["help_header"]="pt:Uso: %s [opcoes]\n  -h, --help        Ajuda\n  -v, --verbose     Log detalhado\n  -j, --json        Saida JSON\n  -l, --lang pt|en|es|zh|ru  Idioma\n  --no-notify       Sem notificacao\n  --timeout N       Timeout (s)\n  --connect-only    So conectar|en:Usage: %s [options]\n  -h, --help        Help\n  -v, --verbose     Verbose log\n  -j, --json        JSON output\n  -l, --lang pt|en|es|zh|ru  Language\n  --no-notify       No notification\n  --timeout N       Timeout (s)\n  --connect-only    Connect only|es:Uso: %s [opciones]\n  -h, --help        Ayuda\n  -v, --verbose     Registro detallado\n  -j, --json        Salida JSON\n  -l, --lang pt|en|es|zh|ru  Idioma\n  --no-notify       Sin notificación\n  --timeout N       Tiempo de espera (s)\n  --connect-only    Solo conectar|zh:用法: %s [选项]\n  -h, --help        帮助\n  -v, --verbose     详细日志\n  -j, --json        JSON输出\n  -l, --lang pt|en|es|zh|ru  语言\n  --no-notify       无通知\n  --timeout N       超时（秒）\n  --connect-only    仅连接|ru:Использование: %s [опции]\n  -h, --help        Помощь\n  -v, --verbose     Подробный журнал\n  -j, --json        JSON-вывод\n  -l, --lang pt|en|es|zh|ru  Язык\n  --no-notify       Без уведомления\n  --timeout N       Таймаут (с)\n  --connect-only    Только подключение"
_MSG["help_pair"]="pt:Uso: adb-pair [opcoes]\n  Pareamento ADB wireless completo.\n  --connect-only  So tenta conectar (sem parear)|en:Usage: adb-pair [options]\n  Full ADB wireless pairing flow.\n  --connect-only  Try connecting only (skip pairing)|es:Uso: adb-pair [opciones]\n  Flujo completo de emparejamiento ADB inalámbrico.\n  --connect-only  Solo intentar conectar (sin emparejar)|zh:用法: adb-pair [选项]\n 完整的ADB无线配对流程。\n  --connect-only  仅尝试连接（跳过配对）|ru:Использование: adb-pair [опции]\n  Полный процесс беспроводного сопряжения ADB.\n  --connect-only  Только попытка подключения (без сопряжения)"
_MSG["help_connect"]="pt:Uso: adb-connect [opcoes]\n  Conexao ADB rapida com descoberta automatica.|en:Usage: adb-connect [options]\n  Quick ADB connect with auto-discovery.|es:Uso: adb-connect [opciones]\n  Conexión ADB rápida con descubrimiento automático.|zh:用法: adb-connect [选项]\n 快速ADB连接与自动发现。|ru:Использование: adb-connect [опции]\n  Быстрое подключение ADB с автообнаружением."
_MSG["help_discover"]="pt:Uso: adb-discover [opcoes]\n  Descobre alvos ADB wireless.|en:Usage: adb-discover [options]\n  Discover ADB wireless targets.|es:Uso: adb-discover [opciones]\n  Descubre objetivos ADB inalámbricos.|zh:用法: adb-discover [选项]\n 发现ADB无线目标。|ru:Использование: adb-discover [опции]\n  Обнаружение беспроводных устройств ADB."
_MSG["help_reconnect"]="pt:Uso: adb-reconnect [opcoes]\n  Reconecta ao ultimo alvo ADB salvo.|en:Usage: adb-reconnect [options]\n  Reconnect to last saved ADB target.|es:Uso: adb-reconnect [opciones]\n  Reconectar al último objetivo ADB guardado.|zh:用法: adb-reconnect [选项]\n 重新连接到上次保存的ADB目标。|ru:Использование: adb-reconnect [опции]\n  Подключиться к последнему сохраненному устройству ADB."
_MSG["write_error"]="pt:Erro de escrita no app: %s (sem permissao?)|en:App write error: %s (no permission?)|es:Error de escritura en la app: %s (¿sin permiso?)|zh:应用写入错误: %s（无权限？）|ru:Ошибка записи приложения: %s (нет разрешения?)"

_i18n_msg() {
  local key="$1"
  local entry="${_MSG[$key]:-}"
  if [ -z "$entry" ]; then
    printf '???%s???' "$key"
    return
  fi
  local lang="${ADBPARITY_LANG:-en}"
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
  local lang="${ADBPARITY_LANG:-en}"
  local pattern="${entry#*"$lang:"}"
  pattern="${pattern%%|*}"
  printf "$pattern" "$@"
}
