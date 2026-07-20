pipeline {
    agent any

    stages {
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