EXPORT TJOB_NAME "tjobpruebas"
docker compose --env-file .env  build
docker compose --env-file .env  up
