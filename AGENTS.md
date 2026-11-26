# Contributor Guide

## Project

This is a Java 25 Spring Boot API for object metadata and object storage. The application code is under `src/main/java/ca/gc/aafc/objectstore/api`, tests are under the matching `src/test/java` package, runtime configuration is in `src/main/resources`, and user documentation is in `docs/` (AsciiDoc).

## Build and test

- Use Java 25 and Maven 3.9 or later.
- Run the full build and test suite with `mvn verify`. Integration tests use Testcontainers, so Docker must be available.
- Run a focused unit test with `mvn -Dtest=ClassNameTest test`.
- Run a focused integration test with `mvn -Dit.test=ClassNameIT verify`.
- Before submitting, use the CI checks: `mvn -B -q verify`, `mvn -q checkstyle:check`, and `mvn -q spotbugs:check`.

Unit test classes generally end in `Test`; integration test classes generally end in `IT`. Keep tests alongside the package they exercise under `src/test/java`.

## Changes

- Follow the existing package structure and nearby implementation and test patterns. Use the project's configured Checkstyle rules and preserve Java type safety.
- Keep API, persistence, and configuration changes consistent with the existing DINA object-store behavior and update relevant AsciiDoc files in `docs/` when behavior or configuration changes.
- Keep credentials and machine-specific settings out of source control. Use the examples under `local/` for local setup.
- Avoid committing generated Maven output from `target/`.
- UUIDs inserted as database values must be real, generated values. Never hand-write or invent one (e.g. 00000000-0000-0000-0000-000000000001); generate it with a UUID generator such as UUID.randomUUID(), uuidgen, or python -c "import uuid; print(uuid.uuid4())".
- Liquibase changeset IDs should follow the repository's existing naming convention.
