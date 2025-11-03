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

# === Определение платформы и WSL ===
detect_os() {
    case "${OSTYPE}" in
        "linux-gnu"* )
            if grep -qi microsoft /proc/version &>/dev/null; then
                echo "wsl"
            else
                echo "linux"
            fi
            ;;
        "msys"*)
            # Git Bash на Windows
            if command -v wslpath &> /dev/null; then
                echo "wsl"  # Скорее всего, запущено из WSL через Git Bash
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

# === Функция для установки jq ===
install_jq() {
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
            error "Вы используете Git Bash."
            error "jq не установлен. Рекомендуется:"
            error "  1. Установить jq через Chocolatey: choco install jq"
            error "  2. Или использовать WSL: wsl ./restart_docker.sh"
            error "  3. Скачать вручную: https://stedolan.github.io/jq/download/"
            exit 1
            ;;
        "unknown")
            error "Не удалось определить ОС. Установите jq вручную."
            exit 1
            ;;
    esac

    # Проверка после установки
    if ! command -v jq &> /dev/null; then
        error "Не удалось установить jq. Убедитесь, что он установлен и в PATH."
        exit 1
    fi

    log "jq успешно установлен!"
}

# === Проверка и установка jq ===
if ! command -v jq &> /dev/null; then
    install_jq
else
    log "jq уже установлен"
fi

# === Основная логика скрипта ===

log "Определённая ОС: $OS"

echo ">>> Остановка Docker Compose"
docker compose down || warn "docker compose down завершился с ошибкой (возможно, контейнеры не запущены)"

echo ">>> Скачивание всех образов браузеров"

json_file="./config/browsers.json"

# Проверка существования файла
if [ ! -f "$json_file" ]; then
    # Попробуем найти в других путях (на случай WSL)
    if [ -f "../config/browsers.json" ]; then
        json_file="../config/browsers.json"
    elif [ -f "config/browsers.json" ]; then
        json_file="config/browsers.json"
    else
        error "Файл config/browsers.json не найден ни в текущей, ни в родительской директории"
        exit 1
    fi
fi

log "Используется файл конфигурации: $json_file"

# Извлекаем все .image из JSON
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file" 2>/dev/null)

# Проверка, что найдены образы
if [ -z "$images" ] || [ "$images" = "null" ]; then
    warn "Не найдено ни одного образа в $json_file"
    exit 0
fi

log "Найдены образы: $images"

# Выполняем docker pull для каждого образа
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image" || error "Ошибка при загрузке образа: $image"
done

log "Все образы успешно загружены"

echo ">>> Запуск Docker Compose"
docker compose up -d --remove-orphans

log "Docker Compose запущен в фоне (--detach)."
echo "📌 Проверить статус: docker compose ps"
echo "📌 Логи: docker compose logs -f"