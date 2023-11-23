#!/bin/bash
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
COISETUPSTART=$(date +%s%3N)

#Directories to store the data
mkdir -p "$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER"
mkdir -p "$WORKSPACE/artifacts"
#Here goes the COI set-up

cd "$SUT_LOCATION/src"
ls -la
echo "Building images"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml build
echo "Desploying containers"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml up -d
echo "Waiting for the system up..."

$WORKSPACE/retorchfiles/scripts/waitforeShopContainers.sh "webmvc-tjobeShopContainers"

cd $WORKSPACE

#Here ends the COI set-up
echo "System READY!! Test execution can start!"

COISETUPEND=$(date +%s%3N)


echo "COI-SETUP-START;COI-SETUP-END;COI-TEARDOWN-START;COI-TEARDOWN-END" >"$OUTPUTDIRCOI"
echo -n "$COISETUPSTART;$COISETUPEND">>"$OUTPUTDIRCOI"

