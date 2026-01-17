# ShopZone Architecture

## System Overview

ShopZone is a polyglot persistence e-commerce platform using the best database for each data type.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS                                    │
│                                                                         │
│    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
│    │  Swagger UI  │    │   React App  │    │  Mobile App  │             │
│    │   (Testing)  │    │   (Future)   │    │   (Future)   │             │
│    └──────────────┘    └──────────────┘    └──────────────┘             │
│                                                                         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT APPLICATION                         │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                       SECURITY LAYER                               │ │
│  │                                                                    │ │
│  │   JWT Authentication Filter → Security Config → Role-Based Access  │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                      CONTROLLER LAYER                              │ │
│  │                                                                    │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │ │
│  │  │  Auth   │ │ Product │ │Category │ │  Cart   │ │ Address │       │ │
│  │  │   API   │ │   API   │ │   API   │ │   API   │ │   API   │       │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘       │ │
│  │       │           │           │           │           │            │ │
│  └───────┼───────────┼───────────┼───────────┼───────────┼────────────┘ │
│          │           │           │           │           │              │
│  ┌───────┼───────────┼───────────┼───────────┼───────────┼────────────┐ │
│  │       ▼           ▼           ▼           ▼           ▼            │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │ │
│  │  │  Auth   │ │ Product │ │Category │ │  Cart   │ │ Address │       │ │
│  │  │ Service │ │ Service │ │ Service │ │ Service │ │ Service │       │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘       │ │
│  │       │           │           │           │           │            │ │
│  │                   SERVICE LAYER                                    │ │
│  └───────┼───────────┼───────────┼───────────┼───────────┼────────────┘ │
│          │           │           │           │           │              │
│  ┌───────┼───────────┼───────────┼───────────┼───────────┼────────────┐ │
│  │       ▼           ▼           ▼           ▼           ▼            │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │ │
│  │  │  User   │ │ Product │ │Category │ │  Cart   │ │ Address │       │ │
│  │  │  Repo   │ │  Repo   │ │  Repo   │ │  Repo   │ │  Repo   │       │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘       │ │
│  │       │           │           │           │           │            │ │
│  │              REPOSITORY LAYER (Data Access)                        │ │
│  └───────┼───────────┼───────────┼───────────┼───────────┼────────────┘ │
│          │           │           │           │           │              │
└──────────┼───────────┼───────────┼───────────┼───────────┼──────────────┘
           │           │           │           │           │
           ▼           ▼           ▼           ▼           ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  PostgreSQL  │ │   MongoDB    │ │    Redis     │ │  Cloudinary  │
│              │ │              │ │              │ │              │
│  • Users     │ │  • Products  │ │  • Cart      │ │  • Images    │
│  • Addresses │ │  • Categories│ │  • Wishlist  │ │              │
│              │ │              │ │  • Sessions  │ │              │
│  (JPA)       │ │  (MongoDB)   │ │  (Redis)     │ │  (HTTP API)  │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

---

## Database Selection Rationale

### PostgreSQL (Relational)
**Used for:** Users, Addresses, Orders (future)

**Why:**
- ACID transactions for financial data
- Complex relationships (user → addresses → orders)
- Strong consistency requirements
- Mature, battle-tested

### MongoDB (Document Store)
**Used for:** Products, Categories

**Why:**
- Flexible schema for varying product attributes
- Nested documents (specifications, variants)
- Fast catalog reads
- Easy to add new product fields

### Redis (In-Memory Cache)
**Used for:** Cart, Wishlist, Sessions

**Why:**
- Lightning-fast reads/writes
- Built-in TTL for cart expiration
- Session data doesn't need complex queries
- Scales horizontally
- Perfect for temporary, high-frequency data

---

## Data Flow Examples

### Add to Cart Flow
```
1. User clicks "Add to Cart"
         │
         ▼
2. CartController receives request
         │
         ▼
3. CartService validates:
   • Product exists (MongoDB query)
   • Stock available
   • Quantity limits
         │
         ▼
4. CartRepository saves to Redis
   Key: "cart:{userId}"
   TTL: 30 days
         │
         ▼
5. Response with updated cart
```

