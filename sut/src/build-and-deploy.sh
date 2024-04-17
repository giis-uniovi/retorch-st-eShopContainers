#!/bin/bash
# This script deploys a single local instance of eShopContainers, either locally or on the CI virtual machine.
# It performs image and volume pruning and then recreates the containers, ensuring a "clean start" for the System Under
# Test (SUT). The script concludes with the tear-down command, executed before using CTRL+C.

TJOB_NAME="tjobeshoptesting"
TMP_DIR="./tmp"
CONFIG_DIR="./ApiGateways/Envoy/config"
DOCKER_COMPOSE_FILE="docker-compose.yml"
ENV_FILE="../../retorchfiles/envfiles/$TJOB_NAME.env"

# Clean up existing temporary directory
rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR/$TJOB_NAME/mobileshopping"
mkdir -p "$TMP_DIR/$TJOB_NAME/webshopping"

# Copy Envoy configurations to the temporary directory
cp -p "$CONFIG_DIR/mobileshopping/envoy.yaml" "$TMP_DIR/$TJOB_NAME/mobileshopping/"
cp -p "$CONFIG_DIR/webshopping/envoy.yaml" "$TMP_DIR/$TJOB_NAME/webshopping/"

#Create Envoy conf
sed -i "s/\${tjobname}/$TJOB_NAME/g" "$TMP_DIR/$TJOB_NAME/mobileshopping/envoy.yaml"
sed -i "s/\${tjobname}/$TJOB_NAME/g" "$TMP_DIR/$TJOB_NAME/webshopping/envoy.yaml"

# Remove Docker resources
docker compose rm -f -v
docker container prune -f
docker volume prune --all -f

# Build and deploy containers
echo "Building images..."
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" build

echo "Deploying containers..."
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d

echo "Waiting for the system to be up..."
# Add a sleep command or other suitable mechanism here to wait for the system to be fully up

# Clean up containers and volumes
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" down --volumes
