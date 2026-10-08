FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY code/pom.xml .
RUN mvn -B dependency:go-offline
COPY code/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 foodshare && useradd --uid 10001 --gid foodshare --no-create-home foodshare \
    && mkdir -p /app/uploads && chown -R foodshare:foodshare /app
WORKDIR /app
COPY --from=build --chown=foodshare:foodshare /build/target/foodshare-0.0.1-SNAPSHOT.jar app.jar
USER foodshare
EXPOSE 8080
ENV UPLOAD_DIR=/app/uploads
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
