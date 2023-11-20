#!/bin/bash

COITEARDOWNSTART="$(date +%s%3N)"
cd "$SUT_LOCATION/src"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml down
echo 'Removing all the volumes not used'
docker volume prune --all -f
cd $WORKSPACE

$E2ESUITE_URL/retorchfiles/scripts/saveTJobLifecycledata.sh
COITEARDOWNEND="$(date +%s%3N)"

OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
echo -n ";$COITEARDOWNSTART;$COITEARDOWNEND" >>"$OUTPUTDIRCOI"
