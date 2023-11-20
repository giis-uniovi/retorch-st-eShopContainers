#!/bin/bash
COUNTER=0
URL="http://${1}/"
until curl --insecure -s "${URL}" | grep -q "<div class=\"esh-catalog-item col-md-4\">"; do
  echo "waiting $COUNTER for $1 with URL $URL"
  sleep 4
  COUNTER=$(expr $COUNTER + 1)
  if test "$COUNTER" -gt 20; then
    echo "The container is down"
    exit 1
  fi
done