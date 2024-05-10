#!/bin/bash
set -e
# Execute the script to write timestamp
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"

# Export Docker Host IP
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
export DOCKER_HOST_IP
"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Exporting the HOST_IP: $DOCKER_HOST_IP"

# Custom Set-up commands
"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Executing custom commands"


# Deploy containers
cd "$SUT_LOCATION"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Building proxy images for TJOB $1"

docker compose -f "docker-compose.yml" --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never build webshoppingapigw mobileshoppingapigw

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "End custom commands"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Deploying containers for TJOB $1"

docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Waiting for the system to be up..."

"$WORKSPACE/retorchfiles/scripts/waitforSUT.sh" "$1"


"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
