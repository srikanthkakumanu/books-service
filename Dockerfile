FROM eclipse-temurin:27-jre-alpine
LABEL authors="skakumanu"
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /application
COPY --chown=appuser:appgroup build/libs/books-service-1.0.jar application.jar
USER appuser:appgroup
EXPOSE 9151
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=5 CMD curl --fail --silent http://localhost:9151/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]
