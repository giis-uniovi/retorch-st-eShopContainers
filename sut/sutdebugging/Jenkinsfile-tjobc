pipeline {
  agent {label 'xretorch-agent'}
  environment {
    SELENOID_PRESENT = "TRUE"
    SUT_LOCATION = "$WORKSPACE/sut/src"
    SCRIPTS_FOLDER = "$WORKSPACE/retorchfiles/scripts"
  }// EndEnvironment
  options {
    disableConcurrentBuilds()
  }// EndPipOptions
  stages{
    stage('Clean Workspace') {
        steps{
            cleanWs()
        }// EndStepsCleanWS
      }// EndStageCleanWS
    stage('Clone Project') {
        steps{
            checkout scm
        }// EndStepsCloneProject
      }// EndStageCloneProject
    stage('SETUP-Infrastructure') {
        steps{
            sh 'chmod +x -R $SCRIPTS_FOLDER'
            sh '$SCRIPTS_FOLDER/coilifecycles/coi-setup.sh'
        }// EndStepsSETUPINF
      }// EndStageSETUPInf
    stage('Stage 0'){
      failFast false
      parallel{
        stage('TJobC IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http:// 5000 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrorTJobC
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepsTJobC
        }// EndStageTJobC
        stage('tjobc1 IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc1 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc1 0 http:// 5020 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrortjobc1
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc1 0'
          }// EndStepstjobc1
        }// EndStagetjobc1
        stage('tjobc2 IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc2 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc2 0 http:// 5040 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrortjobc2
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc2 0'
          }// EndStepstjobc2
        }// EndStagetjobc2

        stage('tjobc3 IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc3 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc3 0 http:// 5090 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrortjobc3
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc3 0'
          }// EndStepstjobc3
        }// EndStagetjobc3

        stage('tjobc4 IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc4 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc4 0 http:// 5100 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrortjobc4
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc4 0'
          }// EndStepstjobc4
        }// EndStagetjobc4
        stage('tjobc5 IdResource: basket-api catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc5 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc5 0 http:// 5200 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrortjobc5
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc5 0'
          }// EndStepstjobc5
        }// EndStagetjobc5

     }// End Parallel
    }// End Stage
    stage('Stage 1'){
      failFast false
      parallel{
        stage('TJobF IdResource: catalog-api chrome-browser webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobf 1'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobf 1 http:// 5036 "CatalogTests#FilterProductsByBrandType"'
              }// EndExecutionStageErrorTJobF
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobf 1'
          }// EndStepsTJobF
        }// EndStageTJobF
     }// End Parallel
    }// End Stage
stage('TEARDOWN-Infrastructure') {
      steps {
        sh '$SCRIPTS_FOLDER/coilifecycles/coi-teardown.sh'
      }// EndStepsTearDownInf
}// EndStageTearDown
  }// EndStagesPipeline
 post { 
      always {
          archiveArtifacts artifacts: 'artifacts/*.csv', onlyIfSuccessful: true
          archiveArtifacts artifacts: 'target/testlogs/**/*.*', onlyIfSuccessful: false
          archiveArtifacts artifacts: 'target/containerlogs/**/*.*', onlyIfSuccessful: false
      }//EndAlways
 }//EndPostActions
}// EndPipeline 
