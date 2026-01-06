# ShopZone Setup Guide

## Prerequisites

### Required Software
| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 17+ | [Adoptium](https://adoptium.net/) |
| Maven | 3.8+ | [Maven](https://maven.apache.org/) |
| Docker | Latest | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| Git | Latest | [Git](https://git-scm.com/) |
| IDE | Any | IntelliJ IDEA recommended |

### Accounts Needed
| Service | Purpose | Signup |
|---------|---------|--------|
| Cloudinary | Image storage | [Free signup](https://cloudinary.com/) |
| GitHub | Version control | [GitHub](https://github.com/) |

---

## Step 1: Clone Repository

```bash
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone
```

---

## Step 2: Start Databases

### Using Docker Compose

```bash
cd docker
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5432` (for users/auth)
- **MongoDB** on port `27017` (for products/categories)

### Verify Databases Running

```bash
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE         PORTS                     NAMES
abc123         postgres:16   0.0.0.0:5432->5432/tcp   shopzone-postgres
def456         mongo:7.0     0.0.0.0:27017->27017/tcp shopzone-mongodb
```

---

## Step 3: Configure Cloudinary

### Get Cloudinary Credentials

1. Login to [Cloudinary Console](https://console.cloudinary.com/)
2. Go to **Dashboard**
3. Copy: Cloud Name, API Key, API Secret

### Option A: Environment Variables (Recommended)

Windows (PowerShell):
```powershell
$env:CLOUDINARY_CLOUD_NAME="your-cloud-name"
$env:CLOUDINARY_API_KEY="your-api-key"
$env:CLOUDINARY_API_SECRET="your-api-secret"
```

Linux/Mac:
```bash
export CLOUDINARY_CLOUD_NAME="your-cloud-name"
export CLOUDINARY_API_KEY="your-api-key"
export CLOUDINARY_API_SECRET="your-api-secret"
```

### Option B: application.yml

Edit `src/main/resources/application.yml`:
```yaml
cloudinary:
  cloud-name: your-cloud-name
  api-key: your-api-key
  api-secret: your-api-secret
```

### Option C: IntelliJ Run Configuration

1. **Run → Edit Configurations**
2. Select **ShopzoneApplication**
3. Add **Environment Variables**:
   ```
   CLOUDINARY_CLOUD_NAME=your-cloud-name;CLOUDINARY_API_KEY=your-api-key;CLOUDINARY_API_SECRET=your-api-secret
   ```

---

## Step 4: Run Application

### Using IntelliJ IDEA

1. Open project in IntelliJ
2. Wait for Maven to download dependencies
3. Open `src/main/java/com/shopzone/ShopzoneApplication.java`
4. Click **green play button ▶️**
5. Select **Run 'ShopzoneApplication'**

### Using Terminal

```bash
# From project root
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### Verify Application Started

Look for:
```
Tomcat started on port 8080
Started ShopzoneApplication in X.XXX seconds
```

---

## Step 5: Access Application

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | API Documentation |
| http://localhost:8080/api-docs | OpenAPI JSON |
| http://localhost:8080/api/products | Products API |
| http://localhost:8080/api/categories | Categories API |

---

## Step 6: Create Admin User

### Using Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. **POST /api/auth/register**
3. Request body:
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@shopzone.com",
  "password": "Admin@123",
  "phone": "1234567890"
}
```
4. Execute

### Promote to Admin (Database)

Connect to PostgreSQL:
```bash
docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone
```

Run SQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@shopzone.com';
```

Exit:
```sql
\q
```

---

## Step 7: Test API

### Login as Admin

**POST /api/auth/login**
```json
{
  "email": "admin@shopzone.com",
  "password": "Admin@123"
}
```

Copy the `accessToken` from response.

### Authorize in Swagger

1. Click **Authorize 🔒** button
2. Enter: `Bearer <your-access-token>`
3. Click **Authorize**

### Create Category

**POST /api/categories**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and gadgets",
  "active": true,
  "displayOrder": 1
}
```

### Create Product

**POST /api/products**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest Apple iPhone",
  "sku": "APPL-IPH15P",
  "price": 999.99,
  "stock": 50,
  "categoryId": "PASTE_CATEGORY_ID_HERE",
  "brand": "Apple",
  "tags": ["smartphone", "apple"],
  "active": true,
  "featured": true
}
```

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

### Database Connection Failed

```bash
# Check if containers are running
docker ps

# Restart containers
cd docker
docker-compose down
docker-compose up -d
```

### Lombok Not Working in IntelliJ

1. **File → Settings → Build → Compiler → Annotation Processors**
2. Check **Enable annotation processing**
3. **File → Invalidate Caches → Invalidate and Restart**

### Maven Dependencies Not Loading

```bash
# Force update
./mvnw dependency:purge-local-repository
./mvnw clean install -U
```

### Cloudinary Upload Fails

- Verify credentials are correct
- Check internet connection
- Ensure file is valid image (JPEG, PNG, WebP, GIF)
- Check file size < 10MB

---

## Stopping Application

### Stop Spring Boot
- **IntelliJ**: Click red 🟥 Stop button
- **Terminal**: Press `Ctrl + C`

### Stop Databases
```bash
cd docker
docker-compose down
```

### Stop and Remove Data
```bash
cd docker
docker-compose down -v  # Removes volumes (data)
```

---

## Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=ProductServiceTest

# With coverage report
./mvnw test jacoco:report
```

---

## Environment Configuration

### Development (default)
```yaml
# application.yml
spring:
  profiles:
    active: dev
```

### Production
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

Run with profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```