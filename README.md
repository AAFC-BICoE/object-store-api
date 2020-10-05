# object-store-api

AAFC DINA object-store implementation.

See DINA object-store [specification](https://github.com/DINA-Web/object-store-specs).

## Required

* Java 11
* Maven 3.6 (tested)
* Docker 19+ (for running integration tests)

## Database
This project requires a PostgreSQL database to run.

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
Run tests using `mvn verify`. Docker is required, so the integration tests can launch an embedded Postgres test container.

## IDE

`object-store-api` requires [Project Lombok](https://projectlombok.org/) to be setup in your IDE.

Setup documentation for Eclipse: <https://projectlombok.org/setup/eclipse>

