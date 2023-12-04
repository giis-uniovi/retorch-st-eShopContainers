#!/bin/bash
set -e

# Function to copy Envoy configs and replace placeholders
copy_and_replace_envoy_configs() {
    local tjobname="$1"
    cp -p "$SUT_LOCATION/src/ApiGateways/Envoy/config/mobileshopping/envoy.yaml" "$SUT_LOCATION/src/tmp/$tjobname/mobileshopping/"
    cp -p "$SUT_LOCATION/src/ApiGateways/Envoy/config/webshopping/envoy.yaml" "$SUT_LOCATION/src/tmp/$tjobname/webshopping/"

    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/src/tmp/$tjobname/mobileshopping/envoy.yaml"
    sed -i "s/\${tjobname}/$tjobname/g" "$SUT_LOCATION/src/tmp/$tjobname/webshopping/envoy.yaml"
}

# Execute the script to write timestamp
"$WORKSPACE/retorchfiles/scripts/writetime.sh" "$2" "$1"

# Export Docker Host IP
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
export DOCKER_HOST_IP
echo "Exporting the HOST_IP: $DOCKER_HOST_IP"
echo "The HOST_IP is: $DOCKER_HOST_IP"

# COI setup
mkdir -p "$SUT_LOCATION/src/tmp/$1/mobileshopping"
mkdir -p "$SUT_LOCATION/src/tmp/$1/webshopping"

copy_and_replace_envoy_configs "$1"

# Deploy containers
cd "$SUT_LOCATION/src"
echo "Deploying containers for TJOB $1"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" up -d

echo "Waiting for the system to be up..."
"$WORKSPACE/retorchfiles/scripts/waitforeShopContainers.sh" "$1"
cd "$WORKSPACE"

echo "System READY!! Test execution can start!"

# Execute the script to write timestamp again
"$WORKSPACE/retorchfiles/scripts/writetime.sh" "$2" "$1"
