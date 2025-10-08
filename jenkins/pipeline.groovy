pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "${env.JOB_NAME.tokenize('/').last().toLowerCase()}"
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        IMAGE_NAME = "${DOCKER_IMAGE}:${DOCKER_TAG}"

        SONAR_HOST_URL = 'https://management.local/sonar'
        SONAR_LOGIN = credentials('sonarqube-token')

        GRADLEW = "./gradlew"
    }

    parameters {
        booleanParam(name: 'PUBLISH_IMAGE', defaultValue: false, description: 'Publish Docker image after build')
    }

    stages {
        stage('Build Test Docker Image') {
            steps {
                sh "docker build --target builder -t ${IMAGE_NAME} ."
            }
        }
        
        stage('Linting') {
            steps {
                sh """
                    # docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} chmod +x ${GRADLEW} && ${GRADLEW} checkstyleMain checkstyleTest
                """
            }
        }
        
        stage('Run Tests') {
            steps {
                sh """
                    docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} chmod +x ${GRADLEW} && ${GRADLEW} clean test
                """
            }
            post {
                always {
                    junit testResults: 'build/test-results/**/*.xml', allowEmptyResults: true
                }
            }
        }

        stage("Run Code Quality Check (SonarQube)") {
            steps {
                withSonarQubeEnv('sonar') {
                    sh """
                        docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} ${GRADLEW} sonarqube \
                            -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_LOGIN}
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage("Run Security Scan (Trivy)") {
            steps {
                sh """
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest \
                        image --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_NAME}
                """
            }
        }

        stage('Publish Docker Image') {
            when {
                expression { return params.PUBLISH_IMAGE == true }
            }
            steps {
                // Replace with your internal/private registry push command
                sh """
                    echo "Pushing to private registry..."
                    docker push ${IMAGE_NAME}
                """
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                docker rmi -f run ${IMAGE_NAME}
            """
        }
        failure {
            script {
                // Get the email of the last commit author
                def committerEmail = sh (
                    script: "git log -1 --pretty=format:'%ae'",
                    returnStdout: true
                ).trim()

                // Send email to committer email
                mail to: committerEmail,
                    subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """Hello,

The Jenkins build ${env.JOB_NAME} #${env.BUILD_NUMBER} has failed.

Please check the build logs here: ${env.BUILD_URL}

Regards,
Jenkins"""
            }
        }
    }
}
