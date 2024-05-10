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
        stage('TJobD IdResource: catalog-api chrome-browser eshopUser identity-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobd 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobd 0 http:// 5012 "LoggedUserTest#loginTest"'
              }// EndExecutionStageErrorTJobD
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobd 0'
          }// EndStepsTJobD
        }// EndStageTJobD
        stage('TJobE IdResource: basket-api catalog-api chrome-browser eshopUser identity-api ordering-api payment-api webmvc ') {
          steps {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobe 0'
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobe 0 http:// 5024 "OrderTests#testCancelOrder,OrderTests#testCreateNewOrder"'
              }// EndExecutionStageErrorTJobE
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobe 0'
          }// EndStepsTJobE
        }// EndStageTJobE
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