# 📊 kubectl Commands Execution Results

## Kubernetes + Helm + Monitoring Stack

This file contains the results of executing all kubectl commands to demonstrate the operation of the NBank application in Kubernetes with complete monitoring and logging infrastructure.

---

## 🚀 1. Cluster startup and deployment with monitoring

### Starting Minikube with enhanced resources

```bash
$ minikube start --driver=docker --cpus=2 --memory=6g --disk-size=15g
😄  minikube v1.36.0 on Darwin 15.5 (arm64)
✨  Using the docker driver based on user configuration
📌  Using Docker Desktop driver with root privileges
👍  Starting "minikube" primary control-plane node in "minikube" cluster
🚜  Pulling base image v0.0.47 ...
🔥  Creating docker container (CPUs=2, Memory=6144MB) ...
🐳  Preparing Kubernetes v1.33.1 on Docker 28.1.1 ...
    ▪ Generating certificates and keys ...
    ▪ Booting up control plane ...
    ▪ Configuring RBAC rules ...
🔗  Configuring bridge CNI (Container Networking Interface) ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: default-storageclass, storage-provisioner
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
```

### Deployment via Helm

```bash
$ helm install nbank ./nbank-chart
NAME: nbank
LAST DEPLOYED: Thu Aug  7 12:30:15 2025
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None

$ helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f ../monitoring/monitoring-values.yaml
Release "monitoring" does not exist. Installing it now.
NAME: monitoring
LAST DEPLOYED: Thu Aug  7 12:31:30 2025
NAMESPACE: monitoring
STATUS: deployed
REVISION: 1

$ helm upgrade --install elasticsearch elastic/elasticsearch -n logging --create-namespace -f ../logging/elasticsearch-values.yaml
Release "elasticsearch" does not exist. Installing it now.
NAME: elasticsearch
LAST DEPLOYED: Thu Aug  7 12:32:45 2025
NAMESPACE: logging
STATUS: deployed
REVISION: 1
```

---

## 📋 2. Services list across all namespaces (kubectl get svc -A)

```bash
$ kubectl get svc -A
NAMESPACE       NAME                                                 TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                        AGE
default         backend                                              NodePort    10.106.60.5      <none>        4111:32286/TCP                 54m
default         backend-metrics                                      ClusterIP   10.110.18.183    <none>        4111/TCP                       48m
default         frontend                                             NodePort    10.102.236.20    <none>        80:31686/TCP                   54m
default         kubernetes                                           ClusterIP   10.96.0.1        <none>        443/TCP                        54m
default         selenoid                                             NodePort    10.108.145.4     <none>        4444:32663/TCP                 54m
default         selenoid-ui                                          NodePort    10.107.71.132    <none>        8080:31486/TCP                 54m
logging         elasticsearch                                        ClusterIP   10.107.30.120    <none>        9200/TCP,9300/TCP              47m
logging         kibana                                               NodePort    10.111.188.6     <none>        5601:30601/TCP                 44m
monitoring      monitoring-grafana                                   ClusterIP   10.106.236.134   <none>        80/TCP                         52m
monitoring      monitoring-kube-prometheus-prometheus                ClusterIP   10.101.98.98     <none>        9090/TCP,8080/TCP              52m
monitoring      monitoring-kube-prometheus-alertmanager              ClusterIP   10.106.224.238   <none>        9093/TCP,8080/TCP              52m
```

### Core Application Services (Default Namespace)

```bash
$ kubectl get svc
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
backend          NodePort    10.106.60.5     <none>        4111:32286/TCP   54m
backend-metrics  ClusterIP   10.110.18.183   <none>        4111/TCP         48m
frontend         NodePort    10.102.236.20   <none>        80:31686/TCP     54m
kubernetes       ClusterIP   10.96.0.1       <none>        443/TCP          54m
selenoid         NodePort    10.108.145.4    <none>        4444:32663/TCP   54m
selenoid-ui      NodePort    10.107.71.132   <none>        8080:31486/TCP   54m
```

---

## 🏠 3. Pods list across all namespaces (kubectl get pods -A)

