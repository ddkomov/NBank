#!/bin/bash

# === Цвета ===
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# === Функции логирования ===
log() { echo -e "${GREEN}✅ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠️  $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; }

# === Определение ОС ===
detect_os() {
    case "${OSTYPE}" in
        "linux-gnu"*)
            if grep -qi microsoft /proc/version &>/dev/null; then
                echo "wsl"
            else
                echo "linux"
            fi
            ;;
        "msys"*)
            if command -v wslpath &> /dev/null; then
                echo "wsl"
            else
                echo "git-bash"
            fi
            ;;
        "darwin"*)
            echo "macos"
            ;;
        *)
            echo "unknown"
            ;;
    esac
}

OS=$(detect_os)

# === Проверка и установка jq (если не установлен) ===
if ! command -v jq &> /dev/null; then
    echo "jq не найден. Попробуем установить..."

    case "$OS" in
        "linux"|"wsl")
            if command -v apt-get &> /dev/null; then
                warn "Установка jq через apt-get..."
                sudo apt-get update && sudo apt-get install -y jq
            elif command -v yum &> /dev/null; then
                warn "Установка jq через yum..."
                sudo yum install -y jq
            else
                error "Не удалось найти пакетный менеджер. Установите jq вручную: https://stedolan.github.io/jq/download/"
                exit 1
            fi
            ;;
        "macos")
            if command -v brew &> /dev/null; then
                warn "Установка jq через Homebrew..."
                brew install jq
            else
                error "Homebrew не установлен. Установите: https://brew.sh"
                exit 1
            fi
            ;;
        "git-bash")
            error "Вы используете Git Bash. jq не установлен."
            error "Рекомендуется:"
            error "  1. Установить: choco install jq"
            error "  2. Или перейти в WSL: wsl ./run-tests-with-docker-compose.sh"
            error "  3. Скачать: https://stedolan.github.io/jq/download/"
            exit 1
            ;;
        *)
            error "Неподдерживаемая ОС: $OSTYPE"
            exit 1
            ;;
    esac

    if ! command -v jq &> /dev/null; then
        error "Не удалось установить jq."
        exit 1
    fi
    log "jq установлен"
else
    log "jq уже установлен"
fi

# === Проверка docker и docker-compose ===
if ! command -v docker &> /dev/null; then
    error "Docker не установлен. Установите: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker compose &> /dev/null && ! command -v docker-compose &> /dev/null; then
    error "Docker Compose не установлен."
    exit 1
fi

# Используем правильную команду compose
if command -v docker-compose &> /dev/null; then
    COMPOSE="docker-compose"
else
    COMPOSE="docker compose"
fi

# === Пути ===
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)

# Простой Unix-путь, который Git Bash и Docker понимают
TEST_OUTPUT_DIR="$SCRIPT_DIR/test-output/$(date +%Y%m%d_%H%M%S)"

# Подпапки — только для создания
TEST_REPORT_DIR="$TEST_OUTPUT_DIR/report"
TEST_LOGS_DIR="$TEST_OUTPUT_DIR/logs"

CONFIG_DIR="$SCRIPT_DIR/config"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
BROWSER_CONFIG="$CONFIG_DIR/browsers.json"

# === Проверка файлов ===
if [ ! -f "$BROWSER_CONFIG" ]; then
    error "Файл browsers.json не найден: $BROWSER_CONFIG"
    exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
    error "Файл docker-compose.yml не найден: $COMPOSE_FILE"
    exit 1
fi

log "🔍 Текущая ОС: $OS"
log "📁 SCRIPT_DIR: $SCRIPT_DIR"
log "📁 Результаты будут сохранены в: $TEST_OUTPUT_DIR"

# === Подготовка окружения ===
log "Подготовка тестового окружения..."

# Останавливаем старые контейнеры
echo ">>> Остановка существующих контейнеров..."
$COMPOSE -f "$COMPOSE_FILE" down --remove-orphans || warn "Контейнеры не были запущены"

# Поднимаем Docker Compose
log "Запуск Docker Compose (Selenoid + app)..."
$COMPOSE -f "$COMPOSE_FILE" up -d

# === Ожидание готовности backend и nginx ===
log "Ожидание готовности backend (http://localhost:4111/actuator/health)..."
for i in {1..30}; do
    if curl -s http://localhost:4111/actuator/health | grep -q '"status":"UP"'; then
        log "Backend готов"
        break
    fi
    sleep 2
done

log "Ожидание готовности nginx (http://localhost/)..."
for i in {1..10}; do
    if curl -s -f http://localhost/ &>/dev/null; then
        log "Nginx готов"
        break
    fi
    sleep 2
done

