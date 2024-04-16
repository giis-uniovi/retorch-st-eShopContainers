#!/bin/bash
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <TJobName>"
  exit 1
fi
DOCKER_HOST_IP=$(/sbin/ip route | awk '/default/ { print $3 }')
COUNTER=0
WAIT_LIMIT=40

while ! curl --insecure -s "http://webmvc_$1:80" | grep -q "<div class=\"esh-catalog-item col-md-4\">"; do
  echo "Waiting $COUNTER seconds for $1 with URL http://webmvc_$1:80"
  sleep 5
  ((COUNTER++))

  if ((COUNTER > WAIT_LIMIT)); then
    echo "SUT is down, making a preventive tear-down and storing the logs"
    "$WORKSPACE/retorchfiles/scripts/storeContainerLogs.sh" "$1"
    # Tearing down the system.
    docker compose -f docker-compose.yml --env-file "$WORKSPACE/retorchfiles/envfiles/$1.env" --ansi never -p "$1" down --volumes
    exit 1
  fi
done
