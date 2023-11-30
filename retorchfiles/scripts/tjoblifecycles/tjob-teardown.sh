#!/bin/bash
set -e

# Function to execute timestamp script
execute_timestamp_script() {
    "$WORKSPACE/retorchfiles/scripts/writetime.sh" "$2" "$1"
}

# Execute the script to write timestamp
execute_timestamp_script "$1" "$2"

# Change to SUT location
cd "$SUT_LOCATION/src"

# Tear down Docker containers and volumes
echo "Tearing down Docker containers and volumes for TJOB $1"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" down --volumes

# Return to the original working directory
cd "$WORKSPACE"

# Execute the script to write timestamp again
execute_timestamp_script "$1" "$2"
