#!/bin/bash

COITEARDOWNSTART="$(date +%s%3N)"

echo 'Switch off all containers...'
docker stop "$(docker ps | grep tjobeShopContainers | awk '{print \$1}')" || echo 'All the containers are stopped!'
docker rm --volumes "$(docker ps -a | grep tjobeShopContainers | awk '{print \$1}')" || echo 'All the containers are removed!'
cd "$SUT_LOCATION/src"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml down

cd $WORKSPACE

$E2ESUITE_URL/retorchfiles/scripts/saveTJobLifecycledata.sh
COITEARDOWNEND="$(date +%s%3N)"

OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
echo -n ";$COITEARDOWNSTART;$COITEARDOWNEND" >>"$OUTPUTDIRCOI"

