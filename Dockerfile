FROM maven:ibmjava-alpine

WORKDIR /project

# Cache maven dependencies
ADD pom.xml /project
RUN mvn clean install -Dmaven.test.skip=true -Dspring-boot.repackage.skip

# Stage 1: build jar
ADD . /project
RUN mvn test
RUN mvn clean install -Dmaven.test.skip=true

# Stage 2: extract jar and set entrypoint
FROM openjdk:8-jre-slim
RUN useradd -s /bin/bash user
USER user
COPY --from=0 --chown=644 /project/target/object-store.api-*.jar /object-store-api.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-jar","/object-store-api.jar"]
