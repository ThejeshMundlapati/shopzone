# ShopZone Microservices Architecture

Phase 5 (Weeks 14-15): Microservices migration from the monolith.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend (React - Port 5173)                 │
└──────────────────────────────┬──────────────────────────────────┘
                               │
    ┌──────────┬───────────┬───┴───┬───────────┬──────────┬──────────┐
    │          │           │       │           │          │          │
    ▼          ▼           ▼       ▼           ▼          ▼          ▼
┌────────┐┌────────┐┌─────────┐┌────────┐┌─────────┐┌────────┐┌────────┐
│  User  ││Product ││  Cart   ││ Order  ││ Payment ││Notifi- ││ Search │
│Service ││Service ││ Service ││Service ││ Service ││cation  ││Service │
│ :8081  ││ :8082  ││  :8083  ││ :8084  ││  :8085  ││ :8086  ││ :8087  │
└───┬────┘└───┬────┘└────┬────┘└───┬────┘└────┬────┘└───┬────┘└───┬────┘
    │         │          │         │          │         │         │
    ▼         ▼          ▼         ▼          ▼         ▼         ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│Postgres││MongoDB ││ Redis  ││Postgres││Postgres││Postgres││Elastic │
│ users  ││products││        ││ orders ││payments││notifs  ││ search │
└────────┘└────────┘└────────┘└────────┘└────────┘└────────┘└────────┘
```

## Services

| Service | Port | Database | Responsibilities |
|---------|------|----------|-----------------|
| user-service | 8081 | PostgreSQL (shopzone_users) | Auth, JWT, Users, Addresses |
| product-service | 8082 | MongoDB (shopzone_products) | Products, Categories, Cloudinary |
| cart-service | 8083 | Redis | Cart, Wishlist |
| order-service | 8084 | PostgreSQL (shopzone_orders) | Orders, Checkout, Dashboard |
| payment-service | 8085 | PostgreSQL (shopzone_payments) | Stripe, Payments, Refunds, Webhooks |
| notification-service | 8086 | PostgreSQL (shopzone_notifications) | Email via Mailtrap/SMTP |
| search-service | 8087 | Elasticsearch | Full-text search, Autocomplete |

## Inter-Service Communication

Services communicate via synchronous REST calls through `/api/internal/**` endpoints.
These endpoints have NO authentication — they're trusted internal network calls.

| Caller | Callee | Purpose |
|--------|--------|---------|
| Order Service | User Service | Get user info + address for checkout |
| Order Service | Product Service | Get product details, reduce/restore stock |
| Order Service | Cart Service | Get cart items, clear cart after checkout |
| Order Service | Payment Service | Create payment intent |
| Order Service | Notification Service | Send order emails |
| Cart Service | Product Service | Validate products, get prices |
| Payment Service | Order Service | Record payment success/failure |
| Product Service | Search Service | Sync products to Elasticsearch |
| User Service | Notification Service | Welcome email, password reset |

## Quick Start

### 1. Start Databases
```bash
cd docker
docker compose up -d
```

### 2. Build All Services
```bash
# From project root
mvn clean install -DskipTests
```

### 3. Run Services (each in a separate terminal)
```bash
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run
```

### 4. Verify
Each service has Swagger UI at: `http://localhost:{port}/swagger-ui.html`

## Shared Module: shopzone-common

Contains shared code used by ALL services:
- `ApiResponse<T>`, `PagedResponse<T>` — standard response wrappers
- `UserResponse`, `ProductResponse` — shared DTOs for inter-service data
- `JwtService`, `JwtAuthenticationFilter` — JWT validation (token-only, no DB)
- `BaseSecurityConfig` — common security setup
- `RestClientConfig` — RestTemplate for service-to-service calls
- Exceptions: `ResourceNotFoundException`, `BadRequestException`, etc.

## Compared to Monolith

| Aspect | Monolith (v2.3.0) | Microservices (v3.0.0) |
|--------|-------------------|----------------------|
| Deployment | Single JAR | 7 independent JARs |
| Database | Shared PostgreSQL + MongoDB + Redis | Separate DB per service |
| Communication | Direct method calls | REST API calls |
| Scaling | Scale everything | Scale individual services |
| Failure | One failure = all down | Isolated failures |
| JWT | Load UserDetails from DB | Token-only validation (no DB) |

## Coming Next

- **Week 16:** API Gateway (Spring Cloud Gateway) + Eureka Service Discovery
- **Week 17-18:** Kafka event-driven communication + Saga pattern