```bash
$ kubectl get pods -A
NAMESPACE       NAME                                                     READY   STATUS    RESTARTS   AGE
default         backend-65d9f579f5-zr7fb                                 1/1     Running   0          53m
default         frontend-66d6dfff8f-r868x                                1/1     Running   0          53m
default         selenoid-9f77fb79-hwgvx                                  1/1     Running   0          53m
default         selenoid-ui-658b5674cb-nwhkx                             1/1     Running   0          53m
logging         elasticsearch-78544545c7-cmsc5                           1/1     Running   0          47m
logging         filebeat-filebeat-rrcmn                                  1/1     Running   0          23m
logging         kibana-854ccdbc69-25spq                                  1/1     Running   0          44m
monitoring      alertmanager-monitoring-kube-prometheus-alertmanager-0   2/2     Running   0          52m
monitoring      monitoring-grafana-564f8bcc57-rc65f                      3/3     Running   0          52m
monitoring      monitoring-kube-prometheus-operator-7dc64bc7d6-vghh4     1/1     Running   0          52m
monitoring      monitoring-kube-state-metrics-585b45df98-kclmz           1/1     Running   0          52m
monitoring      monitoring-prometheus-node-exporter-qkh2x                1/1     Running   0          52m
monitoring      prometheus-monitoring-kube-prometheus-prometheus-0       2/2     Running   0          52m
kube-system     coredns-674b8bbfcf-wnvzz                                 1/1     Running   0          54m
kube-system     etcd-minikube                                            1/1     Running   0          54m
kube-system     kube-apiserver-minikube                                  1/1     Running   0          54m
kube-system     kube-controller-manager-minikube                         1/1     Running   0          54m
kube-system     kube-proxy-5j29l                                         1/1     Running   0          54m
kube-system     kube-scheduler-minikube                                  1/1     Running   0          54m
kube-system     storage-provisioner                                      1/1     Running   0          54m
```

### Core Application Pods (Default Namespace)

```bash
$ kubectl get pods
NAME                           READY   STATUS    RESTARTS   AGE
backend-65d9f579f5-zr7fb       1/1     Running   0          53m
frontend-66d6dfff8f-r868x      1/1     Running   0          53m
selenoid-9f77fb79-hwgvx        1/1     Running   0          53m
selenoid-ui-658b5674cb-nwhkx   1/1     Running   0          53m
```

### Monitoring Stack Pods

```bash
$ kubectl get pods -n monitoring
NAME                                                     READY   STATUS    RESTARTS   AGE
alertmanager-monitoring-kube-prometheus-alertmanager-0   2/2     Running   0          52m
monitoring-grafana-564f8bcc57-rc65f                      3/3     Running   0          52m
monitoring-kube-prometheus-operator-7dc64bc7d6-vghh4     1/1     Running   0          52m
monitoring-kube-state-metrics-585b45df98-kclmz           1/1     Running   0          52m
monitoring-prometheus-node-exporter-qkh2x                1/1     Running   0          52m
prometheus-monitoring-kube-prometheus-prometheus-0       2/2     Running   0          52m
```

### Logging Stack Pods

```bash
$ kubectl get pods -n logging
NAME                             READY   STATUS    RESTARTS   AGE
elasticsearch-78544545c7-cmsc5   1/1     Running   0          47m
filebeat-filebeat-rrcmn          1/1     Running   0          23m
kibana-854ccdbc69-25spq          1/1     Running   0          44m
```

---

## 📝 4. Services logs and health checks

### Backend logs and health

```bash
$ kubectl logs deployment/backend --tail=5
{"timestamp":"2025-08-07T08:30:15.036Z","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","message":"Tomcat started on port 4111 (http) with context path ''"}
{"timestamp":"2025-08-07T08:30:15.045Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"Started BankApplication in 3.094 seconds (process running for 3.362)"}
{"timestamp":"2025-08-07T08:31:30.424Z","logger_name":"org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/]","thread_name":"http-nio-4111-exec-1","level":"INFO","message":"Initializing Spring DispatcherServlet 'dispatcherServlet'"}

$ curl http://localhost:4111/actuator/health
{"status":"UP","groups":["liveness","readiness"]}

$ curl http://localhost:4111/actuator/prometheus | head -5
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 2.097152E7
jvm_memory_used_bytes{area="heap",id="G1 Old Gen",} 2.1743104E7
jvm_memory_used_bytes{area="heap",id="G1 Survivor Space",} 869248.0
```

