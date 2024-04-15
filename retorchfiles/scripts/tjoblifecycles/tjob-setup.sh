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
    cp -p "$SUT_LOCATION/ApiGateways/Envoy/config/mobileshopping/envoy.yaml" "$SUT_LOCATION/tmp/$tjobname/mobileshopping/"
    cp -p "$SUT_LOCATION/ApiGateways/Envoy/config/webshopping/envoy.yaml" "$SUT_LOCATION/tmp/$tjobname/webshopping/"

    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/tmp/$tjobname/mobileshopping/envoy.yaml"
    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/tmp/$tjobname/webshopping/envoy.yaml"
}
# COI setup
mkdir -p "$SUT_LOCATION/tmp/$1/mobileshopping"
mkdir -p "$SUT_LOCATION/tmp/$1/webshopping"

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
