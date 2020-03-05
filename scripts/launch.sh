#!/bin/bash
cd /app

./waitForDatabase.sh

echo "checking for users"
./checkUsers.sh
echo "url=$(echo '$spring.datasource.url' | awk  -f envSubstitution.awk)"
VERSION=$(cat ./pom.xml | grep -m 1 '<version>' | awk -F"[><]" '{print $3}')
export VERSION
echo "Version: '$VERSION'"
echo "executing java"
exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar $1-$VERSION.jar
