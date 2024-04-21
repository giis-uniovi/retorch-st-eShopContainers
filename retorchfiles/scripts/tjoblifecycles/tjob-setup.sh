#!/bin/bash
set -e
# Execute the script to write timestamp
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"

# Export Docker Host IP
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
export DOCKER_HOST_IP
echo "Exporting the HOST_IP: $DOCKER_HOST_IP"

# Custom Set-up commands
echo "Executing custom commands"

copy_and_replace_envoy_configs() {
  local tjobname="$1"
  local CONFIG_DIR="$SUT_LOCATION/ApiGateways/Envoy/config"
  local TMP_DIR="$SUT_LOCATION/tmp/$tjobname"
  if [ -d "$TMP_DIR" ]; then
    echo "Directory /tmp for the tjob $tjobname exist, removing...."
    rm -r "$TMP_DIR"
  else
    echo "Directory /tmp for the tjob $tjobname does not exist."
  fi

  mkdir -p "$TMP_DIR/mobileshopping"
  mkdir -p "$TMP_DIR/webshopping"

  cp -p "$CONFIG_DIR/mobileshopping/envoy.yaml" "$TMP_DIR/mobileshopping/"
  cp -p "$CONFIG_DIR/webshopping/envoy.yaml" "$TMP_DIR/webshopping/"

  sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/mobileshopping/envoy.yaml"
  sed -i "s/\${tjobname}/$tjobname/g" "$TMP_DIR/webshopping/envoy.yaml"

  chmod go+r "$TMP_DIR/mobileshopping/envoy.yaml"
  chmod go+r "$TMP_DIR/webshopping/envoy.yaml"
}

copy_and_replace_envoy_configs "$1"

# Deploy containers
cd "$SUT_LOCATION"
echo "Deploying containers for TJOB $1"
docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

echo "Waiting for the system to be up..."
"$WORKSPACE/retorchfiles/scripts/waitforSUT.sh" "$1"
cd "$WORKSPACE"

echo "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
