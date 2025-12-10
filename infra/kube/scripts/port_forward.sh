#!/bin/bash

echo "🔌 Setting up port forwarding for all NBank services..."
echo "======================================================="

# Kill existing port-forward processes
echo "🛑 Stopping existing port forwards..."
pkill -f "kubectl port-forward" || true
sleep 2

echo "🚀 Starting port forwarding services..."

# NBank Application Services
echo "📱 Setting up NBank services..."
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
BACKEND_PID=$!
echo "  ✅ Backend: http://localhost:4111 (PID: $BACKEND_PID)"

kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &
FRONTEND_PID=$!
echo "  ✅ Frontend: http://localhost:3000 (PID: $FRONTEND_PID)"

# Selenoid Services
echo "🔧 Setting up Selenoid services..."
kubectl port-forward svc/selenoid 4444:4444 > /dev/null 2>&1 &
SELENOID_PID=$!
echo "  ✅ Selenoid: http://localhost:4444 (PID: $SELENOID_PID)"

kubectl port-forward svc/selenoid-ui 8080:8080 > /dev/null 2>&1 &
SELENOID_UI_PID=$!
echo "  ✅ Selenoid UI: http://localhost:8080 (PID: $SELENOID_UI_PID)"

# Monitoring Services
echo "📊 Setting up monitoring services..."
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 9090:9090 > /dev/null 2>&1 &
PROMETHEUS_PID=$!
echo "  ✅ Prometheus: http://localhost:9090 (PID: $PROMETHEUS_PID)"

kubectl port-forward svc/monitoring-grafana -n monitoring 3001:80 > /dev/null 2>&1 &
GRAFANA_PID=$!
echo "  ✅ Grafana: http://localhost:3001 (PID: $GRAFANA_PID)"

# Logging Services
echo "📝 Setting up logging services..."
kubectl port-forward svc/kibana-kibana -n logging 5601:5601 > /dev/null 2>&1 &
KIBANA_PID=$!
echo "  ✅ Kibana: http://localhost:5601 (PID: $KIBANA_PID)"

kubectl port-forward svc/elasticsearch-master -n logging 9200:9200 > /dev/null 2>&1 &
ELASTICSEARCH_PID=$!
echo "  ✅ Elasticsearch: http://localhost:9200 (PID: $ELASTICSEARCH_PID)"

# Wait for port forwards to establish
echo ""
echo "⏳ Waiting for port forwards to establish..."
sleep 5

# Get Elasticsearch password
ELASTIC_PWD=$(kubectl get secret elasticsearch-master-credentials -n logging -o jsonpath="{.data.password}" | base64 -d 2>/dev/null || echo "admin")

echo ""
echo "🎉 All port forwards are active!"
echo "==============================="
echo ""
echo "📱 NBANK APPLICATION"
echo "  🔗 Backend API:    http://localhost:4111"
echo "  🔗 Frontend UI:    http://localhost:3000"
echo ""
echo "🔧 TESTING TOOLS"
echo "  🔗 Selenoid:       http://localhost:4444"
echo "  🔗 Selenoid UI:    http://localhost:8080"
echo ""
echo "📊 MONITORING"
echo "  🔗 Prometheus:     http://localhost:9090"
echo "  🔗 Grafana:        http://localhost:3001"
echo "      Username: admin"
echo "      Password: admin"
echo ""
echo "📝 LOGGING"
echo "  🔗 Kibana:         http://localhost:5601"
echo "  🔗 Elasticsearch:  http://localhost:9200"
echo "      Username: elastic"
echo "      Password: $ELASTIC_PWD"
echo ""
echo "🔧 PROCESS MANAGEMENT"
echo "========================"
echo "To stop all port forwards: pkill -f 'kubectl port-forward'"
echo "To restart this script: ./port_forward_monitoring.sh"
echo ""
echo "Active PIDs:"
echo "  Backend: $BACKEND_PID"
echo "  Frontend: $FRONTEND_PID"
echo "  Selenoid: $SELENOID_PID"
echo "  Selenoid UI: $SELENOID_UI_PID"
echo "  Prometheus: $PROMETHEUS_PID"
echo "  Grafana: $GRAFANA_PID"
echo "  Kibana: $KIBANA_PID"
echo "  Elasticsearch: $ELASTICSEARCH_PID"
echo ""
echo "✅ Port forwarding setup completed!"

# Keep script running to show status
echo ""
echo "🔄 Port forwards are running in background..."
echo "Press Ctrl+C to stop this script (port forwards will continue)"
echo ""

# Function to check if services are accessible
check_services() {
    echo "🔍 Checking service accessibility..."

    # Check backend
    if curl -s http://localhost:4111/actuator/health > /dev/null 2>&1; then
        echo "  ✅ Backend is accessible"
    else
        echo "  ❌ Backend is not accessible"
    fi

    # Check frontend
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        echo "  ✅ Frontend is accessible"
    else
        echo "  ❌ Frontend is not accessible"
    fi

    # Check Prometheus
    if curl -s http://localhost:9090 > /dev/null 2>&1; then
        echo "  ✅ Prometheus is accessible"
    else
        echo "  ❌ Prometheus is not accessible"
    fi

    # Check Grafana
    if curl -s http://localhost:3001 > /dev/null 2>&1; then
        echo "  ✅ Grafana is accessible"
    else
        echo "  ❌ Grafana is not accessible"
    fi

    echo ""
}

# Wait a bit more and check services
sleep 10
check_services

# Keep the script running
while true; do
    sleep 30
done