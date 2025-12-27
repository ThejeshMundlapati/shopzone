# ShopZone Architecture Documentation

## Overview

ShopZone is designed as a modular monolith that will evolve into a microservices architecture. This document describes the architectural decisions and patterns used.

## Current Architecture (Phase 1)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Apps                             │
│              (Web Browser / Mobile / Postman)                   │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Security Filter Chain                  │  │
│  │                  (JWT Authentication)                     │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                     REST Controllers                      │  │
│  │              (AuthController, UserController)             │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      Service Layer                        │  │
│  │          (AuthService, UserService, JwtService)           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Repository Layer                       │  │
│  │                    (Spring Data JPA)                      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        PostgreSQL                               │
│                     (Docker Container)                          │
└─────────────────────────────────────────────────────────────────┘
```

## Design Patterns Used

### 1. Layered Architecture

```
Controller Layer  →  Handles HTTP requests/responses
       ↓
Service Layer     →  Business logic
       ↓
Repository Layer  →  Data access
       ↓
Entity Layer      →  Domain models
```

### 2. DTO Pattern

Separate objects for:
- **Request DTOs**: Input validation
- **Response DTOs**: Output formatting
- **Entities**: Database mapping

```
Request Flow:
RegisterRequest --> AuthService --> User Entity --> UserRepository

Response Flow:
UserRepository --> User Entity --> AuthService --> UserResponse
```

### 3. Repository Pattern

Spring Data JPA repositories abstract database operations:

```java
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
}
```

### 4. Builder Pattern

Used for creating complex objects:

```java
User user = User.builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@example.com")
    .build();
```

## Security Architecture

### JWT Token Flow

```
┌────────┐     ┌────────────┐     ┌────────────┐     ┌──────────┐
│ Client │────►│   Login    │────►│ Validate   │────►│ Generate │
│        │     │  Request   │     │ Credentials│     │   JWT    │
└────────┘     └────────────┘     └────────────┘     └────┬─────┘
                                                          │
┌────────┐     ┌────────────┐     ┌────────────┐          │
│ Client │◄────│  Response  │◄────│  Tokens    │◄─────────┘
│        │     │            │     │ (Access +  │
└────────┘     └────────────┘     │  Refresh)  │
                                  └────────────┘

Subsequent Requests:

┌────────┐     ┌────────────┐     ┌────────────┐     ┌──────────┐
│ Client │────►│  Request   │────►│   JWT      │────►│ Process  │
│        │     │ + Bearer   │     │  Filter    │     │ Request  │
└────────┘     │   Token    │     │ Validates  │     └──────────┘
               └────────────┘     └────────────┘
```

### Token Structure

**Access Token (24 hours):**
```json
{
  "sub": "user@example.com",
  "iat": 1705312200,
  "exp": 1705398600,
  "jti": "unique-token-id"
}
```

**Refresh Token (7 days):**
```json
{
  "sub": "user@example.com",
  "iat": 1705312200,
  "exp": 1705917000,
  "jti": "unique-token-id",
  "type": "refresh"
}
```

## Database Design

### User Table

```sql
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(15),
                       role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
                       email_verified BOOLEAN DEFAULT FALSE,
                       verification_token VARCHAR(255),
                       verification_token_expiry TIMESTAMP,
                       password_reset_token VARCHAR(255),
                       password_reset_token_expiry TIMESTAMP,
                       refresh_token TEXT,
                       enabled BOOLEAN DEFAULT TRUE,
                       locked BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP
);
```

## Future Architecture (Phase 5+)

```
                              ┌─────────────────┐
                              │  Load Balancer  │
                              └────────┬────────┘
                                       │
                              ┌────────┴────────┐
                              │   API Gateway   │
                              │ (Spring Cloud)  │
                              └────────┬────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        │                              │                              │
        ▼                              ▼                              ▼
┌───────────────┐            ┌───────────────┐            ┌───────────────┐
│ User Service  │            │Product Service│            │ Order Service │
│   (8081)      │            │    (8082)     │            │    (8084)     │
└───────┬───────┘            └───────┬───────┘            └───────┬───────┘
        │                            │                            │
        ▼                            ▼                            ▼
┌───────────────┐            ┌───────────────┐            ┌───────────────┐
│  PostgreSQL   │            │   MongoDB     │            │  PostgreSQL   │
└───────────────┘            └───────────────┘            └───────────────┘
        │                            │                            │
        └────────────────────────────┼────────────────────────────┘
                                     │
                              ┌──────┴──────┐
                              │    Kafka    │
                              │  (Events)   │
                              └─────────────┘
```

## Error Handling Strategy

```
Exception Thrown
       │
       ▼
┌─────────────────────────┐
│ GlobalExceptionHandler  │
│  @RestControllerAdvice  │
└───────────┬─────────────┘
            │
   ┌────────┴────────┬────────────────┬─────────────┐
   ▼                 ▼                ▼             ▼
BadRequest      NotFound       Unauthorized      Generic
  (400)          (404)           (401)            (500)
   │                │                │              │
   └────────────────┴────────────────┴──────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  ApiResponse    │
                    │  (Standardized) │
                    └─────────────────┘
```

## Configuration Management

```
application.yml          → Common settings
application-dev.yml      → Development overrides
application-prod.yml     → Production overrides (future)
```

## Testing Strategy

```
Unit Tests           → Service layer (Mockito)
Integration Tests    → Controller layer (MockMvc)
E2E Tests            → Full application (TestContainers - future)
```

## Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| UUID for IDs | Better for distributed systems, no sequential guessing |
| JWT stateless | Scalable, no server-side session storage |
| Refresh tokens | Better UX with short-lived access tokens |
| BCrypt hashing | Industry standard, adaptive work factor |
| Lombok | Reduce boilerplate, cleaner code |
| DTO separation | Security (hide internal structure), flexibility |