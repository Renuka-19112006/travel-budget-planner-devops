@echo off
echo Building Maven application...
call mvn clean test
if errorlevel 1 exit /b 1

echo Building Docker image...
docker build -t travel-budget-planner:latest .
if errorlevel 1 exit /b 1

echo Starting container on http://localhost:8080 ...
docker rm -f travel-budget-planner 2>nul
docker run --name travel-budget-planner -p 8080:8080 travel-budget-planner:latest
