pipeline {
    agent {label 'worker'}
    stages {
        stage('Trigger edc-federated-catalog pipeline') {
            steps {
                script {
                    build job: 'DS4H/Microservices/edc-federated-catalog', parameters: [
                    ]
                }
            }
        }
    }
}