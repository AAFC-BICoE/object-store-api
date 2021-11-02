#!/bin/bash

# exit when any command fails
set -e

# this is a local image
docker_repo_target="aafc-bicoe/image-converter-local"
# building docker image
docker build -t $docker_repo_target -f deployment-local/Dockerfile .
# bring up/update containers
docker-compose -f deployment-local/docker-compose.yml up
