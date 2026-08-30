# Multi-stage build: compile with full Maven+JDK image, run on a slim JRE.
# Build context is the repo root (see dockerContext in render.yaml) so the
# parent pom and all module poms are visible for the Maven reactor.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy just the POMs first so dependency resolution is cached across builds
# whenever only source code changes (not dependencies).
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY expense-service/pom.xml expense-service/pom.xml
COPY habit-service/pom.xml habit-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml

COPY api-gateway/src api-gateway/src

RUN mvn -q -pl api-gateway -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar

# Render sets PORT via envVars in render.yaml; the app reads it via
# ${PORT:...} in application.yml.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
