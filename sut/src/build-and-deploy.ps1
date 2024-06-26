# This script deploys a single local instance of eShopContainers, either locally or on the CI virtual machine.
# It performs image and volume pruning and then recreates the containers, ensuring a "clean start" for the System Under
# Test (SUT). The script concludes with the tear-down command, executed before using CTRL+C.

$TJOB_NAME = "tjobeshoptesting"
$DOCKER_COMPOSE_FILE = "docker-compose.yml"
$ENV_FILE = "..\..\retorchfiles\envfiles\$TJOB_NAME.env"

# Remove Docker resources
docker compose rm -f -v
docker container prune -f
docker volume prune --all -f

# Build and deploy containers
Write-Host "Building images..."
docker compose -f $DOCKER_COMPOSE_FILE --env-file "..\..\retorchfiles\envfiles\tjobeshoptesting.env" build

Write-Host "Deploying containers..."
docker compose -f $DOCKER_COMPOSE_FILE --env-file "..\..\retorchfiles\envfiles\tjobeshoptesting.env" up -d

Write-Host "Waiting for the system to be up..."
# Add a sleep command or other suitable mechanism here to wait for the system to be fully up

# Clean up containers and volumes
#docker-compose -f $DOCKER_COMPOSE_FILE --env-file $ENV_FILE down --volumes
