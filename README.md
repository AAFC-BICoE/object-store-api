# object-store-api

AAFC DINA object-store implementation.

See DINA object-store [specification](https://github.com/DINA-Web/object-store-specs).

## Database
This project requires a PostgreSQL database to run and to run integration tests.

## Minio
A [Minio](https://min.io/) service is also required to run the project (not required for testing).

## Container
The `object-store-api` can be built into a Docker image. It requires a `Postgres` database and `Minio` running (see example in [Testing](#testing)).
The environment variables below should match the values used in the `Postgres` and `Minio` containers.

[Docker Compose](https://docs.docker.com/compose/) snippet to run the image:

```
  object-store-api:
    image: object-store-api:0.1
    ports: 
        - "8080:8080"
    environment:
       spring.datasource.url: jdbc:postgresql://database/dina?currentSchema=objectstore
       spring.datasource.username: myuser
       spring.datasource.password: mypassword
       spring.liquibase.user: migration
       spring.liquibase.password: mypassword2
       spring.liquibase.contexts: schema-change
       minio.scheme: http
       minio.host: localhost
       minio.port: 9000
       minio.accessKey: minio	
       minio.secretKey: minio123
```

## Testing
For testing purpose or local development a [Docker Compose](https://docs.docker.com/compose/) file can be used:

```
version: '3'
services:
  db:
    image: "postgres:9.6"
    container_name: "objectstore_test_postgres"
    environment:
      POSTGRES_DB: object_store_test
      POSTGRES_PASSWORD: mypassword
    volumes:
      - ./src/test/resources/create-test-users.sql:/docker-entrypoint-initdb.d/1-init-schema.sql
    ports:
      - "5432:5432"
      
  minio:
    image: minio/minio
    container_name: minio
    volumes:
       - ./minio-data:/data
    environment:
      MINIO_ACCESS_KEY: minio
      MINIO_SECRET_KEY: minio123
    ports:
      - "9000:9000"
    command: server /data
```

To run the integration tests:

```
 mvn verify -Dspring.datasource.url=jdbc:postgresql://localhost/object_store_test -Dspring.datasource.username=test -Dspring.datasource.password=test
```

## IDE

`object-store-api` requires [Project Lombok](https://projectlombok.org/) to be setup in your IDE.

Setup documentation for Eclipse: <https://projectlombok.org/setup/eclipse>

## Additional License
`CycleAvoidingMappingContext` class is licensed under the Apache License, Version 2.0. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

## Testing