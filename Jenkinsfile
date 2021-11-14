pipeline {
    agent any

    environment {
        EMAIL_ADDRESS = 'anfro2520@gmail.com'
        EMAIL_PASSWORD = 'lsrwminqphyishoa'
        EXPIRE_LENGTH_IN_MILLISECONDS = '1209600000'
        EMOTIE_DOMAIN = 'localhost:8080'
        EMOTIE_SECRET_KEY = 'emotie'
    }

    stages {
        stage('Test') {
            steps {
                checkout scm
                sh './gradlew test'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
