#!/bin/bash
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
COISETUPSTART=$(date +%s%3N)
TJOB_NAME="$1"
#Directories to store the data
mkdir -p "$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER"
mkdir -p "$WORKSPACE/artifacts"
#Here goes the COI set-up

cd "$SUT_LOCATION/src"
echo "Removing volumes and old containers"
docker stop "$(docker ps | grep tjob | awk '{print \$1}')" || echo "All the containers of the TJobs are stopped!"
docker rm --volumes "$(docker ps -a | grep tjob | awk '{print \$1}')" || echo "All the containers of th TJobs are removed!"
echo "Exporting the HOST_IP: $DOCKER_HOST_IP"
export DOCKER_HOST_IP=$(/sbin/ip route|awk '/default/ { print $3 }')
echo "The HOST_IP is: $DOCKER_HOST_IP"
echo "Building images"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml build
echo "Desploying containers"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml up -d
echo "Waiting for the system up..."

$WORKSPACE/retorchfiles/scripts/waitforeShopContainers.sh $TJOB_NAME

cd $WORKSPACE

#Here ends the COI set-up
echo "System READY!! Test execution can start!"

COISETUPEND=$(date +%s%3N)


echo "COI-SETUP-START;COI-SETUP-END;COI-TEARDOWN-START;COI-TEARDOWN-END" >"$OUTPUTDIRCOI"
echo -n "$COISETUPSTART;$COISETUPEND">>"$OUTPUTDIRCOI"

