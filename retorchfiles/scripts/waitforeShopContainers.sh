#!/bin/bash

COUNTER=0
URL="http://webmvc_${1}/"
WAIT_LIMIT=40

while ! curl --insecure -s "${URL}" | grep -q "<div class=\"esh-catalog-item col-md-4\">"; do
  echo "Waiting $COUNTER seconds for $1 with URL $URL"
  sleep 5
  ((COUNTER++))

  if ((COUNTER > WAIT_LIMIT)); then
    echo "The container is down"
    exit 1
  fi
done
