# Changelog

All notable changes to ShopZone will be documented in this file.


---

## [1.2.0] - 2026-01-16

### Added - Week 3: Cart, Wishlist & Address Management

#### Infrastructure
- Redis 7 container for cart and wishlist storage
- Redis Commander GUI for debugging (port 8081)
- Redis persistence with appendonly mode

#### Cart Features
- Shopping cart stored in Redis with 30-day TTL
- Add/update/remove items from cart
- Stock validation before adding items
- Quantity limits (max 10 per item, max 50 unique items)
- Price refresh on cart retrieval
- Cart validation for checkout
- Automatic removal of unavailable items

#### Wishlist Features
- Wishlist stored in Redis (no expiration)
- Add/remove products from wishlist
- Duplicate prevention
- Move single item to cart
- Move all in-stock items to cart
- Track in-stock and on-sale items

#### Address Management
- Full CRUD for shipping addresses
- Address types: HOME, WORK, OTHER
- Automatic default for first address
- Set any address as default
- Soft delete with active flag
- Maximum 10 addresses per user
- Formatted address helper

#### Security
- Cart endpoints secured for authenticated users
- Wishlist endpoints secured for authenticated users
- Address endpoints secured for authenticated users

### Technical
- Spring Data Redis integration
- Custom Redis repositories (not using CrudRepository)
- JSON serialization with Jackson for Redis
- Java 8 time module for datetime handling

---

## [1.1.0] - 2025-12-28

### Added - Week 2: Product Catalog

#### Infrastructure
- MongoDB 7 container for product data
- Dual-database architecture (PostgreSQL + MongoDB)

#### Product Features
- Product CRUD operations with MongoDB
- Pagination and sorting
- Search by name, description, brand, tags
- Filter by category, price range, brand
- Stock management
- SEO-friendly slugs

#### Category Features
- Hierarchical categories (parent-child)
- Category breadcrumbs
- Nested category tree

#### Image Upload
- Cloudinary integration (free 25GB tier)
- Multiple images per product
- Automatic image optimization

#### Security Updates
- Public read access for products/categories
- Admin-only write access

### Technical
- Spring Data MongoDB integration
- Slugify library for URL generation
- Cloudinary SDK integration

---

## [1.0.0] - 2025-12-27

### Added - Week 1: Authentication

#### Infrastructure
- Spring Boot 3.2 project setup
- PostgreSQL 15 container for user data
- Docker Compose configuration

#### Authentication Features
- User registration with validation
- JWT-based authentication
- Access and refresh tokens
- Token refresh mechanism
- Password reset flow
- Email verification (mock)

#### User Management
- User entity with roles
- Role-based access control (CUSTOMER, ADMIN)
- Protected user profile endpoint

#### API Documentation
- OpenAPI 3.0 / Swagger UI
- Interactive API testing

### Technical
- Spring Security 6 configuration
- JWT with jjwt library
- BCrypt password encoding
- Global exception handling

---

## Version History Summary

| Version | Date       | Phase | Week | Features |
|---------|------------|-------|------|----------|
| 1.2.0 | 2026-01-16 | 1 | 3 | Cart, Wishlist, Address |
| 1.1.0 | 2025-12-28 | 1 | 2 | Products, Categories, Images |
| 1.0.0 | 2025-12-27 | 1 | 1 | Authentication, Users |

---

## Upcoming

### [1.3.0] - Phase 1 Week 4 (Planned)
- Order creation and management
- Checkout flow
- Order status tracking
- Order history

### [2.0.0] - Phase 2 (Planned)
- Stripe payment integration
- Reviews and ratings
- Search with Elasticsearch
- Email notifications