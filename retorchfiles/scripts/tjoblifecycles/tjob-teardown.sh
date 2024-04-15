#!/bin/bash
set -e

# Execute the script to write timestamp
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"

#Store container logs
"$WORKSPACE/retorchfiles/scripts/storeContainerLogs.sh" "$1"

# Change to SUT location
cd "$SUT_LOCATION"

# Tear down Docker containers and volumes
echo "Tearing down Docker containers and volumes for TJOB $1"
docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" down --volumes

# Return to the original working directory
cd "$WORKSPACE"


# Execute the script to write timestamp again
"$SCRIPTS_FOLDER/writetime.sh" "$2" "$1"
