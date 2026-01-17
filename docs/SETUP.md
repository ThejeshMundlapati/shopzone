# ShopZone Setup Guide

## Prerequisites

- **Java 17+** - [Download OpenJDK](https://adoptium.net/)
- **Docker Desktop** - [Download Docker](https://www.docker.com/products/docker-desktop/)
- **Maven 3.8+** - (included with IDE)
- **IDE** - IntelliJ IDEA Community (recommended)
- **Git** - [Download Git](https://git-scm.com/)

---

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone
```

### 2. Start All Services
```bash
cd docker
docker-compose up -d
```

### 3. Verify Services
```bash
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE                    STATUS    PORTS
xxxx           postgres:15-alpine       Up        0.0.0.0:5432->5432/tcp
xxxx           mongo:7.0                Up        0.0.0.0:27017->27017/tcp
xxxx           redis:7-alpine           Up        0.0.0.0:6379->6379/tcp
xxxx           rediscommander/...       Up        0.0.0.0:8081->8081/tcp
```

### 4. Configure Cloudinary (Optional for images)
1. Create free account at [cloudinary.com](https://cloudinary.com)
2. Get credentials from Dashboard
3. Update `application.yml`:
```yaml
cloudinary:
  cloud-name: your_cloud_name
  api-key: your_api_key
  api-secret: your_api_secret
```

### 5. Run Application
```bash
./mvnw spring-boot:run
```

Or in IntelliJ: Run `ShopzoneApplication.java`

### 6. Access Applications

| Service | URL | Purpose |
|---------|-----|---------|
| Swagger UI | http://localhost:8080/swagger-ui.html | API Testing |
| Redis Commander | http://localhost:8081 | Redis GUI |

---

## Database Access

### PostgreSQL
```bash
docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone
```

Common commands:
```sql
\dt                          -- List tables
SELECT * FROM users;         -- View users
SELECT * FROM addresses;     -- View addresses
\q                           -- Exit
```

### MongoDB
```bash
docker exec -it shopzone-mongodb mongosh
```

Common commands:
```javascript
use shopzone_products        // Switch database
db.products.find()           // View products
db.categories.find()         // View categories
exit                         // Exit
```

### Redis
```bash
docker exec -it shopzone-redis redis-cli
```

Common commands:
```bash
KEYS *                       # List all keys
GET cart:{userId}            # Get cart
TTL cart:{userId}            # Check expiration
DEL cart:{userId}            # Delete cart
KEYS wishlist:*              # List wishlists
FLUSHALL                     # Clear all (dev only!)
exit                         # Exit
```

Or use Redis Commander: http://localhost:8081

---

## First Time Setup

### Create Admin User

1. **Register via Swagger UI**
   - POST `/api/auth/register`
   ```json
   {
     "email": "admin@shopzone.com",
     "password": "Admin@123",
     "firstName": "Admin",
     "lastName": "User",
     "phone": "+1234567890"
   }
   ```

2. **Update role to ADMIN**
   ```bash
   docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone
   ```
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@shopzone.com';
   \q
   ```

3. **Login to get token**
   - POST `/api/auth/login`

4. **Authorize in Swagger**
   - Click "Authorize" button
   - Enter token (without "Bearer" prefix)

---

## Environment Configuration

### application.yml Structure
```yaml
# Server
server:
  port: 8080

spring:
  # PostgreSQL (Users, Addresses)
  datasource:
    url: jdbc:postgresql://localhost:5432/shopzone
    username: shopzone_admin
    password: shopzone_secret_2024

  # MongoDB (Products, Categories)
  data:
    mongodb:
      uri: mongodb://localhost:27017/shopzone_products
    
    # Redis (Cart, Wishlist)
    redis:
      host: localhost
      port: 6379

# JWT
jwt:
  secret: <base64-encoded-secret>
  expiration: 86400000        # 24 hours
  refresh-expiration: 604800000  # 7 days

# Cart Settings
cart:
  expiration-days: 30
  max-items: 50
  max-quantity-per-item: 10

# Cloudinary
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
```

---

## Docker Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### Reset Everything (Data Loss!)
```bash
docker-compose down -v
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f              # All services
docker-compose logs -f postgres     # PostgreSQL only
docker-compose logs -f redis        # Redis only
```

### Restart Single Service
```bash
docker-compose restart redis
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <pid> /F
```

### Database Connection Failed
```bash
# Check if containers are running
docker ps

# Restart containers
docker-compose restart
```

### Redis Connection Refused
```bash
# Check Redis is running
docker exec -it shopzone-redis redis-cli ping
# Should return: PONG

# Restart Redis
docker-compose restart redis
```

### Cart Not Saving
```bash
# Check Redis Commander
http://localhost:8081

# Look for keys starting with "cart:"
# If empty, check application logs
```

### MongoDB Authentication Error
```bash
# Our setup uses NO AUTH for simplicity
# Check docker-compose.yml doesn't have MONGO_INITDB_ROOT_USERNAME

# Reset MongoDB
docker-compose down
docker volume rm docker_mongodb_data
docker-compose up -d
```

### Token Expired (401 Error)
- Login again at POST `/api/auth/login`
- Copy new accessToken
- Re-authorize in Swagger

### Maven Build Failed
```bash
# In IntelliJ: Right-click pom.xml → Maven → Reload Project

# Or command line:
./mvnw clean install -DskipTests
```

### Lombok Not Working
1. Install Lombok plugin in IntelliJ
2. Enable annotation processing:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"
3. Rebuild project

---

## IDE Setup (IntelliJ IDEA)

### Required Plugins
- Lombok
- Spring Boot Assistant (optional)

### Recommended Settings
1. **Enable Annotation Processing**
   - Settings → Build → Compiler → Annotation Processors → Enable

2. **Auto Import**
   - Settings → Editor → General → Auto Import
   - Check "Add unambiguous imports on the fly"

3. **Code Style**
   - Settings → Editor → Code Style → Java
   - Set indent to 4 spaces

---

## Running Tests

```bash
# All tests
./mvnw test

# Skip tests
./mvnw spring-boot:run -DskipTests

# Single test class
./mvnw test -Dtest=CartServiceTest
```

---

## Production Considerations

For production deployment (future):
- Use environment variables for secrets
- Enable MongoDB authentication
- Use Redis password
- Configure proper CORS origins
- Set up HTTPS
- Use production database instances