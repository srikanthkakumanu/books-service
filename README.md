# books-service

A sample microservice for books


### Run

---

The following command builds an image and tags it as srikanthkakumanu/books-service and runs the Docker image locally. The build creates a spring user and spring group to run the application.

``````bash

docker build --build-arg JAR_FILE=build/libs/books-service-1.0.jar -t srikanthkakumanu/books-service .
``````

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
