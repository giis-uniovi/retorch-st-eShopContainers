export TJOB_NAME="tjobpruebas"
docker compose rm -f -v
docker container prune -f
docker volume prune --all -f
docker compose -f docker-compose.yml -f docker-compose.retorch.yml build
docker compose -f docker-compose.yml -f docker-compose.retorch.yml up


