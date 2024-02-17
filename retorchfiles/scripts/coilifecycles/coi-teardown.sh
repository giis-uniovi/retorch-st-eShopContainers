#!/bin/bash
set -e # Exit immediately if any command exits with a non-zero status

COITEARDOWNSTART="$(date +%s%3N)"

echo "Switch off all containers that start with *tjob*..."
docker rm $(docker stop $(docker ps -a -q --filter name=tjob --format="{{.ID}}") || echo 'Any Container to remove') || echo 'All the containers are removed!'

echo "Pruning also its volumes"
docker volume prune --all -f

# Run saveTJobLifecycledata.sh script
echo "Running saveTJobLifecycledata.sh script..."
sh "$SCRIPTS_FOLDER/savetjoblifecycledata.sh"

COITEARDOWNEND="$(date +%s%3N)"

OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
echo -n ";$COITEARDOWNSTART;$COITEARDOWNEND" >>"$OUTPUTDIRCOI"

echo "COI teardown script completed successfully."
