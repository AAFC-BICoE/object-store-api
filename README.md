# Temporary build instructions:

If you are checking out the repo for the first time...

* git clone https://github.com/AAFC-BICoE/object-store-api.git
* git checkout Feature_2000_Openshift_Deployment

Execute these steps each time the dev branch must be deployed

* git fetch
* git status   // optional to copy the branch name for pasting later
* git checkout dev 
* git pull   // bring dev up to date
* git tag    // optional to check the correct tag is present
* git checkout Feature_2000_Openshift_Deployment
* git merge dev  

This step will bring up the Nano editor with a default message to the effect of "Branch dev merged into Branch Feature_2000_Openshift_Deployment..."
I usually change it slightly so that I recognize it when building.

Use \<Cntl\>O \<Enter\> to save the Nano editor contents.

Use \<Cntl\>X to exit Nano.

* git push  // requires your GitHub credentials

Go to the OKD site.

Enter the DinaUI project.

Go to the Builds/Builds page and click on 'obj-store-api-build-base'.

Press the 'Start Build' button.

ObjectStoreAPI container will automatically be redeployed with new version when the build completes.




# object-store-api


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

