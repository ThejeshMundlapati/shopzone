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

---

## Week 16: API Gateway & Service Discovery (v3.1.0)

### Service Discovery — Eureka Server (Port 8761)

Eureka is a service registry where all microservices register themselves on startup. Instead of hardcoding URLs like `http://localhost:8082`, services refer to each other by name (e.g., `http://product-service`). Eureka resolves the name to the actual IP and port at runtime.

| Component | Port | Purpose |
|-----------|------|---------|
| Discovery Server (Eureka) | 8761 | Service registry and discovery |
| API Gateway | 8080 | Single entry point for all client requests |

### API Gateway — Spring Cloud Gateway (Port 8080)

The API Gateway is the single entry point for all frontend requests. Instead of the React app calling 7 different ports, it calls one port (8080) and the gateway routes requests to the correct service via Eureka lookup.

**Features:**
- JWT validation at the gateway level (services don't need to re-validate)
- Rate limiting via Redis (prevents abuse)
- CORS configuration (centralized)
- Request logging
- Load balancing across service instances

### Updated Architecture with Gateway

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Frontend (React - Port 5173)                     │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ All requests go to port 8080
                                ▼
                    ┌───────────────────────┐
                    │    API Gateway :8080   │
                    │  (Spring Cloud Gateway)│
                    │  JWT + Rate Limiting   │
                    └───────────┬───────────┘
                                │ Routes via Eureka lookup
                                ▼
                    ┌───────────────────────┐
                    │  Eureka Server :8761  │
                    │  (Service Discovery)  │
                    └───────────────────────┘
                                │
    ┌──────────┬───────────┬────┴──┬───────────┬──────────┬──────────┐
    ▼          ▼           ▼       ▼           ▼          ▼          ▼
 User:8081  Product:8082 Cart:8083 Order:8084 Payment:8085 Notif:8086 Search:8087
```

### Quick Start (with Gateway)

```bash
# Start databases + Kafka
cd docker
docker compose up -d

# Start Eureka first, then all services
cd discovery-server && mvn spring-boot:run   # Wait for "Started" before continuing
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
# ... (all other services)
```

**Eureka Dashboard:** http://localhost:8761 (login: `eureka` / `password`)

---

## Week 17-18: Kafka & Event-Driven Architecture (v3.2.0)

### Overview

Added Apache Kafka for asynchronous event-driven communication between services. Instead of all inter-service calls being synchronous REST (Service A calls Service B and waits), certain operations now happen asynchronously via Kafka events (Service A publishes an event, Service B processes it whenever it's ready).

### What Changed

**Before (synchronous REST only):**
```
Customer places order → Order Service calls Product Service to reduce stock (waits)
                       → Order Service calls Payment Service to create intent (waits)
                       → Order Service calls Notification Service to send email (waits)
                       → Customer gets response (slow, tightly coupled)
```

**After (Kafka event-driven):**
```
Customer places order → Order Service publishes ORDER_CREATED event → returns immediately
                       → Product Service consumes event, reserves stock, publishes STOCK_RESERVED
                       → Payment Service creates intent, publishes PAYMENT_CREATED
                       → Notification Service consumes event, sends email asynchronously
```

### Kafka Infrastructure

| Component | Image | Port | Purpose |
|-----------|-------|------|---------|
| Zookeeper | confluentinc/cp-zookeeper:7.5.0 | 2181 | Kafka cluster coordination |
| Kafka | confluentinc/cp-kafka:7.5.0 | 9092 (host) / 29092 (internal) | Message broker |

### Kafka Topics

| Topic | Partitions | Producers | Consumers | Purpose |
|-------|------------|-----------|-----------|---------|
| `shopzone.order.events` | 3 | Order Service | Product, Payment, Notification, Search | Order lifecycle events |
| `shopzone.stock.events` | 3 | Product Service | Order Service | Stock reservation results |
| `shopzone.payment.events` | 3 | Payment Service | Order Service | Payment lifecycle events |
| `shopzone.notification.events` | 3 | Order Service | Notification Service | Email notification requests |

### Event Types

**Order Events (`shopzone.order.events`):**
- `ORDER_CREATED` — new order placed, triggers stock reservation
- `ORDER_CONFIRMED` — payment received, order confirmed
- `ORDER_SHIPPED` — admin shipped the order (includes tracking number)
- `ORDER_DELIVERED` — order delivered to customer
- `ORDER_CANCELLED` — order cancelled (triggers stock restoration)

**Stock Events (`shopzone.stock.events`):**
- `STOCK_RESERVED` — stock successfully reserved for all order items
- `STOCK_RESERVE_FAILED` — insufficient stock for one or more items

**Payment Events (`shopzone.payment.events`):**
- `PAYMENT_CREATED` — Stripe payment intent created
- `PAYMENT_SUCCESS` — payment completed via Stripe webhook
- `PAYMENT_FAILED` — payment failed

**Notification Events (`shopzone.notification.events`):**
- `ORDER_CONFIRMED` — send confirmation email
- `ORDER_SHIPPED` — send shipping email with tracking
- `ORDER_DELIVERED` — send delivery confirmation email
- `ORDER_CANCELLED` — send cancellation email with reason

### Event Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                       ORDER CREATION SAGA                            │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. Customer places order                                            │
│          │                                                           │
│          ▼                                                           │
│  2. Order Service creates order (PENDING)                            │
│          │                                                           │
│          │──► Kafka: ORDER_CREATED                                   │
│          │                                                           │
│          ├─────────────────────────────────────┐                     │
│          │                                     │                     │
│          ▼                                     ▼                     │
│  3. Product Service                    4. Payment Service            │
│     reserves stock                        creates intent             │
│          │                                     │                     │
│          │──► Kafka: STOCK_RESERVED            │──► Kafka:           │
│          │                                     │   PAYMENT_CREATED   │
│          │                                     │                     │
│          └─────────────────────────────────────┤                     │
│                                                │                     │
│                                                ▼                     │
│                                       5. Customer pays               │
│                                       (Stripe webhook)               │
│                                                │                     │
│                                                │──► Kafka:           │
│                                                │   PAYMENT_SUCCESS   │
│                                                │                     │
│                                                ▼                     │
│                                       6. Order CONFIRMED             │
│                                                │                     │
│                                                │──► Kafka:           │
│                                                │   ORDER_CONFIRMED   │
│                                                │                     │
│                                                ▼                     │
│                                       7. Notification Service        │
│                                          sends confirmation email    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Saga Pattern

The Order Creation Saga manages distributed transactions across services. Each step publishes an event that triggers the next step. If any step fails, compensating transactions undo previous steps.

**Happy Path:**
```
STARTED → STOCK_RESERVED → PAYMENT_CREATED → PAYMENT_SUCCESS → COMPLETED
```

**Failure — Insufficient Stock:**
```
STARTED → STOCK_RESERVE_FAILED → Order CANCELLED (nothing to compensate)
```

**Failure — Payment Failed:**
```
STARTED → STOCK_RESERVED → PAYMENT_FAILED → ORDER_CANCELLED → Stock Restored
```

**Saga State Entity:** The `saga_states` table in the orders database tracks each saga's progress. If the Order Service crashes and restarts, it can resume from the last known state.

| Column | Purpose |
|--------|---------|
| order_number | Which order this saga belongs to |
| status | Current saga step (STARTED, STOCK_RESERVED, COMPLETED, FAILED, etc.) |
| failure_reason | Why the saga failed (if applicable) |
| last_event_id | UUID of the last processed event (for idempotency) |

### Message Key Strategy

All events for the same order use `orderNumber` as the Kafka message key. This guarantees:
1. All events for one order go to the same partition
2. Events are processed in order (no race conditions)
3. Different orders can be processed in parallel across partitions

### Per-Service Kafka Components

**Order Service (Producer + Consumer):**
- `OrderEventProducer` — publishes order lifecycle events to `shopzone.order.events`
- `OrderEventConsumer` — consumes stock and payment events to drive the saga
- `OrderSagaManager` — orchestrates saga state transitions with compensation

**Product Service (Producer + Consumer):**
- `StockEventProducer` — publishes stock reservation results to `shopzone.stock.events`
- `OrderEventConsumer` — consumes ORDER_CREATED to reserve stock, ORDER_CANCELLED to restore stock
- Two-phase stock check: verifies ALL items have sufficient stock before reducing any

**Payment Service (Producer + Consumer):**
- `PaymentEventProducer` — publishes payment lifecycle events to `shopzone.payment.events`
- `OrderEventConsumer` — consumes ORDER_CANCELLED for potential auto-refund

**Notification Service (Consumer only):**
- `NotificationEventConsumer` — consumes notification events and sends emails via Mailtrap
- If service is down, messages wait in Kafka and are processed when it comes back

**Search Service (Consumer only):**
- `ProductSyncConsumer` — logs when products need re-indexing after stock changes

### Shared Event DTOs (shopzone-common)

| Class | Purpose |
|-------|---------|
| `OrderEvent` | Order lifecycle events (created, confirmed, shipped, delivered, cancelled) |
| `OrderItemEvent` | Nested line item data (product ID, name, quantity, price) |
| `StockEvent` | Stock reservation results (reserved, failed with product details) |
| `PaymentEvent` | Payment lifecycle events (created, success, failed with error details) |
| `NotificationEvent` | Email notification requests (type, recipient, order context) |
| `KafkaTopicConfig` | Programmatic topic creation with 3 partitions each |

### Configuration

Each service that uses Kafka has this in its `application.yml` under `spring:`:

```yaml
kafka:
  bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  producer:
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
  consumer:
    auto-offset-reset: earliest
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    properties:
      spring.json.trusted.packages: com.shopzone.common.event
```

### Quick Start (with Kafka)

```bash
# 1. Start all infrastructure (databases + Kafka)
cd docker
docker compose up -d

# 2. Verify Kafka is healthy
docker ps --format "table {{.Names}}\t{{.Status}}"
# shopzone-kafka and shopzone-zookeeper should show "healthy"

# 3. Build all services
mvn clean install -DskipTests

# 4. Start services (Eureka first, then others)
cd discovery-server && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run

# 5. Verify Kafka topics created
docker exec shopzone-kafka kafka-topics --bootstrap-server localhost:9092 --list
# Should show: shopzone.order.events, shopzone.stock.events,
#              shopzone.payment.events, shopzone.notification.events

# 6. Watch Kafka events in real-time (optional)
docker exec shopzone-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic shopzone.order.events --from-beginning
```

### Kafka vs REST — What Changed, What Stayed

| Operation | Before (REST) | After (Kafka) |
|-----------|--------------|---------------|
| Reserve stock on order | Order Service calls Product Service directly | ORDER_CREATED event → Product Service reserves async |
| Send order emails | Order Service calls Notification Service directly | Notification events → Notification Service sends async |
| Record payment success | Payment Service calls Order Service directly | PAYMENT_SUCCESS event → Order saga advances (REST kept as fallback) |
| Get product details | REST call | Still REST (needs synchronous response) |
| Get user info | REST call | Still REST (needs synchronous response) |
| Create payment intent | REST call | Still REST (frontend needs clientSecret immediately) |

**Key principle:** Commands that don't need an immediate response (reduce stock, send email) moved to Kafka. Queries that need data back (get product, get user) stay as REST.

---

## Coming Next

- **Week 19-21:** Kubernetes deployment and monitoring (Prometheus, Grafana)

---

## Technology Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Backend | Spring Boot 3.2 (Java 17) | Application framework |
| API Gateway | Spring Cloud Gateway | Request routing, JWT validation, rate limiting |
| Service Discovery | Netflix Eureka | Service registration and discovery |
| Message Broker | Apache Kafka 7.5 (Confluent) | Event-driven async communication |
| Coordination | Apache Zookeeper | Kafka cluster management |
| Event Patterns | Saga Pattern (Choreography) | Distributed transaction management |
| Databases | PostgreSQL 15, MongoDB 7.0, Redis 7, Elasticsearch 8.11 | Polyglot persistence |
| Payments | Stripe (Payment Intent API) | Payment processing |
| Email | Mailtrap (dev) / SMTP (prod) | Transactional emails via Thymeleaf templates |
| Images | Cloudinary | Product image hosting |
| Security | JWT (HS256), Spring Security | Authentication and authorization |
| Build | Maven (multi-module) | Dependency management and builds |
| Containers | Docker + Docker Compose | Local development infrastructure |

## Version History

| Version | Week | Description |
|---------|------|-------------|
| v3.0.0 | 14-15 | Microservices migration (7 services + shopzone-common) |
| v3.1.0 | 16 | API Gateway (Spring Cloud Gateway) + Eureka Service Discovery |
| v3.2.0 | 17-18 | Kafka event-driven architecture + Saga pattern |