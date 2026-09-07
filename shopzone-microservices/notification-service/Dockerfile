# ============================================================
#  notification-service  —  multi-stage image
#  PLACE AT:  shopzone-microservices/notification-service/Dockerfile
#  BUILD (from the shopzone-microservices ROOT, not this folder):
#     docker build -t shopzone/notification-service:latest -f notification-service/Dockerfile .
# ============================================================

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Your parent pom lists all 10 modules, and Maven reads EVERY listed module's
# pom at reactor startup -- so all of them must be present or the build fails
# with "Child module ... does not exist". Copying poms first also caches deps.
COPY pom.xml ./
COPY shopzone-common/pom.xml ./shopzone-common/
COPY discovery-server/pom.xml ./discovery-server/
COPY api-gateway/pom.xml ./api-gateway/
COPY user-service/pom.xml ./user-service/
COPY product-service/pom.xml ./product-service/
COPY cart-service/pom.xml ./cart-service/
COPY order-service/pom.xml ./order-service/
COPY payment-service/pom.xml ./payment-service/
COPY notification-service/pom.xml ./notification-service/
COPY search-service/pom.xml ./search-service/

# Warm the dependency cache for this module (+ shopzone-common via -am)
RUN mvn dependency:go-offline -pl notification-service -am -B 2>/dev/null || true

# Copy only the source this image needs
COPY shopzone-common/src ./shopzone-common/src
COPY notification-service/src ./notification-service/src

# Build just this module and its in-repo deps (shopzone-common)
RUN mvn clean package -pl notification-service -am -DskipTests -B

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
