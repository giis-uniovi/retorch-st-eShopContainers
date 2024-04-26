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

# Custom Set-up commands
"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Executing custom commands"

tjobname="$1"
CONFIG_DIR="$SUT_LOCATION/ApiGateways/Envoy/config"
TMP_DIR="$SUT_LOCATION/tmp/$tjobname"

mkdir -p "$TMP_DIR/mobileshopping"
mkdir -p "$TMP_DIR/webshopping"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Copying the envoy files to the tmp directories for tjob $1"

cp -p "$CONFIG_DIR/mobileshopping/envoy.yaml" "$TMP_DIR/mobileshopping/"
cp -p "$CONFIG_DIR/webshopping/envoy.yaml" "$TMP_DIR/webshopping/"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Reemplacing the TJob name in the envoy conf files"
sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/mobileshopping/envoy.yaml"
sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/webshopping/envoy.yaml"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Changing the permissions for tjob $1"
chmod go+r "$TMP_DIR/mobileshopping/envoy.yaml"
chmod go+r "$TMP_DIR/webshopping/envoy.yaml"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "End custom commands"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Some sleep prior the deployment"

ls -la
# Deploy containers
cd "$SUT_LOCATION"
"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Deploying containers for TJOB $1"
docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "Waiting for the system to be up..."
"$WORKSPACE/retorchfiles/scripts/waitforSUT.sh" "$1"
cd "$WORKSPACE"

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "$1-set-up" "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
