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
        stage('tjoba IdResource: catalog-api chrome-browser webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjoba 0 http://webmvc_tjoba:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjoba 0 http://webmvc_tjoba:80 "CatalogTests#FilterProductsByBrandType"'
            }// EndExecutionStageErrortjoba
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjoba 0'
          }// EndStepstjoba
        }// EndStagetjoba
        stage('tjobb IdResource: basket-api eshopUser identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobb 0 http://webmvc_tjobb:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobb 0 http://webmvc_tjobb:80 "BasketAPITests#addProductsBasketAPI,BasketAPITests#addSingleBasketItemAPI,BasketAPITests#updateBasketItemQuantitiesAPI"'
            }// EndExecutionStageErrortjobb
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobb 0'
          }// EndStepstjobb
        }// EndStagetjobb
        stage('tjobc IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0 http://webmvc_tjobc:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http://webmvc_tjobc:80 "CatalogTests#addProductsToBasket"'
            }// EndExecutionStageErrortjobc
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepstjobc
        }// EndStagetjobc
        stage('tjobd IdResource: identity-api ordering-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobd 0 http://webmvc_tjobd:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobd 0 http://webmvc_tjobd:80 "OrderingAPITests#getCardTypesAPI,OrderingAPITests#getOrderByNonExistentIdAPI"'
            }// EndExecutionStageErrortjobd
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobd 0'
          }// EndStepstjobd
        }// EndStagetjobd
        stage('tjobe IdResource: basket-api eshopUser identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobe 0 http://webmvc_tjobe:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobe 0 http://webmvc_tjobe:80 "OrderingAPITests#getOrderDraftFromNonExistentBasketAPI"'
            }// EndExecutionStageErrortjobe
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobe 0'
          }// EndStepstjobe
        }// EndStagetjobe
        stage('tjobf IdResource: payment-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobf 0 http://webmvc_tjobf:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobf 0 http://webmvc_tjobf:80 "PaymentAPITests#paymentHealthCheckAPI,PaymentAPITests#paymentLivenessAPI"'
            }// EndExecutionStageErrortjobf
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobf 0'
          }// EndStepstjobf
        }// EndStagetjobf
        stage('tjobg IdResource: basket-api catalog-api chrome-browser eshopUser identity-api ordering-api payment-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobg 0 http://webmvc_tjobg:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobg 0 http://webmvc_tjobg:80 "OrderTests#testCancelOrder,OrderTests#testCreateNewOrder"'
            }// EndExecutionStageErrortjobg
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobg 0'
          }// EndStepstjobg
        }// EndStagetjobg
      } // End Parallel
    } // End Stage
    stage('Stage 1') {
      failFast false
      parallel {
        stage('tjobh IdResource: catalog-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobh 1 http://webmvc_tjobh:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobh 1 http://webmvc_tjobh:80 "CatalogAPITests#getCatalogBrandsAPI,CatalogAPITests#getCatalogItemByIdAPI,CatalogAPITests#getCatalogItemsAPI,CatalogAPITests#getCatalogItemsByNameAPI,CatalogAPITests#getCatalogItemsByTypeAndBrandAPI,CatalogAPITests#getCatalogTypesAPI"'
            }// EndExecutionStageErrortjobh
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobh 1'
          }// EndStepstjobh
        }// EndStagetjobh
        stage('tjobi IdResource: eshopUser identity-api ordering-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobi 1 http://webmvc_tjobi:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobi 1 http://webmvc_tjobi:80 "OrderingAPITests#getOrdersForUserAPI"'
            }// EndExecutionStageErrortjobi
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobi 1'
          }// EndStepstjobi
        }// EndStagetjobi
        stage('tjobj IdResource: catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobj 1 http://webmvc_tjobj:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobj 1 http://webmvc_tjobj:80 "LoggedUserTest#loginTest"'
            }// EndExecutionStageErrortjobj
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobj 1'
          }// EndStepstjobj
        }// EndStagetjobj
        stage('tjobk IdResource: eshopUser identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobk 1 http://webmvc_tjobk:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobk 1 http://webmvc_tjobk:80 "IdentityAPITests#testGetCurrentUserInfo,IdentityAPITests#testGetTokenWithValidCredentials"'
            }// EndExecutionStageErrortjobk
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobk 1'
          }// EndStepstjobk
        }// EndStagetjobk
        stage('tjobl IdResource: identity-api ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobl 1 http://webmvc_tjobl:80'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobl 1 http://webmvc_tjobl:80 "IdentityAPITests#testGetOpenIdConfiguration"'
            }// EndExecutionStageErrortjobl
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobl 1'
          }// EndStepstjobl
        }// EndStagetjobl
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
