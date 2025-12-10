# 🚀 NBank Deployment in Kubernetes with Monitoring Stack

## 📋 Task Description

Complete deployment and management of NBank application in Kubernetes using Helm Chart with integrated monitoring and logging stack. The system includes:

- **Core Application**: backend, frontend, selenoid, selenoid-ui
- **Monitoring Stack**: Prometheus + Grafana for metrics
- **Logging Stack**: Elasticsearch + Kibana for logs analysis

## 🏗️ Application Architecture

### Services and their ports:

### Core Application:

| Service         | Internal Port | External Port (NodePort) | Local Access          |
| --------------- | ------------- | ------------------------ | --------------------- |
| **Backend**     | 4111          | 30411                    | http://localhost:4111 |
| **Frontend**    | 80            | 30080                    | http://localhost:3000 |
| **Selenoid**    | 4444          | 30444                    | http://localhost:4444 |
| **Selenoid UI** | 8080          | 30808                    | http://localhost:8080 |

### Monitoring & Logging Stack:

| Service           | Internal Port | Local Access          | Credentials   |
| ----------------- | ------------- | --------------------- | ------------- |
| **Prometheus**    | 9090          | http://localhost:9090 | No auth       |
| **Grafana**       | 80            | http://localhost:3001 | admin / admin |
| **Elasticsearch** | 9200          | http://localhost:9200 | No auth       |
| **Kibana**        | 5601          | http://localhost:5601 | No auth       |

## 🚀 Quick Start

### 1. Deploy complete stack with monitoring:

```bash
cd infra/kube/scripts
chmod +x restart_kube.sh
./restart_kube.sh
```

This script automatically:

- Starts Minikube cluster with enhanced resources
- Deploys NBank application via Helm
- Installs Prometheus + Grafana monitoring stack
- Deploys Elasticsearch + Kibana logging stack
- Configures ServiceMonitor for Spring Boot metrics
- Sets up Filebeat for log collection
- Configures port forwarding for all services

### 2. Alternative: Port forwarding script:

```bash
cd infra/kube/scripts
./port_forward.sh
```

This sets up port forwarding for all services:

- **NBank Application**: Backend, Frontend, Selenoid, Selenoid UI
- **Monitoring**: Prometheus, Grafana
- **Logging**: Elasticsearch, Kibana

## 📊 Detailed Task Information

### Task 1: Services Deployment in Kubernetes

✅ **Completed**: Created Helm Chart with 4 services in `nbank-chart/`

### Task 2: Services and Ports Description

✅ **Completed**: All services configured with NodePort type:

**Backend Service:**

- Internal port: 4111
- External port: 30411
- Image: `nobugsme/nbank:with_validation_fix`

**Frontend Service:**

- Internal port: 80
- External port: 30080
- Image: `nobugsme/nbank-ui:with_nginx`

**Selenoid Service:**

- Internal port: 4444
- External port: 30444
- Image: `aerokube/selenoid:latest`

**Selenoid UI Service:**

- Internal port: 8080
- External port: 30808
- Image: `aerokube/selenoid-ui:latest`

### Task 3: Pods List

```bash
# Get list of all pods
kubectl get pods

# Get detailed information
kubectl get pods -o wide

# Check specific pod status
kubectl describe pod <pod-name>
```

### Task 4: Services Logs Viewing

```bash
# Logs of all services
kubectl logs deployment/backend
kubectl logs deployment/frontend
kubectl logs deployment/selenoid
kubectl logs deployment/selenoid-ui

# Follow logs
kubectl logs deployment/backend -f

# Last 50 lines of logs
kubectl logs deployment/backend --tail=50
```

### Task 5: Port Forwarding

```bash
# Port forwarding for all services
kubectl port-forward svc/frontend 3000:80 &
kubectl port-forward svc/backend 4111:4111 &
kubectl port-forward svc/selenoid 4444:4444 &
kubectl port-forward svc/selenoid-ui 8080:8080 &

# Availability check
curl http://localhost:4111/actuator/health  # Backend health check
curl http://localhost:3000                  # Frontend
curl http://localhost:4444/status          # Selenoid status
curl http://localhost:8080                 # Selenoid UI
```

### Task 6: ConfigMap and Secrets

✅ **ConfigMap Created**: `selenoid-config` with browser configuration

```bash
# View ConfigMap
kubectl get configmap selenoid-config -o yaml

# Describe ConfigMap
kubectl describe configmap selenoid-config
```

**ConfigMap contains:**

- Browser configuration for Selenoid (Chrome 91.0, Firefox 89.0, Opera 76.0)
- Docker images settings for each browser
- Ports and paths for WebDriver

### Task 7: Verification via kubectl

```bash
# Check services
kubectl get svc
kubectl describe svc backend
kubectl describe svc frontend

# Check pods
kubectl get pods
kubectl describe pod <pod-name>

# Check endpoints
kubectl get endpoints
```

### Task 8: Deployment via Helm

```bash
# Installation
helm install nbank ./nbank-chart

# Upgrade
helm upgrade nbank ./nbank-chart

# Uninstall
helm uninstall nbank

# Status check
helm status nbank
helm list
```

### Task 9: Using kubectl port-forward

✅ **Implemented**: Commands for port forwarding of all services

```bash
# Simultaneous port forwarding for all ports
kubectl port-forward svc/frontend 3000:80 &
kubectl port-forward svc/backend 4111:4111 &
kubectl port-forward svc/selenoid 4444:4444 &
kubectl port-forward svc/selenoid-ui 8080:8080 &

# Check processes
jobs
ps aux | grep port-forward

# Stop all port-forward
pkill -f "kubectl port-forward"
```

### Task 10: Pods Scaling

```bash
# Increase replica count
kubectl scale deployment backend --replicas=2
kubectl scale deployment frontend --replicas=3

# Check after scaling
kubectl get pods -l app=backend
kubectl get pods -l app=frontend

# Automatic scaling (HPA)
kubectl autoscale deployment backend --cpu-percent=50 --min=1 --max=5

# Check HPA
kubectl get hpa
```

## 🔧 Useful Commands for Monitoring

```bash
# Check cluster resources
kubectl top nodes
kubectl top pods

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp

# Check cluster status
kubectl cluster-info
kubectl get componentstatuses

# Check Helm configuration
helm get values nbank
helm get manifest nbank
```

## 🎯 Execution Result

After running `./restart_kube.sh` you will get:

- Fully functional Kubernetes cluster with NBank application
- 4 services accessible via localhost
- Detailed information about all components
- Ready commands for further work

All services will be accessible:

- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:4111
- **Selenoid**: http://localhost:4444
- **Selenoid UI**: http://localhost:8080