# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies first (layer optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

# wget: healthcheck en docker-compose (imagen JRE mínima)
RUN apk add --no-cache wget

# Uploads (ClienteTramitesService / app.storage.upload-dir): appuser debe poder crear ./data/uploads
RUN mkdir -p /app/data/uploads \
    && chown -R appuser:appgroup /app/data

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
