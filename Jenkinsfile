pipeline {
    agent {
        label 'xretorch-agent'
    }

    environment {
        SELENOID_URL = "http://selenoid:4444/wd/hub"
        SUT_LOCATION = "$WORKSPACE/sut"
        E2ESUITE_URL = "$WORKSPACE"
        IP_SERVER="156.35.119.57"
        TJOB_NAME="tjobe"
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
         stage('Clean Workspace') {
                steps {
                cleanWs()
                }
            }
        stage('Clone eShopContainers Project') {
            steps {
                checkout scm
            }
        }

        stage('SETUP-Infrastructure') {
            steps {
                sh 'chmod +x -R "$E2ESUITE_URL/retorchfiles/scripts"'
                sh "$E2ESUITE_URL/retorchfiles/scripts/coilifecycles/coi-setup.sh $TJOB_NAME"
            }
        }

        stage('Test') {
            steps {
                sh "$E2ESUITE_URL/retorchfiles/scripts/testexecution.sh tjobA 0"

            }
        }


        stage('Tear-down Infrastructure') {
            steps {
                sh "$E2ESUITE_URL/retorchfiles/scripts/coilifecycles/coi-teardown.sh $TJOB_NAME"

            }
        }
    }

    post {
        always {
         archiveArtifacts artifacts: "artifacts/*.csv", onlyIfSuccessful: true
        }
        cleanup {

            sh """(eval \$CURRENT_DATE ; echo "Cleaning Environment ") | cat | tr '\n' ' ' """



        }
    }
}
