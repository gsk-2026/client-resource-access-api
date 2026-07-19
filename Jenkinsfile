pipeline {
    echo 'Java-api pipeline...'

    agent any

    tools {
        maven 'Maven_3_9_16'
        jdk 'JDK_26'
    }

    environment {
        MVN_CMD = "${isUnix() ? 'mvn' : 'mvn.cmd'}"
    }

    stages {
        stage('Build & Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh "${MVN_CMD} clean validate compile test package verify"
                    } else {
                        bat "${MVN_CMD} clean validate compile test package verify"
                    }
                }
            }
        }
    }
}