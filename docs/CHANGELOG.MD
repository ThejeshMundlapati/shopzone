# Changelog

All notable changes to ShopZone will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2025-12-28 (Phase 1 Week 2)

### Added
- **Product Management**
    - Full CRUD operations for products
    - Product search (name, description, brand, tags)
    - Filter by price range
    - Filter by brand
    - Filter by category
    - Featured products endpoint
    - Pagination and sorting
    - Partial update support (no categoryId required)

- **Category Management**
    - Hierarchical categories (parent-child)
    - Category tree endpoint
    - Root categories endpoint
    - Category breadcrumbs
    - Slug-based URLs

- **Image Upload**
    - Cloudinary integration
    - Multiple images per product
    - Image deletion support
    - Automatic optimization

- **Security**
    - Public read access for products/categories
    - Admin-only write access
    - Updated SecurityConfig for new endpoints

### Technical
- MongoDB 7.0 integration
- Spring Data MongoDB
- Cloudinary SDK
- Slugify library for URL-friendly slugs
- Multi-database architecture (PostgreSQL + MongoDB)

### Fixed
- Search now queries name, description, brand, and tags
- Price filter works with explicit MongoDB query
- Partial updates work without requiring categoryId

---

## [1.0.0] - 2025-12-27 (Phase 1 Week 1)

### Added
- **User Authentication**
    - User registration with validation
    - User login with JWT tokens
    - Access token (24-hour expiry)
    - Refresh token (7-day expiry)
    - Token refresh endpoint
    - Current user endpoint

- **Password Management**
    - Forgot password (mock email)
    - Reset password with token
    - Password validation rules

- **Email Verification**
    - Email verification token generation
    - Verification endpoint (mock)

- **Security**
    - Spring Security 6.x integration
    - JWT-based authentication
    - Role-based access (CUSTOMER, ADMIN)
    - Password encryption (BCrypt)

- **API Documentation**
    - Swagger/OpenAPI 3.0
    - Interactive API testing UI

- **Error Handling**
    - Global exception handler
    - Standardized error responses
    - Custom exceptions

- **Testing**
    - Unit tests for AuthService
    - Integration tests for AuthController

### Technical
- Spring Boot 3.5.0
- Java 17
- PostgreSQL 16
- Docker Compose setup
- Maven build

---

## [1.2.0] - UPCOMING (Phase 1 Week 3)

### Planned
- Shopping cart functionality
- Wishlist management
- Cart persistence
- Guest cart support

---

## [1.3.0] - UPCOMING (Phase 1 Week 4)

### Planned
- Checkout process
- Order management
- Order history
- Order status tracking

---

## Version History

| Version | Date       | Phase | Description |
|---------|------------|-------|-------------|
| 1.0.0 | 2025-12-27 | Phase 1 Week 1 | User Authentication |
| 1.1.0 | 2026-01-05 | Phase 1 Week 2 | Product Catalog |
| 1.2.0 | TBD        | Phase 1 Week 3 | Shopping Cart |
| 1.3.0 | TBD        | Phase 1 Week 4 | Orders |
| 2.0.0 | TBD        | Phase 2 | Microservices |