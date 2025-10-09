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
        stage('Build') {
            steps {
                sh """
                    docker build --target builder -t ${IMAGE_NAME} .
                    docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} \
                        chmod +x ${GRADLEW} && ${GRADLEW} shadowJar --no-daemon -x test
                """
            }
        }
        
        stage('Lint') {
            steps {
                sh """
                    docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} \
                        chmod +x ${GRADLEW} && ${GRADLEW} checkstyleMain checkstyleTest
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: 'build/reports/checkstyle/*.html', allowEmptyArchive: true
                }
            }
        }
        
        stage('Test') {
            steps {
                sh """
                    docker run --rm -v \$PWD:/workspace -w /workspace ${IMAGE_NAME} \
                        chmod +x ${GRADLEW} && ${GRADLEW} test
                """
            }
            post {
                always {
                    junit testResults: 'build/test-results/**/*.xml', allowEmptyResults: true
                }
            }
        }

        stage("Code Quality Check (SonarQube)") {
            steps {
                withSonarQubeEnv('sonar') {
                    script {
                        def scannerHome = tool "sonar"
                        sh """
                            ${scannerHome}/bin/sonar-scanner -X \
                                -Dsonar.projectKey=${DOCKER_IMAGE} \
                                -Dsonar.sources=. \
                                -Dsonar.java.binaries=extensions/**/build/classes/java/main
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage("Security Scan (Trivy)") {
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
                // def committerEmail = sh (
                //     script: "git log -1 --pretty=format:'%ae'",
                //     returnStdout: true
                // ).trim()

                // Send email to committer email
                // mail to: committerEmail,
                mail to: "${env.CHANGE_AUTHOR_EMAIL}",
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
