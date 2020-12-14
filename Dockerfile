FROM openjdk:11-jre-slim

RUN useradd -s /bin/bash user
USER user
COPY --chown=644 target/object-store.api-*.jar /object-store-api.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/object-store-api.jar"]

