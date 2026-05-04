pipeline {
  agent {label 'xretorch-agent'}
  environment {
    SELENOID_PRESENT = "TRUE"
    SUT_LOCATION = "$WORKSPACE/sut/src"
    SCRIPTS_FOLDER = "$WORKSPACE/.retorch/scripts"
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
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjoba 0 http://webmvc_tjoba:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjoba 0 http://webmvc_tjoba:80 "WebMVCCatalogTests#addProductsToBasketMVC"'
            }// EndExecutionStageErrortjoba
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjoba 0'
          }// EndStepstjoba
        }// EndStagetjoba
        stage('tjobb IdResource: basket-api eshopUser identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobb 0 http://webmvc_tjobb:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobb 0 http://webmvc_tjobb:80 "DesktopAPIGatewayAPITests#testAddProductsBasket"'
            }// EndExecutionStageErrortjobb
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobb 0'
          }// EndStepstjobb
        }// EndStagetjobb
        stage('tjobc IdResource: basket-api catalog-api chrome-browser eshopUser identity-api ordering-api payment-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0 http://webmvc_tjobc:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http://webmvc_tjobc:80 "WebMVCOrderTests#testCancelOrderMVC,WebMVCOrderTests#testCreateNewOrderMVC"'
            }// EndExecutionStageErrortjobc
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepstjobc
        }// EndStagetjobc
        stage('tjobd IdResource: basket-api catalog-api chrome-browser eshopUser identity-api ordering-api payment-api webspa ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobd 0 http://webmvc_tjobd:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobd 0 http://webmvc_tjobd:80 "WebSPAOrderTests#testCancelOrderSPA,WebSPAOrderTests#testCreateNewOrderSPA"'
            }// EndExecutionStageErrortjobd
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobd 0'
          }// EndStepstjobd
        }// EndStagetjobd
      } // End Parallel
    } // End Stage
    stage('Stage 1') {
      failFast false
      parallel {
        stage('tjobe IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webspa ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobe 1 http://webmvc_tjobe:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobe 1 http://webmvc_tjobe:80 "WebSPACatalogTests#addProductsToBasketSPA"'
            }// EndExecutionStageErrortjobe
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobe 1'
          }// EndStepstjobe
        }// EndStagetjobe
        stage('tjobf IdResource: catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobf 1 http://webmvc_tjobf:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobf 1 http://webmvc_tjobf:80 "WebMVCLoggedUserTest#loginTestMVC"'
            }// EndExecutionStageErrortjobf
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobf 1'
          }// EndStepstjobf
        }// EndStagetjobf
        stage('tjobg IdResource: catalog-api chrome-browser eshopUser identity-api webspa ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobg 1 http://webmvc_tjobg:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobg 1 http://webmvc_tjobg:80 "WebSPALoggedUserTest#loginTestSPA"'
            }// EndExecutionStageErrortjobg
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobg 1'
          }// EndStepstjobg
        }// EndStagetjobg
      } // End Parallel
    } // End Stage
    stage('Stage 2') {
      failFast false
      parallel {
        stage('tjobh IdResource: catalog-api chrome-browser webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobh 2 http://webmvc_tjobh:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobh 2 http://webmvc_tjobh:80 "WebMVCCatalogTests#FilterProductsByBrandTypeMVC"'
            }// EndExecutionStageErrortjobh
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobh 2'
          }// EndStepstjobh
        }// EndStagetjobh
        stage('tjobi IdResource: catalog-api chrome-browser webspa ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobi 2 http://webmvc_tjobi:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobi 2 http://webmvc_tjobi:80 "WebSPACatalogTests#filterProductsByBrandTypeSPA"'
            }// EndExecutionStageErrortjobi
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobi 2'
          }// EndStepstjobi
        }// EndStagetjobi
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
