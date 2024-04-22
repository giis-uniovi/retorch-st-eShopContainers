#!/bin/bash
set -e

# Function to log messages
log_message() {
  echo "[$(date +"%Y-%m-%d %T")] [INFO] - $1"
}

# Execute the script to write timestamp
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"

# Export Docker Host IP
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
export DOCKER_HOST_IP
log_message "Exporting the HOST_IP: $DOCKER_HOST_IP"

# Custom Set-up commands
log_message "Executing custom commands"

tjobname="$1"
CONFIG_DIR="$SUT_LOCATION/ApiGateways/Envoy/config"
TMP_DIR="$SUT_LOCATION/tmp/$tjobname"
if [ -d "$TMP_DIR" ]; then
  log_message "Directory /tmp for the tjob $tjobname exist, removing...."
  rm -r "$TMP_DIR"
else
  log_message "Directory /tmp for the tjob $tjobname does not exist."
fi

mkdir -p "$TMP_DIR/mobileshopping"
mkdir -p "$TMP_DIR/webshopping"

log_message "Copying the envoy files to the tmp directories for tjob $1"

cp -p "$CONFIG_DIR/mobileshopping/envoy.yaml" "$TMP_DIR/mobileshopping/"
cp -p "$CONFIG_DIR/webshopping/envoy.yaml" "$TMP_DIR/webshopping/"

log_message "Reemplacing the TJob name in the envoy conf files"
sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/mobileshopping/envoy.yaml"
sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/webshopping/envoy.yaml"

log_message "Changing the permissions for tjob $1"
chmod go+r "$TMP_DIR/mobileshopping/envoy.yaml"
chmod go+r "$TMP_DIR/webshopping/envoy.yaml"

# Deploy containers
cd "$SUT_LOCATION"
log_message "Deploying containers for TJOB $1"
docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

log_message "Waiting for the system to be up..."
"$WORKSPACE/retorchfiles/scripts/waitforSUT.sh" "$1"
cd "$WORKSPACE"

log_message "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