# === Ожидание готовности Selenoid ===
log "Ожидание запуска Selenoid (http://localhost:4444/status)..."
for i in {1..30}; do
    if curl -s http://localhost:4444/status | jq -e '.selenoidVersion' >/dev/null 2>&1; then
        log "Selenoid API доступен"
        break
    fi
    sleep 2
done

# === Проверка, что Chrome доступен ===
log "Проверка доступности Chrome в Selenoid..."
if ! curl -s http://localhost:4444/status | jq -e '.browsers.chrome' >/dev/null 2>&1; then
    error "Chrome не найден в Selenoid. Проверьте config/browsers.json"
    $COMPOSE -f "$COMPOSE_FILE" down
    exit 1
fi
log "Chrome доступен в Selenoid"

# === Создаём папки для результатов ===
mkdir -p "$TEST_OUTPUT_DIR"
mkdir -p "$TEST_LOGS_DIR"

# === Преобразуем пути в Windows-формат для Docker Desktop ===
if [ "$OS" = "git-bash" ]; then
    if command -v cygpath &> /dev/null; then
        # Преобразуем в C:\... → C:/... (Docker понимает /, но не \)
        WIN_TEST_OUTPUT_DIR=$(cygpath -w "$TEST_OUTPUT_DIR" | sed 's|\\|/|g')
        WIN_TEST_LOGS_DIR=$(cygpath -w "$TEST_LOGS_DIR" | sed 's|\\|/|g')
        log "📁 Volume host path (Windows): $WIN_TEST_OUTPUT_DIR"
    else
        error "cygpath не найден. Установите Git с полным набором утилит."
        exit 1
    fi
else
    # Linux, WSL, macOS — оставляем как есть
    WIN_TEST_OUTPUT_DIR="$TEST_OUTPUT_DIR"
    WIN_TEST_LOGS_DIR="$TEST_LOGS_DIR"
fi
# === Запуск тестов ===
log "Запуск API и UI тестов в Docker..."

docker run --rm \
  --network nbank-network \
  -e APIBASEURL="http://backend:4111" \
  -e UIBASEURL="http://nginx" \
  -v "$WIN_TEST_OUTPUT_DIR/site":/app/target/site \
  -v "$WIN_TEST_OUTPUT_DIR/surefire-reports":/app/target/surefire-reports \
  -v "$WIN_TEST_LOGS_DIR":/app/logs \
  nbank-tests:latest \
  sh -c "
      echo '🚀 Запуск тестов...' ;
      mvn test -P all \
        -DapiBaseUrl=http://backend:4111 \
        -DuiRemote=http://selenoid:4444/wd/hub \
        -DuiBaseUrl=http://nginx \
        -DbrowserSize=1920x1080 \
        -Dbrowser=chrome ;

      RC=\$?
      echo \"Код завершения тестов: \$RC\"

      # === Генерация отчёта ===
      echo '📊 Генерация HTML-отчёта...'
      mvn -q -DskipTests=true surefire-report:report

      # === Копируем вручную (резерв) ===
      mkdir -p /app/target/site /app/target/surefire-reports
      cp -r /app/target/site /app/target/surefire-reports /app/target/failsafe-reports 2>/dev/null || true

      # === Ждём синхронизации ===
      echo '⏳ Ждём 3 секунды для сброса данных...'
      sleep 3

      # === Команда sync (принудительная синхронизация) ===
      sync /app/target || echo 'sync не сработал'

      exit \$RC
    "

# === Проверка результата ===
if [ $? -ne 0 ]; then
    error "Тесты завершились с ошибкой"
    exit 0
fi

# === Копируем явно, если вдруг volume не сработал (резерв) ===
mkdir -p "$TEST_REPORT_DIR"
if [ -f "$TEST_OUTPUT_DIR/site/surefire-report.html" ]; then
    cp "$TEST_OUTPUT_DIR/site/surefire-report.html" "$TEST_REPORT_DIR/"
    cp -r "$TEST_OUTPUT_DIR/surefire-reports" "$TEST_REPORT_DIR/" 2>/dev/null || true
fi

# === Финал ===
log "Тесты успешно завершены!"
echo "📁 Результаты: $TEST_OUTPUT_DIR"
echo "📊 Отчёт: file://$TEST_OUTPUT_DIR/site/surefire-report.html"
echo "📌 Логи Selenoid: $COMPOSE -f $COMPOSE_FILE logs selenoid"
echo "📌 UI Selenoid: http://localhost:6567"

# Спросить, остановить ли окружение
read -p "Остановить Docker Compose? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    $COMPOSE -f "$COMPOSE_FILE" down
    log "Docker Compose остановлен"
fi