pipeline {
    agent any

    tools {
        maven 'Maven_3_9_16'
        jdk 'JDK_26'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('client-resource-access-api') {
                    script {
                        if (isUnix()) {
                            sh "mvn -B clean compile"
                        } else {
                            bat "mvn -B clean compile"
                        }
                    }
                }
            }
        }

        stage('Unit Test') {
            steps {
                dir('client-resource-access-api') {
                    script {
                        if (isUnix()) {
                            sh "mvn -B test"
                        } else {
                            bat "mvn -B test"
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                dir('client-resource-access-api') {
                    script {
                        if (isUnix()) {
                            sh "mvn -B package -DskipTests"
                        } else {
                            bat "mvn -B package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Verify Build') {
            steps {
                dir('client-resource-access-api') {
                    script {
                        env.SKIP_GATLING = (env.BRANCH_NAME == 'main') ? 'false' : 'true'
                        env.SKIP_E2E = (env.BRANCH_NAME == 'main') ? 'false' : 'true'

                        echo "Branch: ${env.BRANCH_NAME}"
                        echo "Skip Gatling: ${env.SKIP_GATLING}"
                        echo "Skip E2E: ${env.SKIP_E2E}"

                        if (isUnix()) {
                            sh "mvn -B verify -Pqatest -DskipGatling=${env.SKIP_GATLING} -DskipE2E=${env.SKIP_E2E}"
                        } else {
                            bat "mvn -B verify -Pqatest -DskipGatling=${env.SKIP_GATLING} -DskipE2E=${env.SKIP_E2E}"
                        }
                    }
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts(
                    artifacts: '**/target/client-resource-access-api-*.jar',
                    fingerprint: true
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('client-resource-access-api') {
                    script {
                        def imageName = "client-resource-access-api:${env.BUILD_NUMBER}"

                        if (isUnix()) {
                            sh "docker build -t ${imageName} ."
                        } else {
                            bat "docker build -t ${imageName} ."
                        }

                        echo "Built Docker image: ${imageName}"
                    }
                }
            }
        }

        stage('List Docker Images') {
            steps {
                script {
                    if (isUnix()) {
                        sh "docker images"
                    } else {
                        bat "docker images"
                    }
                }
            }
        }

        stage('Docker Smoke Test') {
            steps {
                script {

                    bat '''
                    docker rm -f client-resource-access-api-test || exit 0

                    docker run -d ^
                      --name client-resource-access-api-test ^
                      -p 8181:8181 ^
                      client-resource-access-api:3
                    '''

                    echo "Waiting for Spring Boot startup..."

                    sleep(time: 30, unit: 'SECONDS')

                    bat '''
                    curl http://localhost:8181/actuator/health
                    '''
                }
            }

            post {
                always {
                    bat '''
                    docker rm -f client-resource-access-api-test || exit 0
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Publishing test reports...'

            junit(
                allowEmptyResults: true,
                testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'
            )

            archiveArtifacts(
                allowEmptyArchive: true,
                artifacts: '**/target/gatling/**'
            )

            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java',
                exclusionPattern: '**/model/**, **/dto/**',

                minimumLineCoverage: '80',
                minimumClassCoverage: '80',
                minimumMethodCoverage: '80',
                minimumBranchCoverage: '70',
                minimumComplexityCoverage: '70',
                minimumInstructionCoverage: '80'
            )

            publishHTML([
                allowMissing: true,
                keepAll: true,
                alwaysLinkToLastBuild: true,
                reportDir: 'client-resource-access-api/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
        }

        success {
            echo 'BUILD SUCCESS'
        }

        failure {
            echo 'BUILD FAILED'
        }
    }
}