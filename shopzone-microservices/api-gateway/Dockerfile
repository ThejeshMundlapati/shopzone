# ╔══════════════════════════════════════════════════════════════════╗
# ║  FILE: shopzone-microservices/api-gateway/Dockerfile            ║
# ║  ACTION: CREATE NEW                                             ║
# ╚══════════════════════════════════════════════════════════════════╝

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent POM and common module first (for dependency caching)
COPY pom.xml ./pom.xml
COPY shopzone-common/pom.xml ./shopzone-common/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl api-gateway -am -B 2>/dev/null || true

# Copy source code
COPY shopzone-common/src ./shopzone-common/src
COPY api-gateway/src ./api-gateway/src

# Build the api-gateway jar
RUN mvn clean package -pl api-gateway -am -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add curl for health checks
RUN apk add --no-cache curl

COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]