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
SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
CONFIG_DIR="$SCRIPT_DIR/config"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
BROWSER_CONFIG="$CONFIG_DIR/browsers.json"
TEST_OUTPUT_DIR="$SCRIPT_DIR/test-output/$(date +%Y%m%d_%H%M%S)"
TEST_RESULTS_DIR="$TEST_OUTPUT_DIR/results"
TEST_REPORT_DIR="$TEST_OUTPUT_DIR/report"
TEST_LOGS_DIR="$TEST_OUTPUT_DIR/logs"

# === Проверка файлов ===
if [ ! -f "$BROWSER_CONFIG" ]; then
    error "Файл browsers.json не найден: $BROWSER_CONFIG"
    exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
    error "Файл docker-compose.yml не найден: $COMPOSE_FILE"
    exit 1
fi

log "Используется конфиг: $BROWSER_CONFIG"
log "Используется compose: $COMPOSE_FILE"

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
mkdir -p "$TEST_RESULTS_DIR"
mkdir -p "$TEST_REPORT_DIR"
mkdir -p "$TEST_LOGS_DIR"

# === Запуск тестов ===
log "Запуск API и UI тестов в Docker..."

docker run --rm \
  --network nbank-network \
  -e APIBASEURL="http://backend:4111" \
  -e UIBASEURL="http://nginx" \
  -v "$TEST_RESULTS_DIR":/app/target/surefire-reports \
  -v "$TEST_REPORT_DIR":/app/target/site \
  -v "$TEST_LOGS_DIR":/app/logs \
  nbank-tests:latest \
  mvn test -P api,ui \
    -DapiBaseUrl="http://backend:4111" \
    -DuiRemote="http://selenoid:4444/wd/hub" \
    -DuiBaseUrl="http://nginx" \
    -DbrowserSize="1920x1080" \
    -Dbrowser="chrome"

# === Проверка результата ===
if [ $? -ne 0 ]; then
    error "Тесты завершились с ошибкой"
    exit 1
fi

# === Финал ===
log "Тесты успешно завершены!"
echo "📁 Результаты: $TEST_OUTPUT_DIR"
echo "📊 Отчёт: file://$TEST_OUTPUT_DIR/report/surefire-report.html"
echo "📌 Логи Selenoid: $COMPOSE -f $COMPOSE_FILE logs selenoid"
echo "📌 UI Selenoid: http://localhost:6567"

# Спросить, остановить ли окружение
read -p "Остановить Docker Compose? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    $COMPOSE -f "$COMPOSE_FILE" down
    log "Docker Compose остановлен"
fi