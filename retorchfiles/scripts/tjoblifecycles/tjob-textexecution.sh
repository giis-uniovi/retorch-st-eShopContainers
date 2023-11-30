#!/bin/bash
set -e

# Constants
WORKSPACE_DIR="$WORKSPACE/retorchfiles"
SCRIPT_DIR="$WORKSPACE_DIR/scripts"
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')

# Function to execute timestamp script
EXECUTE_TIMESTAMP_SCRIPT() {
    local STAGE="$1"
    local TJOB_NAME="$2"
    $SCRIPT_DIR/writetime.sh "$STAGE" "$TJOB_NAME"
}

# Check if the required parameters are provided
if [ "$#" -lt 4 ]; then
    echo "Usage: $0 <TJOB_NAME> <STAGE> <PORT> <TEST_NAME>"
    exit 1
fi

# Define variables
TJOB_NAME="$1"
STAGE="$2"
PORT="$3"
TEST_NAME="$4"

# Execute the script to write timestamp
EXECUTE_TIMESTAMP_SCRIPT "$STAGE" "$TJOB_NAME"

# Display HOST_IP and PORT information
echo "The HOST_IP is: $DOCKER_HOST_IP and PORT $PORT for the TJOB $TJOB_NAME"

# Run Maven test
mvn test -Ddirtarget=TJOBExample -Dtest="$TEST_NAME" -Dtjob_name="$TJOB_NAME" -DSUT_URL="$DOCKER_HOST_IP:$PORT"

# Check if Maven test failed
if [ $? -ne 0 ]; then
    echo "Maven test FAILED with exit code $?."
    exit 1 # Return 1 if the Maven test fails
fi

# If the script reaches here, the Maven test succeeded
echo "Maven test succeeded."

# Execute the script to write timestamp again
EXECUTE_TIMESTAMP_SCRIPT "$STAGE" "$TJOB_NAME"
exit 0
