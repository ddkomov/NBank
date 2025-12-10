#!/bin/bash
set -e

echo "🛑 Stopping and cleaning up previous Minikube instance..."
minikube stop || true
minikube delete || true

echo "🚀 Starting Minikube with enhanced resources..."
minikube start --driver=docker --cpus=2 --memory=6g --disk-size=15g

echo "🔧 Enabling required addons..."
minikube addons enable ingress

echo "📦 Adding required Helm repositories..."
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

cd "$(dirname "$0")/.."
echo "🚀 Installing NBank application..."
helm uninstall nbank || true
helm install nbank ./nbank-chart

echo "⏳ Waiting for NBank backend to be ready..."
kubectl wait --for=condition=ready pod -l app=backend --timeout=300s

echo "📋 NBank Backend logs:"
kubectl logs deployment/backend --tail=10

echo "📈 Installing Prometheus + Grafana stack..."
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  -f monitoring/monitoring-values.yaml \
  --timeout=10m

echo "📋 Checking pods in monitoring namespace..."
kubectl get pods -n monitoring

echo "⏳ Waiting for pods to start (give time for Init Containers)..."
sleep 60

echo "⏳ Waiting for Prometheus pod to be ready..."
kubectl wait --for=condition=ready pod -l app=prometheus -n monitoring --timeout=300s || \
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n monitoring --timeout=300s

echo "⏳ Waiting for Grafana pod to be ready..."
kubectl wait --for=condition=ready pod -l app=grafana -n monitoring --timeout=300s || \
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s

echo "🔐 Creating monitoring basic auth secret..."
kubectl create secret generic backend-basic-auth \
  --from-literal=username=admin \
  --from-literal=password=admin \
  -n monitoring || true

echo "📡 Applying ServiceMonitor for Spring Boot metrics..."
kubectl apply -f monitoring/spring-monitoring.yaml

echo "📊 Installing Elasticsearch..."
helm upgrade --install elasticsearch elastic/elasticsearch \
  -n logging --create-namespace \
  -f logging/elasticsearch-values.yaml \
  --timeout=10m

echo "⏳ Waiting for pods to start (give time for Init Containers)..."
sleep 60

echo "⏳ Waiting for Elasticsearch to be ready..."
kubectl wait --for=condition=ready pod -l app=elasticsearch-master -n logging --timeout=600s

echo "📊 Installing Kibana..."
helm upgrade --install kibana elastic/kibana \
  -n logging \
  -f logging/kibana-values.yaml \
  --timeout=10m

echo "⏳ Waiting for Kibana to be ready..."
kubectl wait --for=condition=ready pod -l app=kibana -n logging --timeout=600s

echo "📝 Installing Filebeat for log collection..."
ELASTIC_PWD=$(kubectl get secret elasticsearch-master-credentials -n logging -o jsonpath="{.data.password}" | base64 -d)
NODE_NAME=$(kubectl get node -o jsonpath="{.items[0].metadata.name}")

helm upgrade --install filebeat elastic/filebeat \
  -n logging \
  --set elasticsearch.hosts="{https://elasticsearch-master:9200}" \
  --set elasticsearch.username=elastic \
  --set elasticsearch.password="$ELASTIC_PWD" \
  --set elasticsearch.ssl.verificationMode=none \
  --set "tolerations[0].operator=Exists" \
  --set "filebeat.autodiscover.providers[0].type=kubernetes" \
  --set "filebeat.autodiscover.providers[0].node=$NODE_NAME" \
  --set "filebeat.autodiscover.providers[0].hints.enabled=true" \
  --set "processors[0].add_kubernetes_metadata.host=true" \
  --set "processors[0].add_kubernetes_metadata.matchers[0].logs_path.logs_path=/var/log/containers/" \
  --set daemonset.useHostPID=true \
  --timeout=5m

echo "🔌 Setting up port forwarding for all services..."

# Kill existing port-forward processes
pkill -f "kubectl port-forward" || true
sleep 2

# Start port forwarding in background
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 9090:9090 > /dev/null 2>&1 &
kubectl port-forward svc/monitoring-grafana -n monitoring 3001:80 > /dev/null 2>&1 &
kubectl port-forward svc/kibana-kibana -n logging 5601:5601 > /dev/null 2>&1 &

# Wait for port forwards to establish
sleep 5

echo ""
echo "🎉 All systems are up and running!"
echo "=============================================="
echo ""
echo "📊 SERVICES ACCESS INFORMATION"
echo "=============================================="
echo "🔗 NBank Backend:   http://localhost:4111"
echo "🔗 NBank Frontend:  http://localhost:3000"
echo "🔗 Prometheus:      http://localhost:9090"
echo "🔗 Grafana:         http://localhost:3001 (admin / admin)"
echo "🔗 Kibana:          http://localhost:5601 (elastic / $ELASTIC_PWD)"
echo ""
echo "📋 USEFUL COMMANDS"
echo "=============================================="
echo "# Check all pods status:"
echo "kubectl get pods -A"
echo ""
echo "# Check services:"
echo "kubectl get svc -A"
echo ""
echo "# View backend logs:"
echo "kubectl logs deployment/backend"
echo ""
echo "# Scale services:"
echo "kubectl scale deployment backend --replicas=2"
echo "kubectl scale deployment frontend --replicas=2"
echo ""
echo "# Restart port forwarding if needed:"
echo "./port_forward.sh"
echo ""
echo "✅ Deployment completed successfully!"

# Display cluster status
echo ""
echo "📊 CLUSTER STATUS"
echo "=============================================="
kubectl get pods -A
echo ""
kubectl get svc -A