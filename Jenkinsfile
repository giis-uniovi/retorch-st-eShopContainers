pipeline {
    agent {
        label 'xretorch-agent'
    }

    environment {
        SELENOID_PRESENT = "TRUE"
        SUT_LOCATION = "$WORKSPACE/sut"
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

        stage('COI-Set-UP') {
            steps {
                script {
                    sh 'chmod +x -R "$WORKSPACE/retorchfiles/scripts"'
                    sh "$WORKSPACE/retorchfiles/scripts/coilifecycles/coi-setup.sh"
                }
            }
        }
        stage('Build eShopContainers') {
            steps {
                script {
                    sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/build-eShopContainers.sh"
                }
            }
        }
        stage('Stage 0') {
            failFast false
            parallel {
                stage('TJobA') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-setup.sh tjobeshopa 0"
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-textexecution.sh tjobeshopa 0 5028 \"CatalogTests#FilterProductsByBrandType,LoggedUserTest#loginTest\""
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-teardown.sh tjobeshopa 0"
                            }
                        }
                    }
                }

                stage('TJobB') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-setup.sh tjobeshopb 0"
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-textexecution.sh tjobeshopb 0 5009 \"CatalogTests#addProductsToBasket\""
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-teardown.sh tjobeshopb 0"
                            }
                        }
                    }
                }
                stage('TJobC') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-setup.sh tjobeshopc 0"
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-textexecution.sh tjobeshopc 0 5049 \"OrderTests#testCreateNewOrder\""
                                sh "$WORKSPACE/retorchfiles/scripts/tjoblifecycles/tjob-teardown.sh tjobeshopc 0"
                            }
                        }
                    }
                }
            }
        }

        stage('COI-Tear-down') {
            steps {
                script {
                    sh "$WORKSPACE/retorchfiles/scripts/coilifecycles/coi-teardown.sh"
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: "artifacts/*.csv", onlyIfSuccessful: true
        }
        cleanup {
            script {
                def currentDate = sh(script: 'date', returnStdout: true).trim()
                echo "Cleaning Environment $currentDate"
            }
        }
    }
}
