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
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0'
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http:// 5000 "CatalogTests#addProductsToBasket"'
              }// EndExecutionStageErrorTJobC
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepsTJobC
        }// EndStageTJobC
     }// End Parallel
    }// End Stage
    stage('Stage 1'){
      failFast false
      parallel{
        stage('TJobG IdResource: catalog-api chrome-browser webmvc ') {
          steps {
              catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobg 1'
                  sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobg 1 http:// 5048 "CatalogTests#FilterProductsByBrandType"'
              }// EndExecutionStageErrorTJobG
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobg 1'
          }// EndStepsTJobG
        }// EndStageTJobG
     }// End Parallel
    }// End Stage
stage('TEARDOWN-Infrastructure') {
      failFast false
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
