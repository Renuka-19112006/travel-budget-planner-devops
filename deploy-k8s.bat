@echo off
echo Applying Kubernetes resources...
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

echo Waiting for rollout...
kubectl -n travel-budget rollout status deployment/travel-budget-planner --timeout=180s

echo.
echo Application URL (Docker Desktop Kubernetes):
echo http://localhost:30080
echo.
kubectl -n travel-budget get pods
kubectl -n travel-budget get service
