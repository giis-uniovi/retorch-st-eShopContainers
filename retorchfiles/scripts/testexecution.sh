#!/bin/bash
#https://stackoverflow.com/questions/4365418/get-state-of-maven-execution-within-shell-script
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
echo 'Starting 10 seconds sleep'
sleep 10
echo 'End 10 seconds sleep'
mvn test
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
