# Case Study: End-to-End CI/CD Pipeline for a Web Application

## 1. Title

**Travel Budget Planner – End-to-End CI/CD Pipeline using Git, GitHub, Maven, Jenkins, Docker and Kubernetes**

## 2. Objective

To develop a web application and implement a CI/CD pipeline that automates source-code management, build, testing, containerization, deployment and orchestration.

## 3. Problem Statement

Manual software delivery requires developers to build, test, package and deploy an application separately. This can lead to errors, inconsistent environments and slow releases. The Travel Budget Planner is therefore integrated with DevOps tools so that a code change can move through an automated delivery pipeline.

## 4. Application Overview

The Travel Budget Planner is a Spring Boot web application. A user enters:

- Starting city
- Destination
- Number of days
- Number of members
- Total budget

The application calculates an estimated travel budget and displays transport, accommodation, food, local travel, activities, emergency fund, itinerary, packing list, weather information and money-saving tips.

The application uses sample data for academic demonstration and does not use live booking or payment services.

## 5. Technologies Used

| Technology | Purpose |
|---|---|
| Java 17 | Application development |
| Spring Boot | Web application framework |
| HTML/CSS/JavaScript | Frontend |
| JUnit 5 | Automated testing |
| Git | Version control |
| GitHub | Remote source-code repository |
| Maven | Build and dependency management |
| Jenkins | CI/CD automation |
| Docker | Application containerization |
| Kubernetes | Container orchestration |

## 6. CI/CD Architecture

```text
Developer
   |
   | git add / commit / push
   v
GitHub Repository
   |
   | Jenkins Pipeline
   v
+----------------------+
| Jenkins               |
|                      |
| 1. Checkout          |
| 2. Maven Test        |
| 3. Maven Package     |
| 4. Docker Build      |
| 5. Kubernetes Deploy |
+----------------------+
        |
        v
Docker Image
        |
        v
Kubernetes Cluster
        |
        +---- Pod 1
        |
        +---- Pod 2
        |
        v
NodePort Service :30080
        |
        v
Travel Budget Planner
```

## 7. Version Control using Git and GitHub

The project source code is maintained using Git.

### Commands

```cmd
git init
git add .
git commit -m "Initial DevOps project with CI CD Docker Kubernetes"
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/travel-budget-planner.git
git push -u origin main
```

The GitHub repository acts as the central remote repository from which Jenkins obtains the project source code.

**Screenshot to include:** GitHub repository showing the project files.

## 8. Build and Test using Maven

Maven manages dependencies and builds the Spring Boot application.

### Test

```cmd
mvn clean test
```

### Package

```cmd
mvn clean package
```

The generated executable JAR is:

```text
target/travel-budget-planner.jar
```

The project contains JUnit tests for the planner service, REST controller and health endpoint.

**Screenshot to include:** Command Prompt showing `BUILD SUCCESS`.

## 9. Containerization using Docker

The Dockerfile uses a multi-stage build.

### Build image

```cmd
docker build -t travel-budget-planner:latest .
```

### Run container

```cmd
docker run --name travel-budget-planner -p 8080:8080 travel-budget-planner:latest
```

The application is then available at:

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/api/health
```

### Why Docker is used

Docker packages the application and its runtime into a portable container. The same container image can be used for testing and deployment.

**Screenshot to include:** Docker image list and running container.

## 10. Continuous Integration and Continuous Delivery using Jenkins

The `Jenkinsfile` contains the pipeline.

### Pipeline stages

1. Checkout source code from GitHub.
2. Run Maven tests.
3. Package the Spring Boot JAR.
4. Build the Docker image.
5. Apply Kubernetes configuration.
6. Update the Kubernetes deployment image.
7. Wait for the Kubernetes rollout to complete.

### Pipeline flow

```text
Checkout
   ↓
Maven Clean Test
   ↓
Maven Package
   ↓
Docker Build
   ↓
Kubernetes Apply
   ↓
Set New Image
   ↓
Rollout Status
```

**Screenshot to include:** Jenkins job console showing successful stages.

## 11. Kubernetes Deployment

Kubernetes manages the Dockerized application.

The project contains:

```text
k8s/
├── namespace.yaml
├── deployment.yaml
└── service.yaml
```

### Namespace

The application is deployed into:

```text
travel-budget
```

### Deployment

The deployment runs:

```text
2 replicas
```

This demonstrates basic availability and orchestration.

### Service

A NodePort service exposes the application on:

```text
30080
```

For Docker Desktop Kubernetes:

```text
http://localhost:30080
```

### Kubernetes commands

```cmd
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

Check the deployment:

```cmd
kubectl -n travel-budget get pods
kubectl -n travel-budget get deployments
kubectl -n travel-budget get services
```

Check rollout:

```cmd
kubectl -n travel-budget rollout status deployment/travel-budget-planner
```

**Screenshot to include:** `kubectl get pods` showing two running pods.

## 12. Health Monitoring

A simple health endpoint was added:

```text
GET /api/health
```

Expected response:

```json
{
  "status": "UP",
  "application": "travel-budget-planner"
}
```

Kubernetes uses this endpoint for readiness and liveness probes.

This means Kubernetes can check whether the application is ready to receive traffic and whether the application is still running correctly.

## 13. End-to-End Working

The complete workflow is:

```text
Developer changes code
        ↓
Git commit
        ↓
GitHub push
        ↓
Jenkins checkout
        ↓
Maven build and test
        ↓
Docker image creation
        ↓
Kubernetes deployment
        ↓
Two application pods
        ↓
NodePort service
        ↓
User accesses web application
```

## 14. Advantages

- Automated build and testing
- Consistent application packaging
- Faster deployment
- Reduced manual errors
- Version-controlled source code
- Container portability
- Kubernetes-based scaling and orchestration
- Health checks for deployed containers
- Repeatable CI/CD process

## 15. Conclusion

The Travel Budget Planner demonstrates an end-to-end DevOps workflow. Git and GitHub provide version control, Maven performs application building and testing, Jenkins automates the CI/CD pipeline, Docker packages the application into a container, and Kubernetes deploys and manages the containerized application.

The project therefore satisfies the required technologies for the CI/CD case study and demonstrates how a source-code change can move from development to a running application through an automated pipeline.

## 16. Suggested Screenshots for the Final Report

Capture these screenshots after completing the setup:

1. Project folder showing `Dockerfile`, `Jenkinsfile` and `k8s`.
2. GitHub repository.
3. `mvn clean test` with `BUILD SUCCESS`.
4. `docker images`.
5. `docker ps`.
6. Application running in the browser.
7. Jenkins pipeline stage view.
8. Jenkins console output showing successful build.
9. `kubectl get pods`.
10. `kubectl get service`.
11. Application running through Kubernetes at `localhost:30080`.
12. Kubernetes health endpoint returning `UP`.
