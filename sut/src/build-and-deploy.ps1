# This script deploys a single local instance of eShopContainers, either locally or on the CI virtual machine.
# It performs image and volume pruning and then recreates the containers, ensuring a "clean start" for the System Under
# Test (SUT). The script concludes with the tear-down command, executed before using CTRL+C.

$TJOB_NAME = "tjobeshoptesting"
$TMP_DIR = ".\tmp"
$CONFIG_DIR = ".\ApiGateways\Envoy\config"
$DOCKER_COMPOSE_FILE = "docker-compose.yml"
$ENV_FILE = "..\..\retorchfiles\envfiles\$TJOB_NAME.env"

# Clean up existing temporary directory
Remove-Item -Path $TMP_DIR -Recurse -Force
New-Item -Path "$TMP_DIR\$TJOB_NAME\mobileshopping" -ItemType Directory | Out-Null
New-Item -Path "$TMP_DIR\$TJOB_NAME\webshopping" -ItemType Directory | Out-Null

# Copy Envoy configurations to the temporary directory
Copy-Item -Path "$CONFIG_DIR\mobileshopping\envoy.yaml" -Destination "$TMP_DIR\$TJOB_NAME\mobileshopping\" -Force
Copy-Item -Path "$CONFIG_DIR\webshopping\envoy.yaml" -Destination "$TMP_DIR\$TJOB_NAME\webshopping\" -Force

# Create Envoy conf
(Get-Content "$TMP_DIR\$TJOB_NAME\mobileshopping\envoy.yaml") -replace '\${tjobname}', $TJOB_NAME | Set-Content "$TMP_DIR\$TJOB_NAME\mobileshopping\envoy.yaml"
(Get-Content "$TMP_DIR\$TJOB_NAME\webshopping\envoy.yaml") -replace '\${tjobname}', $TJOB_NAME | Set-Content "$TMP_DIR\$TJOB_NAME\webshopping\envoy.yaml"

# Remove Docker resources
docker-compose rm -f -v
docker container prune -f
docker volume prune --all -f

# Build and deploy containers
Write-Host "Building images..."
docker-compose -f $DOCKER_COMPOSE_FILE --env-file $ENV_FILE build

Write-Host "Deploying containers..."
docker-compose -f $DOCKER_COMPOSE_FILE --env-file $ENV_FILE up -d

Write-Host "Waiting for the system to be up..."
# Add a sleep command or other suitable mechanism here to wait for the system to be fully up

# Clean up containers and volumes
docker-compose -f $DOCKER_COMPOSE_FILE --env-file $ENV_FILE down --volumes
