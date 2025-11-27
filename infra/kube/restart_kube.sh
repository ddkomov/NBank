#!/bin/bash

# ШАГ 1: поднятие сервисов приложения

# Запустили локальный Kubernetes-кластер с помощью minikube, используя Docker как драйвер
# (кластер будет запущен внутри докер контейнера)
minikube start --driver=docker

# Создали ConfigMap с именем selenoid-config, файл будет доступен под ключом browsers.json
kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

# Устанавливаем Helm чарт с именем релиза nbank, беря шаблоны из ./nbank-chart
# Это создаст все ресурсв, описанные в шаблонах Helm (Deployment, Service)
helm install nbank ./nbank-chart

# Все сервисы в namespace=default
kubectl get svc

# Все поды в namespace=default
kubectl get pods

# Логи конкретного сервиса
kubectl logs deployment/backend

# Проброс портов на локальную машину
kubectl port-forward svc/frontend 3000:80 #  > /dev/null 2>&1 & (проброс порта в фоновом режиме)
kubectl port-forward svc/backend 4111:4111
kubectl port-forward svc/selenoid 4444:4444
kubectl port-forward svc/selenoid-ui 8080:8080

# ШАГ 2: поднятие сервисов мониторинга
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f monitoring-values.yaml
# ШАГ 3: поднятие сервисов логирования
# Устанавливаем Elasticsearch
helm install elasticsearch elastic/elasticsearch -f elastic-values.yaml --namespace logging --create-namespace --set singleNode=true
# Установка Fluent Bit
helm repo add fluent https://fluent.github.io/helm-charts
helm repo update

helm install fluent-bit fluent/fluent-bit -f fluent-bit-values.yaml --namespace logging --create-namespace
# Устанавливаем Kibana
helm install kibana elastic/kibana -f kibana-values.yaml --namespace logging --create-namespace
# Пробрасываем порт к прометеусу и графане и кибане
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090 # > /dev/null 2>&1
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80
kubectl port-forward svc/kibana-kibana -n logging 5601:5601
# Создаем секреты для авторизации на бекенде
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring
# Применяем yaml с настройкой SpringMonitoring за бекендом
kubectl apply -f spring-monitoring.yaml



