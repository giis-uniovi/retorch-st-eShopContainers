export TJOB_NAME="tjobeshopb"

rm -rf "./tmp"
mkdir -p "./tmp/$TJOB_NAME/mobileshopping"
mkdir -p "./tmp/$TJOB_NAME/webshopping"
cp -p "./ApiGateways/Envoy/config/mobileshopping/envoy.yaml" "./tmp/$TJOB_NAME/mobileshopping/"
cp -p "./ApiGateways/Envoy/config/webshopping/envoy.yaml" "./tmp/$TJOB_NAME/webshopping/"
docker compose rm -f -v
docker container prune -f
docker volume prune --all -f
echo "Building images"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "../../retorchfiles/envfiles/$TJOB_NAME.env" -p "$TJOB_NAME" build
echo "Desploying containers"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "../../retorchfiles/envfiles/$TJOB_NAME.env" -p "$TJOB_NAME" up -d
echo "Waiting for the system up..."
export TJOB_NAME="tjobeshopa"
mkdir -p "./tmp/$TJOB_NAME/mobileshopping"
mkdir -p "./tmp/$TJOB_NAME/webshopping"
cp -p "./ApiGateways/Envoy/config/mobileshopping/envoy.yaml" "./tmp/$TJOB_NAME/mobileshopping/"
cp -p "./ApiGateways/Envoy/config/webshopping/envoy.yaml" "./tmp/$TJOB_NAME/webshopping/"
echo "Building images for the TJOB: $TJOB_NAME"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "../../retorchfiles/envfiles/$TJOB_NAME.env" -p "$TJOB_NAME" build
echo "Desploying containers for the TJOB: $TJOB_NAME"
docker compose -f docker-compose.yml -f docker-compose.retorch.yml --env-file "../../retorchfiles/envfiles/$TJOB_NAME.env" -p "$TJOB_NAME" up -d
