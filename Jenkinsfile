pipeline {
  agent {label 'xretorch-agent'}
  environment {
    SELENOID_PRESENT = "TRUE"
    SUT_LOCATION = "$WORKSPACE/sut/src"
    SCRIPTS_FOLDER = "$WORKSPACE/retorchfiles/scripts"
  } // EndEnvironment
  options {
    disableConcurrentBuilds()
  } // EndPipOptions
  stages {
    stage('Clean Workspace') {
        steps {
            cleanWs()
        } // EndStepsCleanWS
    } // EndStageCleanWS
    stage('Clone Project') {
        steps {
            checkout scm
        } // EndStepsCloneProject
    } // EndStageCloneProject
    stage('SETUP-Infrastructure') {
        steps {
            sh 'chmod +x -R $SCRIPTS_FOLDER'
            sh '$SCRIPTS_FOLDER/coilifecycles/coi-setup.sh'
        } // EndStepsSETUPINF
    } // EndStageSETUPInf
    stage('Stage 0') {
      failFast false
      parallel {
        stage('tjoba IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjoba 0'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjoba 0 http:// 5000 "CatalogTests#addProductsToBasket"'
            }// EndExecutionStageErrortjoba
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjoba 0'
          }// EndStepstjoba
        }// EndStagetjoba
        stage('tjobb IdResource: catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobb 0'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobb 0 http:// 5012 "LoggedUserTest#loginTest"'
            }// EndExecutionStageErrortjobb
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobb 0'
          }// EndStepstjobb
        }// EndStagetjobb
        stage('tjobc IdResource: basket-api eshopUser identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http:// 5024 "DesktopAPIGatewayAPITests#testAddProductsBasket"'
            }// EndExecutionStageErrortjobc
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepstjobc
        }// EndStagetjobc
        stage('tjobd IdResource: basket-api catalog-api chrome-browser eshopUser identity-api ordering-api payment-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobd 0'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobd 0 http:// 5036 "OrderTests#testCancelOrder,OrderTests#testCreateNewOrder"'
            }// EndExecutionStageErrortjobd
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobd 0'
          }// EndStepstjobd
        }// EndStagetjobd
      } // End Parallel
    } // End Stage
    stage('Stage 1') {
      failFast false
      parallel {
        stage('tjobe IdResource: catalog-api chrome-browser webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobe 1'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobe 1 http:// 5048 "CatalogTests#FilterProductsByBrandType"'
            }// EndExecutionStageErrortjobe
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobe 1'
          }// EndStepstjobe
        }// EndStagetjobe
      } // End Parallel
    } // End Stage
stage('TEARDOWN-Infrastructure') {
      failFast false
      steps {
          sh '$SCRIPTS_FOLDER/coilifecycles/coi-teardown.sh'
      } // EndStepsTearDownInf
} // EndStageTearDown
} // EndStagesPipeline
post {
    always {
        archiveArtifacts artifacts: 'artifacts/*.csv', onlyIfSuccessful: true
        archiveArtifacts artifacts: 'target/testlogs/**/*.*', onlyIfSuccessful: false
        archiveArtifacts artifacts: 'target/containerlogs/**/*.*', onlyIfSuccessful: false
    }// EndAlways
} // EndPostActions
} // EndPipeline 
