#!/bin/bash
cd /app

echo "checking for users"
./checkUsers.sh
echo "url=$(echo '$spring.datasource.url' | awk  -f envSubstitution.awk)"
echo "executing java"
exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar  object-store.api-0.2-SNAPSHOT.jar --debug