### Checkout Flow (Future Week 4)
```
1. User clicks "Checkout"
         │
         ▼
2. Validate cart (Redis)
         │
         ▼
3. Get shipping address (PostgreSQL)
         │
         ▼
4. Reserve stock (MongoDB)
         │
         ▼
5. Create order (PostgreSQL)
         │
         ▼
6. Clear cart (Redis)
```

---

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        HTTP REQUEST                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   JWT Authentication Filter                     │
│                                                                 │
│   1. Extract token from Authorization header                    │
│   2. Validate token signature and expiration                    │
│   3. Load user details from database                            │
│   4. Set authentication context                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Security Filter Chain                       │
│                                                                 │
│   PUBLIC ENDPOINTS:                                             │
│   • /api/auth/register, /login, /refresh                        │
│   • GET /api/products/**, /api/categories/**                    │
│   • /swagger-ui/**                                              │
│                                                                 │
│   AUTHENTICATED:                                                │
│   • /api/cart/**                                                │
│   • /api/wishlist/**                                            │
│   • /api/addresses/**                                           │
│   • /api/auth/me                                                │
│                                                                 │
│   ADMIN ONLY:                                                   │
│   • POST/PUT/DELETE /api/products/**                            │
│   • POST/PUT/DELETE /api/categories/**                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Package Structure

```
com.shopzone/
├── config/           # Configuration classes
│   ├── SecurityConfig.java      # Security rules
│   ├── RedisConfig.java         # Redis template
│   ├── MongoConfig.java         # MongoDB auditing
│   ├── CloudinaryConfig.java    # Image storage
│   ├── JwtConfig.java           # JWT properties
│   └── OpenApiConfig.java       # Swagger setup
│
├── controller/       # REST endpoints (thin layer)
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── CartController.java
│   ├── WishlistController.java
│   └── AddressController.java
│
├── service/          # Business logic (thick layer)
│   ├── AuthService.java
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   ├── WishlistService.java
│   ├── AddressService.java
│   ├── JwtService.java
│   └── CloudinaryService.java
│
├── repository/       # Data access
│   ├── UserRepository.java        (JPA)
│   ├── AddressRepository.java     (JPA)
│   ├── ProductRepository.java     (MongoDB)
│   ├── CategoryRepository.java    (MongoDB)
│   ├── CartRepository.java        (Redis - manual)
│   └── WishlistRepository.java    (Redis - manual)
│
├── model/            # Domain entities
│   ├── User.java                  (JPA Entity)
│   ├── Address.java               (JPA Entity)
│   ├── Product.java               (MongoDB Document)
│   ├── Category.java              (MongoDB Document)
│   ├── Cart.java                  (Redis POJO)
│   ├── CartItem.java              (Redis POJO)
│   ├── Wishlist.java              (Redis POJO)
│   └── WishlistItem.java          (Redis POJO)
│
├── dto/              # Data Transfer Objects
│   ├── request/      # Input validation
│   └── response/     # Output formatting
│
├── exception/        # Error handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── UnauthorizedException.java
│
└── security/         # Security components
    └── JwtAuthenticationFilter.java
```

---

## Key Design Patterns

### 1. Repository Pattern
- Abstracts data access
- Different implementations per database
- Easy to test with mocks

### 2. DTO Pattern
- Separates internal models from API contracts
- Request DTOs for validation
- Response DTOs for formatting

### 3. Service Layer Pattern
- Business logic centralized
- Controllers are thin
- Services can call other services

### 4. Factory Method Pattern
- `fromEntity()` methods in DTOs
- Clean entity-to-DTO conversion

---

## Future Architecture (Microservices)

```
Phase 5 Architecture:
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                              │
│                    (Spring Cloud Gateway)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
       ┌──────────┬───────────┼───────────┬──────────┐
       ▼          ▼           ▼           ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│   User   │ │ Product  │ │   Cart   │ │  Order   │ │ Payment  │
│ Service  │ │ Service  │ │ Service  │ │ Service  │ │ Service  │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
     │            │            │            │            │
     │            │            │            │            │
     └────────────┴────────────┴────────────┴────────────┘
                              │
                    ┌─────────┴─────────┐
                    │   Apache Kafka    │
                    │   (Event Bus)     │
                    └───────────────────┘
```