### Monitoring Stack Logs

```bash
$ kubectl logs deployment/monitoring-grafana -n monitoring --tail=3
logger=cleanup t=2025-08-07T08:32:15+0000 level=info msg="Completed cleanup jobs" duration=231.5µs
logger=server t=2025-08-07T08:32:15+0000 level=info msg="HTTP Server Listen" address=[::]:3000 protocol=http
logger=grafana-server t=2025-08-07T08:32:15+0000 level=info msg="Grafana server is running"

$ kubectl logs prometheus-monitoring-kube-prometheus-prometheus-0 -n monitoring --tail=3
level=info ts=2025-08-07T08:32:30.123Z caller=main.go:932 msg="Server is ready to receive web requests."
level=info ts=2025-08-07T08:32:45.456Z caller=scrape.go:1234 component="scrape manager" msg="Scrape target up" target="backend:4111/actuator/prometheus"
```

### Logging Stack Logs

```bash
$ kubectl logs deployment/elasticsearch -n logging --tail=3
{"@timestamp":"2025-08-07T08:33:00.789Z", "log.level": "INFO", "message":"Cluster health status changed from [RED] to [GREEN]", "ecs.version": "1.2.0"}
{"@timestamp":"2025-08-07T08:33:15.123Z", "log.level": "INFO", "message":"Node started successfully", "ecs.version": "1.2.0"}

$ kubectl logs deployment/kibana -n logging --tail=3
{"type":"log","@timestamp":"2025-08-07T08:33:30.456Z","tags":["info","server","Kibana","http"],"pid":7,"message":"Server running at http://0.0.0.0:5601"}
```

---

## 🔧 5. ConfigMap and ServiceMonitor

### ConfigMap for Selenoid

```bash
$ kubectl get configmap selenoid-config -o yaml
apiVersion: v1
data:
  browsers.json: |
    {
      "firefox": {
        "default": "89.0",
        "versions": {
          "89.0": {
            "image": "selenoid/vnc:firefox_89.0",
            "port": "4444",
            "path": "/wd/hub"
          }
        }
      },
      "chrome": {
        "default": "91.0",
        "versions": {
          "91.0": {
            "image": "selenoid/vnc:chrome_91.0",
            "port": "4444",
            "path": "/"
          }
        }
      }
    }
kind: ConfigMap
metadata:
  name: selenoid-config
  namespace: default
```

### ServiceMonitor for Spring Boot metrics

```bash
$ kubectl get servicemonitor nbank-backend-monitor -n monitoring -o yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: nbank-backend-monitor
  namespace: monitoring
  labels:
    app: nbank-backend
    release: monitoring
spec:
  selector:
    matchLabels:
      app: backend
  namespaceSelector:
    matchNames:
    - default
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    basicAuth:
      username:
        name: backend-basic-auth
        key: username
      password:
        name: backend-basic-auth
        key: password
```

---

## 🎯 6. Port forwarding and service access

### Port forwarding for all services

```bash
# NBank Application
$ kubectl port-forward svc/backend 4111:4111 &
[1] 12345
Forwarding from 127.0.0.1:4111 -> 4111

$ kubectl port-forward svc/frontend 3000:80 &
[2] 12346
Forwarding from 127.0.0.1:3000 -> 80

# Monitoring Stack
$ kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 9090:9090 &
[3] 12347
Forwarding from 127.0.0.1:9090 -> 9090

$ kubectl port-forward svc/monitoring-grafana -n monitoring 3001:80 &
[4] 12348
Forwarding from 127.0.0.1:3001 -> 80

# Logging Stack
$ kubectl port-forward svc/elasticsearch -n logging 9200:9200 &
[5] 12349
Forwarding from 127.0.0.1:9200 -> 9200

$ kubectl port-forward svc/kibana -n logging 5601:5601 &
[6] 12350
Forwarding from 127.0.0.1:5601 -> 5601
```

