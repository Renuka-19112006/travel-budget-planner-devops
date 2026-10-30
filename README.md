# Travel Budget Planner

A Spring Boot web application that suggests how to spend your travel money.

You enter only five things: **From, To, number of days, number of members and total budget**.
The app then shows a dashboard with a budget split (transport, stay, food, local travel, activities,
emergency fund), a day-by-day plan, a packing list, typical weather, plan options and money-saving tips.

Built as a DevOps case study for Continuous Integration and Continuous Delivery Using DevOps (24CR1SCO1).

**Important:** all costs, distances, weather and places are predefined SAMPLE estimates for learning.
The app does not use live travel prices, booking APIs, payments, login or a database.

## Tech stack
Java 17, Spring Boot 3.5, Maven, HTML/CSS/JavaScript, JUnit 5

## Run locally (Windows Command Prompt)
```cmd
mvn clean test
set SERVER_PORT=9090
mvn spring-boot:run
```
Open http://localhost:9090  (change the port if 9090 is already used on your computer)

## Build the executable JAR
```cmd
mvn clean package
java -jar target\travel-budget-planner.jar --server.port=9090
```

## How the plan is calculated
1. Travel cost = estimated round-trip fare per person (from the distance between the cities) x members.
2. Stay = hotel cost per room per night x rooms (2 people per room) x nights (days - 1, minimum 0).
3. Food, local travel and activities = daily cost per person x members x days.
4. 5 percent of the plan is kept as an emergency fund.
5. The standard costs are scaled between 65 percent and 160 percent so the plan fits your budget.
   Below 65 percent the app reports "Budget too low" and shows the shortfall.

## API
- GET  /api/destinations
- GET  /api/cities
- POST /api/plan   body: {"from":"Hyderabad","to":"Goa","days":4,"members":2,"budget":25000}


## Complete DevOps CI/CD setup

This project now covers all five technologies required by the case study:

1. **Git + GitHub** – source-code version control and remote repository.
2. **Maven** – build and automated JUnit testing.
3. **Jenkins** – CI/CD pipeline defined in `Jenkinsfile`.
4. **Docker** – application packaged as a container image.
5. **Kubernetes** – two application replicas deployed and exposed through a NodePort service.

### Project DevOps files

```text
Dockerfile
.dockerignore
Jenkinsfile
k8s/
  namespace.yaml
  deployment.yaml
  service.yaml
run-docker.bat
deploy-k8s.bat
```

### 1. Git and GitHub

From the project root:

```cmd
git init
git add .
git commit -m "Initial DevOps project with CI CD Docker Kubernetes"
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/travel-budget-planner.git
git push -u origin main
```

Create an empty GitHub repository named `travel-budget-planner` before the `git remote add` command.

### 2. Verify Maven

```cmd
mvn clean test
mvn clean package
```

The generated JAR is:

```text
target\travel-budget-planner.jar
```

### 3. Build and run Docker

Make sure Docker Desktop is running:

```cmd
docker build -t travel-budget-planner:latest .
docker run --name travel-budget-planner -p 8080:8080 travel-budget-planner:latest
```

Open:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/api/health
```

Stop the container:

```cmd
docker stop travel-budget-planner
docker rm travel-budget-planner
```

### 4. Enable Kubernetes

For Docker Desktop Kubernetes, enable Kubernetes in Docker Desktop and wait until it shows as running.

Verify:

```cmd
kubectl version
kubectl get nodes
```

### 5. Deploy manually to Kubernetes

First build the local image:

```cmd
docker build -t travel-budget-planner:latest .
```

Then deploy:

```cmd
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

Check:

```cmd
kubectl -n travel-budget get pods
kubectl -n travel-budget get deployments
kubectl -n travel-budget get services
```

The service uses NodePort `30080`, so with Docker Desktop Kubernetes open:

```text
http://localhost:30080
```

### 6. Jenkins CI/CD

Create a Jenkins Pipeline job.

Recommended configuration:

- **Definition:** Pipeline script from SCM
- **SCM:** Git
- **Repository URL:** your GitHub repository URL
- **Branch:** `*/main`
- **Script Path:** `Jenkinsfile`

The Jenkinsfile performs:

```text
GitHub checkout
      ↓
Maven clean test
      ↓
Maven package
      ↓
Docker image build
      ↓
Kubernetes deployment
      ↓
Rollout status check
```

The Jenkins machine must have these commands available:

```cmd
java -version
mvn -version
docker --version
kubectl version --client
```

For a Jenkins installation running as a Windows service, make sure the Jenkins service account can access Docker Desktop/Docker Engine and `kubectl`.

### 7. End-to-end workflow

After the first setup, the normal development workflow is:

```text
Edit Java/HTML/CSS/JS
        ↓
git add .
        ↓
git commit
        ↓
git push
        ↓
Jenkins detects/builds the commit
        ↓
Maven test
        ↓
Maven package
        ↓
Docker build
        ↓
Kubernetes deploy
        ↓
Application available on NodePort
```

### Kubernetes useful commands

```cmd
kubectl -n travel-budget get all
kubectl -n travel-budget logs deployment/travel-budget-planner
kubectl -n travel-budget describe deployment travel-budget-planner
kubectl -n travel-budget rollout status deployment/travel-budget-planner
kubectl -n travel-budget rollout restart deployment/travel-budget-planner
```

To remove the complete deployment:

```cmd
kubectl delete namespace travel-budget
```

### Optional Docker Hub step

Docker Hub is not required by the five tools in the case-study statement, but it can be added as an extra DevOps feature. If you want to demonstrate image publishing, tag the image with your Docker Hub username, log in with `docker login`, and push it. For Kubernetes on a remote cluster, use the Docker Hub image instead of `imagePullPolicy: Never`.
