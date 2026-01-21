#!/bin/bash

echo "🎯 Demonstration of pods scaling in Kubernetes"
echo "==============================================="

echo "📊 Current pods status:"
kubectl get pods -o wide

echo ""
echo "🔄 Scaling Backend to 2 replicas..."
kubectl scale deployment backend --replicas=2

echo "⏳ Waiting for new pods readiness..."
kubectl wait --for=condition=Ready pods -l app=backend --timeout=60s

echo ""
echo "📊 Status after Backend scaling:"
kubectl get pods -l app=backend

echo ""
echo "🔄 Scaling Frontend to 3 replicas..."
kubectl scale deployment frontend --replicas=3

echo "⏳ Waiting for new pods readiness..."
kubectl wait --for=condition=Ready pods -l app=frontend --timeout=60s

echo ""
echo "📊 Status after Frontend scaling:"
kubectl get pods -l app=frontend

echo ""
echo "📊 All pods after scaling:"
kubectl get pods -o wide

echo ""
echo "🔍 Detailed information about Deployments:"
kubectl get deployments

echo ""
echo "📈 Resource usage statistics:"
kubectl top pods 2>/dev/null || echo "Metrics server is not configured"

echo ""
echo "🎯 Demonstration of scale down..."
echo "Returning to 1 replica for resource saving:"

kubectl scale deployment backend --replicas=1
kubectl scale deployment frontend --replicas=1

echo "⏳ Waiting for scale down completion..."
sleep 10

echo ""
echo "📊 Final pods status:"
kubectl get pods -o wide

echo ""
echo "✅ Scaling demonstration completed!"
echo ""
echo "🔧 Useful commands for scaling:"
echo "kubectl scale deployment <name> --replicas=<number>"
echo "kubectl autoscale deployment <name> --cpu-percent=50 --min=1 --max=5"
echo "kubectl get hpa"
echo "kubectl describe hpa <name>"