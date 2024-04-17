#!/bin/bash

# Function to check the status of frontend and database
check_status() {
  local COUNTER=0
  local WAIT_LIMIT=20
  local TJOB_NAME="$1"
  local DB_SERVICE_NAME="sqldata_$TJOB_NAME"
  local WEB_SERVICE_URL="http://webmvc_$TJOB_NAME:80"
  local DB_NAME="[Microsoft.eShopOnContainers.Services.CatalogDb]"
  local TABLE_NAME="Catalog"
  local QUERY="USE $DB_NAME; SELECT COUNT (*) FROM $TABLE_NAME;"

  while true; do
    # Check if frontend service is up
    if curl --insecure -s "$WEB_SERVICE_URL" | grep -q "<div class=\"esh-catalog-item col-md-4\">"; then
      # Check if database service is up
      if docker exec "$DB_SERVICE_NAME" /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Pass@word' -Q "$QUERY" | grep -q "14"; then
        break
      fi
    fi

    echo "Waiting $COUNTER seconds for frontend and database services"
    sleep 5
    ((COUNTER++))

    if ((COUNTER > WAIT_LIMIT)); then
      echo "SUT is down, making a preventive tear-down and storing the logs for $1"
      "$WORKSPACE/retorchfiles/scripts/storeContainerLogs.sh" "$TJOB_NAME"
      # Tearing down the system.
      docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$TJOB_NAME.env" --ansi never -p "$TJOB_NAME" down --volumes
      exit 1
    fi
  done
}

# Check if the correct number of arguments is provided
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <TJobName>"
  exit 1
fi

# Call the function to check the status of frontend and database
check_status "$1"
echo "Frontend and database working"