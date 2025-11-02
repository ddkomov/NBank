#!/bin/bash

# Настройка
IMAGE_NAME=nbank-tests
DOCKERHUB_USERNAME=ddkomov # ⚠️ Docker Hub логин
TEST_PROFILE=${1:-api} # аргумент запуска
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP
TAG=latest

# Полное имя образа
FULL_IMAGE_NAME="$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

echo ">>> Подготовка к отправке образа: $FULL_IMAGE_NAME"

# === Шаг 1: Логин в Docker Hub с использованием токена ===
echo ">>> Выполняем вход в Docker Hub..."
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

if [ $? -ne 0 ]; then
    echo "❌ Ошибка входа в Docker Hub. Проверьте токен и имя пользователя."
    exit 1
fi

echo "✅ Успешный вход в Docker Hub"

# === Шаг 2: Тегируем локальный образ ===
echo ">>> Тегируем образ как $FULL_IMAGE_NAME"
docker tag $IMAGE_NAME $FULL_IMAGE_NAME

if [ $? -ne 0 ]; then
    echo "❌ Ошибка тегирования. Убедитесь, что образ '$IMAGE_NAME' существует локально."
    exit 1
fi

# === Шаг 3: Пушим образ в Docker Hub ===
echo ">>> Отправляем образ в Docker Hub..."
docker push $FULL_IMAGE_NAME

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при отправке образа в Docker Hub."
    exit 1
fi

echo "✅ Образ успешно отправлен: $FULL_IMAGE_NAME"
echo "👉 Теперь его можно использовать: docker pull $FULL_IMAGE_NAME"

# Собираем Docker образ
echo ">>> Сборка тестов запущена"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Запуск Docker контейнера
echo ">>> Тесты запущены"
docker run --rm \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://192.168.1.12 \
  -e UIBASEURL=http://192.168.1.12 \
$IMAGE_NAME

# Вывод итогов
echo ">>> Тесты завершены"
echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Репорт: $TEST_OUTPUT_DIR/report"