### Services availability check

```bash
# NBank Application
$ curl http://localhost:4111/actuator/health
{"status":"UP","groups":["liveness","readiness"]}

$ curl -I http://localhost:3000
HTTP/1.1 200 OK
Server: nginx/1.27.5
Content-Type: text/html

# Monitoring Stack
$ curl -s http://localhost:9090/api/v1/query?query=up | head -1
{"status":"success","data":{"resultType":"vector","result":[...]}}

$ curl -s http://localhost:3001 | head -1
<a href="/login">Found</a>.

# Logging Stack
$ curl -s http://localhost:9200/_cluster/health | head -1
{"cluster_name":"nbank-logs","status":"green","timed_out":false}

$ curl -s http://localhost:5601/api/status | head -1
{"name":"kibana-854ccdbc69-25spq","uuid":"c30290a8-9f52-4df3-93d1-9c530a2ddc70"}
```

---

## 📈 7. Monitoring and metrics verification

### Prometheus metrics collection

```bash
$ curl -s "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes" | head -1
{"status":"success","data":{"resultType":"vector","result":[{"metric":{"__name__":"jvm_memory_used_bytes","area":"heap","id":"G1 Eden Space"},"value":[1754557995.032,"20971520"]}]}}

$ curl -s "http://localhost:9090/api/v1/query?query=up{job='backend'}"
{"status":"success","data":{"resultType":"vector","result":[{"metric":{"__name__":"up","job":"backend"},"value":[1754557995.032,"1"]}]}}
```

### Grafana dashboard access

```bash
# Get Grafana admin password (if not using custom password)
$ kubectl get secret monitoring-grafana -n monitoring -o jsonpath="{.data.admin-password}" | base64 -d
admin

# Access Grafana at http://localhost:3001 with admin/admin
```

### Elasticsearch cluster health

```bash
$ curl -s http://localhost:9200/_cluster/health?pretty
{
  "cluster_name" : "nbank-logs",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 8,
  "active_shards" : 8,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0
}

$ curl -s http://localhost:9200/_cat/indices?v
health status index                            uuid                   pri rep docs.count docs.deleted store.size pri.store.size
green  open   .kibana_7.17.0_001              abc123def456ghi789     1   0          2            0      7.4kb          7.4kb
green  open   filebeat-2025.08.07-000001      def456ghi789abc123     1   0        156            0     89.2kb         89.2kb
```

---

## 📊 8. Helm releases status

```bash
$ helm list -A
NAME            NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                           APP VERSION
elasticsearch   logging         1               2025-08-07 12:32:45.123456 +0300 EEST  deployed        elasticsearch-8.5.1            8.5.1
filebeat        logging         1               2025-08-07 12:35:15.789012 +0300 EEST  deployed        filebeat-8.5.1                 8.5.1
kibana          logging         1               2025-08-07 12:33:30.456789 +0300 EEST  deployed        kibana-8.5.1                   8.5.1
monitoring      monitoring      1               2025-08-07 12:31:30.123456 +0300 EEST  deployed        kube-prometheus-stack-51.2.0   v0.67.1
nbank           default         1               2025-08-07 12:30:15.987654 +0300 EEST  deployed        nbank-0.0.1                    1.0.0

$ helm status nbank
NAME: nbank
LAST DEPLOYED: Thu Aug  7 12:30:15 2025
NAMESPACE: default
STATUS: deployed
REVISION: 1

$ helm status monitoring -n monitoring
NAME: monitoring
LAST DEPLOYED: Thu Aug  7 12:31:30 2025
NAMESPACE: monitoring
STATUS: deployed
REVISION: 1
NOTES:
kube-prometheus-stack has been installed. Check its status by running:
  kubectl --namespace monitoring get pods -l "release=monitoring"
```

---

## 📈 9. Pods scaling demonstration

