#!/bin/bash

# Change to the source directory
cd "$SUT_LOCATION/src"

echo "Building images of eShopContainers"

# Use double quotes for Docker Compose files
docker compose -f "docker-compose.yml" -f "docker-compose.retorch.yml" --ansi never build

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Images for eShopContainers."
else
    echo "Failed to build images of eShopContainers"
    exit 1
fi
