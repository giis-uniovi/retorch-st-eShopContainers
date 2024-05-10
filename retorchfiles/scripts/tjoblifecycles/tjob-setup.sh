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

copy_and_replace_envoy_configs() {
    local tjobname="$1"
    mkdir -p "$SUT_LOCATION/tmp/$tjobname/mobileshopping/"
    mkdir -p "$SUT_LOCATION/tmp/$tjobname/webshopping/"

    "$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Copying the envoy files to the tmp directories for tjob $1"
    cp -p "$SUT_LOCATION/ApiGateways/Envoy/config/mobileshopping/envoy.yaml" "$SUT_LOCATION/tmp/$tjobname/mobileshopping/"
    cp -p "$SUT_LOCATION/ApiGateways/Envoy/config/webshopping/envoy.yaml" "$SUT_LOCATION/tmp/$tjobname/webshopping/"

    "$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Reemplacing the TJob name in the envoy conf files"
    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/tmp/$tjobname/mobileshopping/envoy.yaml"
    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/tmp/$tjobname/webshopping/envoy.yaml"
}

copy_and_replace_envoy_configs "$1"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "End custom commands"

# Deploy containers
cd "$SUT_LOCATION"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Deploying containers for TJOB $1"

docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Waiting for the system to be up..."

"$WORKSPACE/retorchfiles/scripts/waitforSUT.sh" "$1"


"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
