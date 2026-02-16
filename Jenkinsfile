pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'IMAGE_REGISTRY', defaultValue: 'docker.io', description: 'Docker registry host (for Docker Hub use docker.io)')
        string(name: 'IMAGE_REPOSITORY', defaultValue: 'kolan7416/java-gradle', description: 'Repository path in the registry')
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Image tag (leave blank to use BUILD_NUMBER)')
        string(name: 'K8S_NAMESPACE', defaultValue: 'default', description: 'Kubernetes namespace')
        string(name: 'K8S_DEPLOYMENT_NAME', defaultValue: 'my-portfolio', description: 'Kubernetes deployment name')
        string(name: 'K8S_CONTAINER_NAME', defaultValue: 'javagradle', description: 'Container name inside deployment')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: true, description: 'Push image to registry')
        booleanParam(name: 'DEPLOY_TO_K8S', defaultValue: true, description: 'Deploy to Kubernetes after image build')
    }

    environment {
        // Create tag once and reuse across all stages.
        APP_IMAGE_TAG = "${params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : env.BUILD_NUMBER}"
        APP_IMAGE = "${params.IMAGE_REGISTRY}/${params.IMAGE_REPOSITORY}:${APP_IMAGE_TAG}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean test build'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${APP_IMAGE} .'
            }
        }

        stage('Push Docker Image') {
            when {
                expression { params.PUSH_IMAGE }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "${DOCKER_PASS}" | docker login ${IMAGE_REGISTRY} -u "${DOCKER_USER}" --password-stdin
                        docker push ${APP_IMAGE}
                        docker logout ${IMAGE_REGISTRY} || true
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { params.DEPLOY_TO_K8S }
            }
            steps {
                sh '''
                    kubectl apply -f minikube/deployment.yaml -n ${K8S_NAMESPACE}
                    kubectl set image deployment/${K8S_DEPLOYMENT_NAME} ${K8S_CONTAINER_NAME}=${APP_IMAGE} -n ${K8S_NAMESPACE}
                    kubectl rollout status deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE} --timeout=120s
                '''
            }
        }
    }

    post {
        success {
            echo "Build, image, and deployment flow completed. Image: ${APP_IMAGE}"
        }
        failure {
            echo 'Pipeline failed. Check stage logs for details.'
        }
    }
}
