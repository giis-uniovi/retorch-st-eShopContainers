#!/bin/bash
#https://stackoverflow.com/questions/4365418/get-state-of-maven-execution-within-shell-script
TJOB_NAME="$1"
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
export DOCKER_HOST_IP=$(/sbin/ip route|awk '/default/ { print $3 }')
echo "The HOST_IP is: $DOCKER_HOST_IP"

mvn test -Ddirtarget=TJOBExample -Dtjob_name=tjobe -DSUT_URL=$DOCKER_HOST_IP:5100
$E2ESUITE_URL/retorchfiles/scripts/writetime.sh "$2" "$1" #$2 is the stage, $1 tjob
