#!/bin/bash
#https://stackoverflow.com/questions/4365418/get-state-of-maven-execution-within-shell-script
TJOB_NAME="$1"
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
mvn test -Ddirtarget=TJOBExample -Dtjob_name=tjobe -DSUT_URL=webmvc-$TJOB_NAME
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
