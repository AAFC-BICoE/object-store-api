# object-store-api

The object-store-api is an API providing an object metadata store backed by [MinIO](https://min.io/).

Important note on MinIO: Starting version RELEASE.2022-06-02T02-11-04Z, MinIO implements [Erasure Coding](https://min.io/docs/minio/linux/operations/concepts/erasure-coding.html?ref=docs-redirect#minio-erasure-coding). On local filesystems files are now stored in another format and are named like `.xl.meta`.

Features :
 * Implements DINA object-store [specification](https://dina-web.github.io/object-store-specs/)
 * Aligned with [Audiovisual Core](https://ac.tdwg.org/termlist/) standard
 * Objects type detection
 * EXIF extraction for images
 * Supports derivative and relationship to original object
 * Supports external resources (link with metadata to a resource stored outside the object-store)
 * Duplicate detection
 * Thumbnails generation for images (supported types) and PDF
 * Optional Message Queue producer for integration (e.g. search index)

## Container Image
The Docker Image is available on [DockerHub](https://hub.docker.com/r/aafcbicoe/object-store-api/tags).

## Required

### To build and run tests
* Java 17
* Maven 3.8 (tested)
* Docker 20+ (for running integration tests)

### To run
* [Minio](https://min.io/)
* [PostgreSQL](https://www.postgresql.org/) 12
* [Keycloak](https://www.keycloak.org/)

## Usage

To run the object-store-api in the DINA ecosystem see [dina-local-deployment](https://github.com/AAFC-BICoE/dina-local-deployment).

For testing purpose a [Docker Compose](https://docs.docker.com/compose/) example file is available in the `local` folder.
Please note that the app will start without Keycloak and in `dev` mode.

Create a new docker-compose.yml file and .env file from the example file in the local directory:

```
cp local/docker-compose.yml.example docker-compose.yml
cp local/*.env .
```

Start the app (default port is 8081):

```
docker compose up
```

Once the services have started you can access metadata at http://localhost:8081/api/v1/metadata

Cleanup:
```
docker compose down
```

## Documentation

See [documentation](https://github.com/AAFC-BICoE/object-store-api/tree/master/docs) folder.

## Testing
Run tests using `mvn verify`. Docker is required, so the integration tests can launch an embedded Postgres test container.

## IDE

`object-store-api` requires [Project Lombok](https://projectlombok.org/) to be setup in your IDE.

Setup documentation for Eclipse: <https://projectlombok.org/setup/eclipse>

