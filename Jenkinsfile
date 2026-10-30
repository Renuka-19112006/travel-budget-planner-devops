pipeline {
    agent any

    environment {
        IMAGE_NAME = 'travel-budget-planner'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn -B clean test'
            }
        }

        stage('Package JAR') {
            steps {
                bat 'mvn -B package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%IMAGE_TAG% .'
                bat 'docker tag %IMAGE_NAME%:%IMAGE_TAG% %IMAGE_NAME%:latest'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f k8s/namespace.yaml'
                bat 'kubectl apply -f k8s/deployment.yaml'
                bat 'kubectl apply -f k8s/service.yaml'
                bat 'kubectl -n travel-budget set image deployment/travel-budget-planner travel-budget-planner=%IMAGE_NAME%:%IMAGE_TAG%'
                bat 'kubectl -n travel-budget rollout status deployment/travel-budget-planner --timeout=180s'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo 'CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'CI/CD pipeline failed. Check the stage logs above.'
        }
    }
}
