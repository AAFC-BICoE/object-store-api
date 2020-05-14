# object-store-api

AAFC DINA object-store implementation.

See DINA object-store [specification](https://github.com/DINA-Web/object-store-specs).

## Database
This project requires a PostgreSQL database to run and to run integration tests.

## Minio
A [Minio](https://min.io/) service is also required to run the project (not required for testing).

## To Run

For testing purpose or local development a [Docker Compose](https://docs.docker.com/compose/) example file is available in the `local` folder.
Please note that the jar running in the container will be the jar currently available in the `target` folder.

Create a new docker-compose.yml file and .env file from the example file in the local directory:

```
cp local/docker-compose.yml.example docker-compose.yml
cp local/*.env .
```

Start the app (default port is 8081):

```
docker-compose up --build
```

Once the services have started you can access the endpoints at http://localhost:8081/api/v1

Cleanup:
```
docker-compose down
```

## Testing
For testing purposes use the same docker-compose.yml and .env file (from the section before).

### 1. Add a `docker-compose.override.yml` file.

Create an override file to expose the postgres port on your host:
```
version: "3"

services:
  object-store-db:
    ports:
      - 5432:5432

```

### 2. Launch the database service and object services: (to perform db initialization)

```
docker-compose up -d

### 3. Get the ip address of the Postgres database:
...
POSTGRES_IP=$(docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' objectstoreapifork_object-store-db_1)

### 3. Launch  the integration tests:
...
mvn verify -Dspring.datasource.url=jdbc:postgresql://$POSTGRES_IP/object_store_test -Dspring.datasource.username=web_user -Dspring.datasource.password=test

### 4. Shutdown all of the containers:
...

docker-compose down

```
## IDE

`object-store-api` requires [Project Lombok](https://projectlombok.org/) to be setup in your IDE.

Setup documentation for Eclipse: <https://projectlombok.org/setup/eclipse>

