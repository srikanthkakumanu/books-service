# books-service

Book and author catalog APIs at `/api/books` and `/api/authors`. Runs independently on port 9151 with Java 27, Spring Boot 4.1.1, PostgreSQL, Flyway, MapStruct 1.6.3, and Keycloak JWT validation.

## Build And Test

Use this service's own Gradle wrapper:

```bash
./gradlew clean build
./gradlew integrationTest
```

The integration tests require Docker and use an isolated PostgreSQL 18 container. Unit and MVC tests run without Docker. If Gradle 8.14.3 cannot launch on Java 27, use Java 21 to launch Gradle; compilation and tests still use the Java 27 toolchain:

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew clean build
```

## Run

```bash
docker compose up -d postgres keycloak
./gradlew bootRun
```

Keycloak must contain the `company-platform` realm before protected APIs can validate tokens. Realm/client provisioning and the collective gateway smoke flow are tracked in the workspace implementation checkpoint.

Configuration defaults to PostgreSQL `booksdb`, runtime credentials `theuser/theuser`, and Flyway credentials `bookadmin/bookadmin`. Shared database initialization is mounted from `../micro-services/postgres-init`. Vault supplies the same keys (`user`, `password`, `flw-user`, `flw-password`) outside local defaults.

Set `SPRING_DATASOURCE_URL`, `KEYCLOAK_ISSUER_URI`, `VAULT_HOST`, `VAULT_PORT`, `VAULT_TOKEN`, and `SERVER_PORT` for your environment. Optional Config Server and Eureka connections use the shared platform configuration. Enable the `seed` Spring profile explicitly to load sample authors/books; existing populated tables are left alone.

Health: `/actuator/health`. OpenAPI: `/api-docs`. Swagger UI: `/swagger-ui.html`. Search endpoints use query parameters, for example `/api/books/title?title=Example`.

## Authorization And Architecture

Protected endpoints require `Authorization: Bearer <token>`. Authenticated users may read the catalog. New books belong to the JWT subject unless a catalog manager assigns another owner. Book updates/deletes require the owner, `ADMIN`, or `MANAGER`; ownership transfers and author writes require `ADMIN` or `MANAGER`. Client-supplied owner ids cannot override a regular user's identity.

`domain` contains framework-free immutable book/author models and use-case/repository ports. `application/usecase` orchestrates transactions. `infrastructure` contains REST adapters, compatibility DTO facades, MapStruct mappers, JPA entities/repositories, and JWT identity extraction. Domain timestamps use UTC instants; legacy response date-times are rendered in UTC.

The remaining workspace migration is tracked in `../IAM_IMPLEMENTATION_CHECKPOINT.md`.
