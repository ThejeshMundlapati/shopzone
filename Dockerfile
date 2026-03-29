# ============================================================
# ShopZone Backend — Multi-Stage Dockerfile
# Place this file at: C:\Users\theje\Desktop\New folder\shopzone\Dockerfile
# ============================================================

# ==================== STAGE 1: BUILD ====================
# Uses full JDK + Maven to compile the application
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Make Maven wrapper executable (in case it isn't)
RUN chmod +x mvnw

# Download dependencies ONLY (cached unless pom.xml changes)
# This is the key optimization — deps don't re-download on code changes
RUN ./mvnw dependency:resolve -B

# Now copy source code (this layer changes most often)
COPY src/ ./src/

# Build the JAR, skip tests (we'll test separately in CI/CD)
RUN ./mvnw package -DskipTests -B

# ==================== STAGE 2: RUNTIME ====================
# Uses slim JRE only — no JDK, no Maven, no source code
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S shopzone && adduser -S shopzone -G shopzone

# Copy ONLY the built JAR from the build stage
COPY --from=build /app/target/shopzone-0.0.1-SNAPSHOT.jar app.jar

# Copy email templates (Thymeleaf needs these at runtime)
# These are already inside the JAR, but this is a safety net
# COPY --from=build /app/src/main/resources/templates/ /app/templates/

# Switch to non-root user
USER shopzone

# Expose the application port
EXPOSE 8080

# Health check using Spring Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
# -XX:+UseContainerSupport tells JVM to respect container memory limits
# -XX:MaxRAMPercentage=75 uses 75% of container memory for heap
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]