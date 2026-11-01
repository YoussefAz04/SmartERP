FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:22-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system smarterp \
    && useradd --system --gid smarterp --home-dir /app smarterp

WORKDIR /app
COPY --from=build --chown=smarterp:smarterp /app/target/smarterp-backend-*.jar app.jar

USER smarterp
EXPOSE 8020

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl --fail --silent http://localhost:8020/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
