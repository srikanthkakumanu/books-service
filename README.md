# books-service

This repository contains a sample microservice for books and authors. This particular branch uses GraphQL, OpenTelemetry, Swagger, Zipkin, PostgreSQL.

**Note**: Do not merge the branch with master/main branch.

### .env

```properties
# This file contains environment variables for your Docker Compose setup.
# Docker Compose automatically loads this file.

# --- Secrets ---
# Replace with your actual New Relic Ingest License Key.
NEW_RELIC_API_KEY=<eu01xx4eFFFFNRAL>

# --- Database Credentials ---
# Ensure these match the user created in your PostgreSQL container.
SPRING_DATASOURCE_USERNAME=booksadmin
SPRING_DATASOURCE_PASSWORD=booksadmin
FLYWAY_USERNAME=booksadmin
FLYWAY_PASSWORD=booksadmin
```

### Run

---

The following command builds an image and tags it as srikanthkakumanu/books-service and runs the Docker image locally. The build creates a spring user and spring group to run the application.

Simply run: `bash sh build.sh` and then run: `bash docker compose up`. build.sh file builds compile/builds source code and builds the docker images.

or

```bash

docker build --build-arg JAR_FILE=build/libs/books-service-1.0.jar -t srikanthkakumanu/books-service .
```

Run the application with user privileges helps to mitigate some risks. So, an important improvement to the Dockerfile is to run the application as a non-root user.

### Build Docker Image

---

```bash

./gradlew bootBuildImage --imageName=srikanthkakumanu/books-service
```

or

```bash
docker build -t srikanthkakumanu/books-service:1.0 .
```

### Push Docker Image to DockerHub

---

```bash

docker image push srikanthkakumanu/books-service:1.0
```

### Using Spring Profiles

---

```bash

docker run -e "SPRING_PROFILES_ACTIVE=prod" -p 8080:8080 -t srikanthkakumanu/books-service
```

or

```bash
docker run -e "SPRING_PROFILES_ACTIVE=dev" -p 8080:8080 -t srikanthkakumanu/books-service
```

### Debug App in Docker container (using JPDA)

---

```bash
docker run -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" -p 8080:8080 -p 5005:5005 -t srikanthkakumanu/books-service
```
