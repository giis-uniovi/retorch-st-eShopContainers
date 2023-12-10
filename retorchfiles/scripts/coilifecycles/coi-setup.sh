#!/bin/bash

SEL_VIDEO_DIR="/opt/selenoid/video/"
SEL_LOG_DIR="/opt/selenoid/logs/"

# Function to remove older files
remove_old_files() {
    dir=$1
    days=$2
    echo "Removing older files ($days days) in $dir! The number of files prior to remove:"
    find "$dir" | wc -l
    find "$dir" -mindepth 1 -maxdepth 1 -mtime +$days -exec rm -rf {} \;
    echo "Removing older files ($days days) in $dir! The number of files after remove:"
    find "$dir" | wc -l
}

# Set output directory for COI
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"

# Record start time for COI setup
COISETUPSTART=$(date +%s%3N)

# Remove older videos (older than 15 days)
remove_old_files "$SEL_VIDEO_DIR" 15

# Remove older logs (older than 7 days)
remove_old_files "$SEL_LOG_DIR" 7

# Create necessary directories
mkdir -p "$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER"
mkdir -p "$WORKSPACE/artifacts"
mkdir -p "$SUT_LOCATION/tmp"

# Pull Docker images
echo "Pulling images"
if docker pull selenoid/vnc_chrome:116.0 && docker pull selenoid/video-recorder:latest-release; then
    echo "Images pulled successfully."
else
    echo "Failed to pull Docker images."
fi

echo "Building images of SUT"

cd "$SUT_LOCATION"
# Use double quotes for Docker Compose files TEMPORAL FIX
docker compose -f "docker-compose.yml" --env-file "$WORKSPACE/retorchfiles/envfiles/tjobc.env" --ansi never build

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Images for SUT"
else
    echo "Failed to build images of SUT"
    exit 1
fi

# Record end time for COI setup
COISETUPEND=$(date +%s%3N)

# Write setup timestamps to output file
echo "COI-SETUP-START;COI-SETUP-END;COI-TEARDOWN-START;COI-TEARDOWN-END" > "$OUTPUTDIRCOI"
echo -n "$COISETUPSTART;$COISETUPEND" >> "$OUTPUTDIRCOI"
