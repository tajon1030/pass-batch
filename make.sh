#!/bin/sh

if [ $1 = "start" ]; then
	echo "db-start"
	docker-compose up -d --build --force-recreate
elif [ $1 = "down" ]; then
	echo "db-down"
	docker-compose down -v
fi
