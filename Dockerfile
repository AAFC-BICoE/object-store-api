FROM eclipse-temurin:21-jre-jammy

RUN useradd -s /bin/bash user
USER user
COPY --chown=644 target/object-store.api-*.jar /object-store-api.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/object-store-api.jar"]
