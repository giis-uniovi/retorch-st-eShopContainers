#!/bin/bash
#https://stackoverflow.com/questions/4365418/get-state-of-maven-execution-within-shell-script
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
mvn test -DSUT_URL="156.35.119.57" -DSUT_PORT=5100 -Ddirtarget=TJOBExample -Dtjob_name=tjobe
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
