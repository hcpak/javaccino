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
                // gradlew test
                checkout scm
                // sh './gradlew clean test'
            }
        }
        stage('Build') {
            steps {
                echo hello
                // gradlew build
                // sh './gradlew build'
            }
        }
        stage('Deploy') {
            steps {
                echo hello
                // sh 'docker build -t poolchm/javaccin:v0.0.8 .'
                // docker push
            }
        }
    }
}


// 1. webhook  집에서 해보기
// 2. jenkinsfile 3 단계 완성하기 
// 3. 환경변수 jenkins에다가 넣기