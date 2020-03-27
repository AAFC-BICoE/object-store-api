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

Create a new docker-compose file and .env file from the example file in the local directory:

```
cp local/docker-compose.yml.example docker-compose.yml
cp local/.env.example .env
```

Start the app (default port is 8081):

```
docker-compose up --build
```


## Testing
For testing purposes a [Docker Compose](https://docs.docker.com/compose/) example file is available in the `local` folder.

### 1. Add a `docker-compose.override.yml` file.
```
version: "3"

services:
  db:
    ports:
      - 5432:5432

```
This will expose the postgres service port

### 2. Launch the database service

```
docker-compose up -d db
```

To run the integration tests:

```
 mvn verify -Dspring.datasource.url=jdbc:postgresql://localhost/object_store_test -Dspring.datasource.username=web_user -Dspring.datasource.password=test
```

## IDE

`object-store-api` requires [Project Lombok](https://projectlombok.org/) to be setup in your IDE.

Setup documentation for Eclipse: <https://projectlombok.org/setup/eclipse>

## Additional License
`CycleAvoidingMappingContext` class is licensed under the Apache License, Version 2.0. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0