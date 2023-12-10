#!/bin/bash
set -e
# Function to execute timestamp script
EXECUTE_TIMESTAMP_SCRIPT() {
  local STAGE="$1"
  local TJOB_NAME="$2"
  $SCRIPTS_FOLDER/writetime.sh "$STAGE" "$TJOB_NAME"
}

# Check if the required parameters are provided
if [ "$#" -lt 5 ]; then
  echo "Usage: $0 <TJOB_NAME> <STAGE> <SUT_URL> <PORT> <TESTS_TO_EXECUTE>"
  exit 1
fi
# Constants
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
# Define variables
TJOB_NAME="$1"
STAGE="$2"
PORT="$4"
TEST_NAME="$5"
if [[ $url == "https://" || $url == "http://" ]]; then
  echo "Only http:// || https:// protocol provided, using HOST DOCKER INTERNAL IP "
  SUT_URL="$3"+DOCKER_HOST_IP
else
  echo "Using the provided URL"
  SUT_URL="$3"
fi
echo "The URL IS $SUT_URL"

# Execute the script to write timestamp
EXECUTE_TIMESTAMP_SCRIPT "$STAGE" "$TJOB_NAME"

# Display HOST_IP and PORT information
echo "The HOST_IP is: $DOCKER_HOST_IP and PORT $PORT for the TJOB $TJOB_NAME"

LOCALHOST="$DOCKER_HOST_IP:$PORT"
# Run Maven test
mvn test -Ddirtarget="$TJOBNAME" -Dtest="$TEST_NAME" -Dtjob_name="$TJOB_NAME" -DSUT_URL="$SUT_URL" -DSUT_PORT="$PORT"

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
