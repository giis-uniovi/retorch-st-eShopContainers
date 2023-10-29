pipeline {
    agent {
        label 'xretorch-agent'
    }

    environment {
        ET_EUS_API = "http://selenoid:4444/wd/hub"
        SUT_URL = "$WORKSPACE/sut"
        E2ESUITE_URL = "$WORKSPACE"
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Clone eShopContainers Project') {
            steps {
                checkout scm
            }
        }

        stage('SETUP-Infrastructure') {
            steps {
                sh 'chmod +x -R "$E2ESUITE_URL/retorchfiles/scripts"'
                sh '$E2ESUITE_URL/retorchfiles/scripts/coilifecycles/coi-setup.sh'
            }
        }

        stage('Test') {
            steps {
                sh '$E2ESUITE_URL/retorchfiles/scripts/testexecution.sh tjobA 0'
            }
        }
    }

    post {
        always {
         archiveArtifacts artifacts: 'eshopcontainers-e2etestsuite/artifacts/*.csv', onlyIfSuccessful: true
        }
        cleanup {
             cleanWs(cleanWhenNotBuilt: false,
                                deleteDirs: true,
                                disableDeferredWipeout: true,
                                notFailBuild: true,
                                patterns: [[pattern: '.gitignore', type: 'INCLUDE'],
                                           [pattern: '.propsfile', type: 'EXCLUDE']])
            sh """(eval \$CURRENT_DATE ; echo "Cleaning Environment ") | cat | tr '\n' ' ' """
            sh '$E2ESUITE_URL/retorchfiles/scripts/coilifecycles/coi-teardown.sh'

        }
    }
}
