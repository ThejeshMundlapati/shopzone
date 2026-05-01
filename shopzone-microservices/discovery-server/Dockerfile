# ╔══════════════════════════════════════════════════════════════════╗
# ║  FILE: shopzone-microservices/discovery-server/Dockerfile       ║
# ║  ACTION: CREATE NEW                                             ║
# ╚══════════════════════════════════════════════════════════════════╝

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent POM and common module first (for dependency caching)
COPY pom.xml ./pom.xml
COPY shopzone-common/pom.xml ./shopzone-common/pom.xml
COPY discovery-server/pom.xml ./discovery-server/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl discovery-server -am -B 2>/dev/null || true

# Copy source code
COPY shopzone-common/src ./shopzone-common/src
COPY discovery-server/src ./discovery-server/src

# Build the discovery-server jar
RUN mvn clean package -pl discovery-server -am -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add curl for health checks
RUN apk add --no-cache curl

COPY --from=build /app/discovery-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]