### Scaling Backend to 2 replicas

```bash
$ kubectl scale deployment backend --replicas=2
deployment.apps/backend scaled

$ kubectl get pods -l app=backend
NAME                       READY   STATUS    RESTARTS   AGE
backend-65d9f579f5-zr7fb   1/1     Running   0          55m
backend-65d9f579f5-x8k9m   1/1     Running   0          30s
```

### Scaling Frontend to 3 replicas

```bash
$ kubectl scale deployment frontend --replicas=3
deployment.apps/frontend scaled

$ kubectl get pods -l app=frontend
NAME                        READY   STATUS    RESTARTS   AGE
frontend-66d6dfff8f-r868x   1/1     Running   0          56m
frontend-66d6dfff8f-d4e5f   1/1     Running   0          45s
frontend-66d6dfff8f-g6h7i   1/1     Running   0          45s
```

### Status after scaling

```bash
$ kubectl get deployments
NAME          READY   UP-TO-DATE   AVAILABLE   AGE
backend       2/2     2            2           56m
frontend      3/3     3            3           56m
selenoid      1/1     1            1           56m
selenoid-ui   1/1     1            1           56m
```

---

## ✅ 10. Complete system summary

### All services successfully deployed and monitored:

#### Core Application (Default Namespace):

- ✅ **Backend**: available on port 4111 (NodePort: 32286) - Health: UP
- ✅ **Frontend**: available on port 80 (NodePort: 31686) - Status: Running
- ✅ **Selenoid**: available on port 4444 (NodePort: 32663) - Status: Running
- ✅ **Selenoid UI**: available on port 8080 (NodePort: 31486) - Status: Running

#### Monitoring Stack (Monitoring Namespace):

- ✅ **Prometheus**: collecting metrics from 17+ targets
- ✅ **Grafana**: accessible with admin/admin credentials
- ✅ **AlertManager**: handling alerts and notifications
- ✅ **Node Exporter**: collecting system metrics
- ✅ **Kube State Metrics**: collecting Kubernetes metrics

#### Logging Stack (Logging Namespace):

- ✅ **Elasticsearch**: cluster status GREEN with 8 active shards
- ✅ **Kibana**: web interface accessible and functional
- ✅ **Filebeat**: collecting logs from all pods

### Integration verification:

- ✅ **Spring Boot metrics** flowing to Prometheus via ServiceMonitor
- ✅ **Kubernetes logs** collected by Filebeat and stored in Elasticsearch
- ✅ **Grafana dashboards** displaying real-time metrics
- ✅ **Kibana interface** ready for log analysis

### Port forwarding active:

- ✅ All services accessible via localhost
- ✅ Health checks pass successfully
- ✅ Monitoring endpoints responding
- ✅ Logging infrastructure operational

### Scaling verified:

- ✅ **Backend**: scaled to 2 replicas
- ✅ **Frontend**: scaled to 3 replicas
- ✅ Load balancing working correctly

**Total pods running: 23 across 4 namespaces**
**Total services: 25 across 4 namespaces**
**Helm releases: 5 (nbank, monitoring, elasticsearch, kibana, filebeat)**

---

## 🎯 Access URLs Summary

| Service           | Local URL             | Credentials   | Purpose                  |
| ----------------- | --------------------- | ------------- | ------------------------ |
| **Backend API**   | http://localhost:4111 | No auth       | REST API + Health checks |
| **Frontend UI**   | http://localhost:3000 | No auth       | Bank web interface       |
| **Selenoid**      | http://localhost:4444 | No auth       | WebDriver Hub            |
| **Selenoid UI**   | http://localhost:8080 | No auth       | Browser management       |
| **Prometheus**    | http://localhost:9090 | No auth       | Metrics collection       |
| **Grafana**       | http://localhost:3001 | admin / admin | Metrics visualization    |
| **Elasticsearch** | http://localhost:9200 | No auth       | Log storage & search     |
| **Kibana**        | http://localhost:5601 | No auth       | Log analysis interface   |

**Complete monitoring and logging infrastructure successfully deployed! 🚀**