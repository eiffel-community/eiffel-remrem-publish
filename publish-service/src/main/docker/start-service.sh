#!/bin/bash

# These two variables need to be set from Docker-compose or K8S at startup if MB and/or DB healthcheck should be used.
#WAIT_MB_HOSTS="localhost:15672 localhost:15672"

if [ ! -z "$WAIT_MB_HOSTS" ]
then
  /eiffel/health-check.sh "$WAIT_MB_HOSTS"
fi

echo
echo "Starting Eiffel RemRem-Publish"
echo

/eiffel/health-check.sh && catalina.sh run
