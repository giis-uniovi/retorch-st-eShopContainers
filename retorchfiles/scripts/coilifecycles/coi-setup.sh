#!/bin/bash
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
COISETUPSTART=$(date +%s%3N)

#Directories to store the data
mkdir -p "$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER"
mkdir -p "$WORKSPACE/artifacts"
#Here goes the COI set-up

cd "$SUT_LOCATION/src"
ls -la
docker compose  -f docker-compose.yml -f docker-compose.override.yml  --env-file .env  build
docker compose -f docker-compose.yml -f docker-compose.override.yml --env-file .env  up -d


$WORKSPACE/retorchfiles/scripts/waitforeShopContainers.sh "webmvc-$1"

cd $WORKSPACE

#Here ends the COI set-up


COISETUPEND=$(date +%s%3N)


echo "COI-SETUP-START;COI-SETUP-END;COI-TEARDOWN-START;COI-TEARDOWN-END" >"$OUTPUTDIRCOI"
echo -n "$COISETUPSTART;$COISETUPEND">>"$OUTPUTDIRCOI"

