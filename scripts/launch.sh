#!/bin/bash
cd /app

echo "Appending env var translated from dot to underscore"
eval $(awk -f ./addEnvForDotWithUnderscore.awk)

echo "Postgress database $POSTGRES_DB on $POSTGRES_HOST:5432 with root user $POSTGRES_USER"
echo "application = $APPLICATION, database = $POSTGRES_DB, schema = $spring_liquibase_defaultSchema"

./waitForDatabase.sh
if [ 0 != $? ]; then
   echo "Cannot connect to Postgress database on $POSTGRES_HOST:5432 with root user $POSTGRES_USER."
   echo "Exiting with error"
   exit -1
fi
./checkUsers.sh

# Need to build the spring.datasource.url as database and schema name may be set differently in the Env Var settings.
URL="jdbc:postgresql://$POSTGRES_HOST/$POSTGRES_DB?currentSchema=$spring_liquibase_defaultSchema"

VERSION=$(cat ./pom.xml | grep -m 1 '<version>' | awk -F"[><]" '{print $3}')
echo "Version: '$VERSION'"

echo "executing java"
echo "java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap $ARGS -Dspring.datasource.url=$URL -jar $1-$VERSION.jar"
exec  java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap $ARGS -Dspring.datasource.url=$URL -jar $1-$VERSION.jar