#!/bin/bash

if [ $# -ne 1 ]; then
  echo "Please run the script with <script_name> <docker container name or id>"
  exit 1
fi

if [[ $(/usr/bin/id -u) -ne 0 ]]; then
    echo "Not running as root"
    exit
fi

containerId=$1
echo "Rm folders a4c"
docker exec --user root ${containerId} /bin/sh -c "rm -R /opt/a4c/deployment_logs/ /opt/a4c/logs/ /opt/a4c/runtime/"
echo "Kill container ${containerId}"
docker kill ${containerId}
echo "Start container ${containerId}"
docker start ${containerId}

echo "Wait for Alien4cloud to startup"
while true; do
	if docker logs --tail 1 ${containerId} 2>&1 | grep --quiet "Started Bootstrap in"; then
		break
	else
		sleep 2
	fi
done
