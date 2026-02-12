# ShopZone Setup Guide

## Prerequisites

| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 17+ | [Adoptium](https://adoptium.net/) |
| Maven | 3.8+ | [Maven](https://maven.apache.org/) |
| Docker | 20+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| Git | 2.30+ | [Git](https://git-scm.com/) |
| IDE | Any | IntelliJ IDEA recommended |
| Stripe CLI | Latest | [Stripe CLI](https://stripe.com/docs/stripe-cli) 🆕 |

---

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone
```

### 2. Start Databases
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
CONTAINER ID   IMAGE          PORTS                     NAMES
xxxx           postgres:15    0.0.0.0:5432->5432/tcp    shopzone-postgres
xxxx           mongo:7        0.0.0.0:27017->27017/tcp  shopzone-mongodb
xxxx           redis:7        0.0.0.0:6379->6379/tcp    shopzone-redis
```

### 4. Set Up Stripe (Week 5+) 🆕
```bash
# Set environment variables
export STRIPE_SECRET_KEY=sk_test_your_key_here
export STRIPE_PUBLIC_KEY=pk_test_your_key_here
export STRIPE_WEBHOOK_SECRET=whsec_your_secret_here
```

### 5. Run Application
```bash
# From project root
./mvnw spring-boot:run
```

Or in IntelliJ:
- Open `ShopzoneApplication.java`
- Click the green ▶️ Run button

### 6. Access Application
| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |
| Redis Commander | http://localhost:8081 |

---

## Stripe Setup (Week 5) 🆕

### Step 1: Create Stripe Account
1. Go to https://dashboard.stripe.com/register
2. Create a FREE account
3. Complete email verification

### Step 2: Get API Keys
1. Go to **Developers → API keys**
2. Copy your **Test** keys:
   - Publishable key: `pk_test_...`
   - Secret key: `sk_test_...`

> ⚠️ **NEVER** use Live keys for development!

### Step 3: Install Stripe CLI
```bash
# macOS
brew install stripe/stripe-cli/stripe

# Windows (with Scoop)
scoop install stripe

# Linux
# Download from https://github.com/stripe/stripe-cli/releases
```

### Step 4: Login to Stripe CLI
```bash
stripe login
```

### Step 5: Forward Webhooks to Local Server
```bash
# In a new terminal, run:
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Copy the webhook signing secret (whsec_...)
# Set it as environment variable:
export STRIPE_WEBHOOK_SECRET=whsec_your_secret_here
```

### Step 6: Test Webhook Connection
```bash
# Trigger a test event
stripe trigger payment_intent.succeeded
```

You should see the event received in your application logs.

---

## Docker Compose Configuration

```yaml
# docker/docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: shopzone-postgres
    environment:
      POSTGRES_DB: shopzone
      POSTGRES_USER: shopzone_admin
      POSTGRES_PASSWORD: shopzone_secret_123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:7
    container_name: shopzone-mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: rootpassword
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  redis:
    image: redis:7-alpine
    container_name: shopzone-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: shopzone-redis-commander
    environment:
      - REDIS_HOSTS=local:redis:6379
    ports:
      - "8081:8081"
    depends_on:
      - redis

volumes:
  postgres_data:
  mongodb_data:
  redis_data:
```

---

## Application Configuration

### application.yml
```yaml
spring:
  application:
    name: shopzone

  # PostgreSQL
  datasource:
    url: jdbc:postgresql://localhost:5432/shopzone
    username: shopzone_admin
    password: shopzone_secret_123
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # MongoDB
  data:
    mongodb:
      uri: mongodb://shopzone_admin:shopzone_secret_123@localhost:27017/shopzone_products?authSource=admin

    # Redis
    redis:
      host: localhost
      port: 6379

# JWT Configuration
jwt:
  secret: your-256-bit-secret-key-here-make-it-long-and-random
  expiration: 86400000        # 24 hours
  refresh-expiration: 604800000  # 7 days

# Cloudinary Configuration
cloudinary:
  cloud-name: your-cloud-name
  api-key: your-api-key
  api-secret: your-api-secret

# Stripe Configuration 🆕
stripe:
  secret-key: ${STRIPE_SECRET_KEY:sk_test_default}
  public-key: ${STRIPE_PUBLIC_KEY:pk_test_default}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_default}
  currency: usd

# Order Configuration
shopzone:
  order:
    tax-rate: 0.08                    # 8% tax
    free-shipping-threshold: 50.00    # Free shipping over $50
    flat-shipping-rate: 5.99          # Otherwise $5.99
    cancellation-window-hours: 24     # Cancel within 24 hours
  
  # Payment Configuration 🆕
  payment:
    refund-window-days: 30            # Refund within 30 days

# Logging
logging:
  level:
    com.shopzone: DEBUG
    org.springframework.security: DEBUG
```

---

## Environment Variables (Production)

For production, use environment variables:

```bash
# Database
export POSTGRES_URL=jdbc:postgresql://prod-host:5432/shopzone
export POSTGRES_USER=prod_user
export POSTGRES_PASSWORD=secure_password

export MONGODB_URI=mongodb://user:pass@prod-host:27017/shopzone

export REDIS_HOST=prod-redis-host
export REDIS_PORT=6379

# JWT
export JWT_SECRET=your-production-secret-key

# Cloudinary
export CLOUDINARY_CLOUD_NAME=your-cloud
export CLOUDINARY_API_KEY=your-key
export CLOUDINARY_API_SECRET=your-secret

# Stripe 🆕
export STRIPE_SECRET_KEY=sk_live_your_live_key   # Use LIVE keys in production!
export STRIPE_PUBLIC_KEY=pk_live_your_live_key
export STRIPE_WEBHOOK_SECRET=whsec_your_production_webhook_secret

# Order Settings
export ORDER_TAX_RATE=0.08
export ORDER_FREE_SHIPPING_THRESHOLD=50.00
export PAYMENT_REFUND_WINDOW_DAYS=30
```

---

## MongoDB Setup

### Create Application User
```bash
# Connect to MongoDB
docker exec -it shopzone-mongodb mongosh -u root -p rootpassword

# Create user
use admin
db.createUser({
  user: "shopzone_admin",
  pwd: "shopzone_secret_123",
  roles: [
    { role: "readWrite", db: "shopzone_products" },
    { role: "dbAdmin", db: "shopzone_products" }
  ]
})
```

---

## Cloudinary Setup

1. Create free account at [cloudinary.com](https://cloudinary.com)
2. Go to Dashboard → Copy credentials
3. Update `application.yml` with:
   - cloud-name
   - api-key
   - api-secret

---

## Create Admin User

### Option 1: Register + Update
```bash
# 1. Register via API (Swagger UI)
POST /api/auth/register
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@shopzone.com",
  "password": "Admin123!",
  "phone": "1234567890"
}

# 2. Update role in PostgreSQL
docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@shopzone.com';"

# 3. Login again to get new token with ADMIN role
```

---

## Testing Payments 🆕

### Test Cards

| Card Number | Scenario |
|-------------|----------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0000 0000 0002` | Declined |
| `4000 0025 0000 3155` | Requires 3D Secure |
| `4000 0000 0000 9995` | Insufficient funds |
| `4000 0000 0000 0069` | Expired card |

Use any future expiry date (e.g., 12/34) and any 3-digit CVC.

### Test Payment Flow

1. **Create an order:**
```bash
POST /api/checkout/place-order
{
  "shippingAddressId": "your-address-id"
}
```

2. **Create payment intent:**
```bash
POST /api/payments/create-intent
{
  "orderNumber": "ORD-20260131-XXXX"
}
```

3. **Simulate payment success (via Stripe CLI):**
```bash
# The webhook will be triggered automatically when using Stripe.js
# For testing without frontend, trigger manually:
stripe trigger payment_intent.succeeded --add payment_intent:metadata.orderNumber=ORD-20260131-XXXX
```

4. **Check payment status:**
```bash
GET /api/payments/ORD-20260131-XXXX
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # Mac/Linux
taskkill /PID <PID> /F  # Windows
```

### Database Connection Failed
```bash
# Check containers are running
docker ps

# Restart containers
docker-compose down
docker-compose up -d

# Check logs
docker logs shopzone-postgres
docker logs shopzone-mongodb
docker logs shopzone-redis
```

### Stripe Webhook Not Received 🆕
```bash
# Ensure Stripe CLI is listening
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Check the webhook secret matches
echo $STRIPE_WEBHOOK_SECRET

# Check application logs for signature verification errors
```

### Invalid Webhook Signature 🆕
```bash
# The webhook secret from `stripe listen` changes each session
# Copy the new secret and update environment variable:
export STRIPE_WEBHOOK_SECRET=whsec_new_secret_here

# Restart the application
```

### Payment History Returns 500 Error 🆕
```bash
# Ensure you're using valid sort fields:
# Valid: createdAt, amount, status, paidAt
# Invalid: string, any other field

# Correct request:
GET /api/payments/history?page=0&size=10&sortBy=createdAt
```

### MongoDB Authentication Failed
```bash
# Recreate user
docker exec -it shopzone-mongodb mongosh -u root -p rootpassword --eval "
  db.getSiblingDB('admin').dropUser('shopzone_admin');
  db.getSiblingDB('admin').createUser({
    user: 'shopzone_admin',
    pwd: 'shopzone_secret_123',
    roles: [{role: 'readWrite', db: 'shopzone_products'}]
  });
"
```

### Lombok Not Working (IntelliJ)
1. File → Settings → Build → Compiler → Annotation Processors
2. Check "Enable annotation processing"
3. File → Invalidate Caches → Invalidate and Restart

### Order Query Fails with Null Parameters
If you see `could not determine data type of parameter` error:
- Ensure `OrderRepository.findWithFilters` uses `CAST(:startDate AS timestamp)`
- This is a PostgreSQL issue with null LocalDateTime parameters

---

## Stopping the Application

### Stop Spring Boot
- IntelliJ: Click red 🟥 Stop button
- Terminal: Press `Ctrl + C`

### Stop Stripe CLI 🆕
- Press `Ctrl + C` in the terminal running `stripe listen`

### Stop Databases
```bash
cd docker
docker-compose down
```

### Stop and Remove Data
```bash
cd docker
docker-compose down -v  # Removes all data!
```

---

## Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=PaymentServiceTest

# With coverage
./mvnw test jacoco:report
```

---

## Useful Commands

```bash
# View PostgreSQL data
docker exec -it shopzone-postgres psql -U shopzone_admin -d shopzone

# View orders
SELECT order_number, status, payment_status, total_amount FROM orders;

# View payments 🆕
SELECT order_number, status, amount, stripe_payment_intent_id FROM payments;

# View MongoDB data
docker exec -it shopzone-mongodb mongosh -u root -p rootpassword

# View products
use shopzone_products
db.products.find().pretty()

# View Redis data
docker exec -it shopzone-redis redis-cli
KEYS *
GET "cart:user-id"
```

---

## Stripe Dashboard Verification 🆕

After testing, check in Stripe Dashboard:
1. **Payments** → See all test payments
2. **Customers** → Customer records (if created)
3. **Developers → Logs** → API request logs
4. **Developers → Webhooks** → Webhook delivery attempts
5. **Developers → Events** → All events

---

## Production Checklist 🆕

Before going live with payments:

- [ ] Switch to **Live** API keys (`sk_live_`, `pk_live_`)
- [ ] Configure production webhook endpoint in Stripe Dashboard
- [ ] Update webhook signing secret for production
- [ ] Remove test card validation
- [ ] Enable HTTPS (required for Stripe)
- [ ] Set up error alerting/monitoring
- [ ] Configure dispute handling
- [ ] Test with real cards (small amounts)
- [ ] Review Stripe's [go-live checklist](https://stripe.com/docs/development/checklist)