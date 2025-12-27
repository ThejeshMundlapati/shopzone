# ShopZone Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

| Software | Version | Download Link |
|----------|---------|---------------|
| Java JDK | 17+ | [Adoptium](https://adoptium.net/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/) |
| Docker | Latest | [Docker Desktop](https://docker.com/products/docker-desktop) |
| Git | Latest | [Git SCM](https://git-scm.com/) |
| IDE | Any | [IntelliJ IDEA](https://jetbrains.com/idea/) (recommended) |

## Verify Installation

```bash
# Check Java
java -version
# Expected: openjdk version "17.x.x" or higher

# Check Maven
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Check Docker
docker --version
docker-compose --version

# Check Git
git --version
```

## Step 1: Clone Repository

```bash
git clone https://github.com/yourusername/shopzone.git
cd shopzone
```

## Step 2: Start Database

```bash
# Navigate to docker folder
cd docker

# Start PostgreSQL container
docker-compose up -d

# Verify container is running
docker ps

# Check logs if needed
docker logs shopzone-postgres
```

**Expected Output:**
```
CONTAINER ID   IMAGE             STATUS          PORTS                    NAMES
abc123def456   postgres:15       Up 10 seconds   0.0.0.0:5432->5432/tcp   shopzone-postgres
```

## Step 3: Configure Application

The default configuration in `application.yml` should work out of the box.

If you need to customize:

```yaml
# src/main/resources/application.yml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopzone
    username: shopzone_admin
    password: shopzone_secret_2024

jwt:
  secret: <your-base64-encoded-secret>
  expiration: 86400000
```

## Step 4: Build Application

```bash
# From project root directory
./mvnw clean install

# Or skip tests for faster build
./mvnw clean install -DskipTests
```

## Step 5: Run Application

### Option A: Command Line
```bash
./mvnw spring-boot:run
```

### Option B: IntelliJ IDEA
1. Open the project in IntelliJ
2. Navigate to `ShopzoneApplication.java`
3. Click the green play button ▶️

### Option C: JAR File
```bash
java -jar target/shopzone-0.0.1-SNAPSHOT.jar
```

**Expected Console Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.0)

... Tomcat started on port(s): 8080 (http)
... Started ShopzoneApplication in X.XXX seconds
```

## Step 6: Verify Setup

### Check Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Access Swagger UI
Open in browser: http://localhost:8080/swagger-ui.html

### Test Registration API
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "TestPass123!",
    "phone": "1234567890"
  }'
```

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Database Connection Failed

1. Check if Docker container is running:
```bash
docker ps
```

2. Check container logs:
```bash
docker logs shopzone-postgres
```

3. Verify connection details in `application.yml`

4. Test database connection:
```bash
docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone
```

### Maven Build Fails

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Re-download dependencies
./mvnw dependency:resolve

# Build again
./mvnw clean install
```

### IntelliJ Not Recognizing Lombok

1. Install Lombok plugin: Settings → Plugins → Search "Lombok"
2. Enable annotation processing: Settings → Build → Compiler → Annotation Processors → Enable

## Development Tips

### Hot Reload
DevTools is included. Just save your file and the app will restart automatically.

### Database GUI
Connect using any PostgreSQL client:
- Host: `localhost`
- Port: `5432`
- Database: `shopzone`
- Username: `shopzone_admin`
- Password: `shopzone_secret_2024`

### Log Levels
Adjust in `application.yml`:
```yaml
logging:
  level:
    com.shopzone: DEBUG
    org.springframework.security: DEBUG
```

## Stopping the Application

```bash
# Stop Spring Boot (Ctrl+C in terminal)

# Stop database
cd docker
docker-compose down

# Stop and remove volumes (resets database)
docker-compose down -v
```

## Next Steps

1. ✅ Setup complete
2. Test all authentication endpoints
3. Review the API documentation
4. Proceed to Phase 1 Week 2 (Product Module)