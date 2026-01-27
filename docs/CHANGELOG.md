# Changelog

All notable changes to ShopZone will be documented in this file.


---

## [1.3.0] - 2026-01-27 (Phase 1 Week 4)

### Added

#### Checkout System
- Cart validation endpoint with stock and price checks
- Checkout preview with tax and shipping calculation
- Order placement with transactional flow
- Free shipping for orders over $50
- Configurable tax rate (default 8%)

#### Order Management
- Human-readable order numbers (ORD-YYYYMMDD-XXXX)
- Order lifecycle: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
- Order cancellation with stock restoration
- Order tracking with shipping carrier and tracking number
- User order history with pagination and filtering
- Order count endpoint

#### Admin Features
- View all orders with filters (status, payment status, date range)
- Update order status with validation
- Ship orders with tracking number requirement
- Order statistics dashboard (total orders, revenue, average order value)
- Search orders by order number, email, or customer name

#### Data Integrity
- Address snapshot preserved at order time
- Product snapshot preserved in order items
- Price locked at checkout time
- Stock validation before order placement

### Technical
- Order and OrderItem entities (PostgreSQL)
- OrderStatus and PaymentStatus enums
- OrderRepository with custom JPQL queries
- CheckoutService for checkout flow
- OrderService for order management
- OrderNumberGenerator for unique order IDs
- Fixed PostgreSQL null parameter casting in findWithFilters query

### API Endpoints Added
```
Checkout:
POST   /api/checkout/place-order
GET    /api/checkout/validate
GET    /api/checkout/preview

User Orders:
GET    /api/orders
GET    /api/orders/{orderNumber}
GET    /api/orders/{orderNumber}/track
GET    /api/orders/count
POST   /api/orders/{orderNumber}/cancel

Admin Orders:
GET    /api/admin/orders
GET    /api/admin/orders/{orderNumber}
GET    /api/admin/orders/stats
GET    /api/admin/orders/search
PATCH  /api/admin/orders/{orderNumber}/status
```

### Fixed
- PostgreSQL null parameter type inference in date range queries (CAST to timestamp)

---

## [1.2.0] - 2026-01-16 (Phase 1 Week 3)

### Added

#### Shopping Cart
- Redis-based cart storage with 30-day TTL
- Add, update, remove cart items
- Stock validation on add/update
- Cart totals calculation
- Clear cart functionality
- Validate cart endpoint

#### Wishlist
- Redis-based wishlist storage
- Add/remove wishlist items
- Check if product in wishlist
- Move item from wishlist to cart

#### Address Management
- Full CRUD for user addresses
- Set default address
- Address validation
- Multiple addresses per user

### Technical
- Spring Data Redis integration
- RedisTemplate with JSON serialization
- Cart and Wishlist models
- Address entity (PostgreSQL)
- CartService, WishlistService, AddressService

### API Endpoints Added
```
Cart:
GET    /api/cart
POST   /api/cart/add
PUT    /api/cart/update
DELETE /api/cart/remove/{productId}
DELETE /api/cart/clear
GET    /api/cart/validate

Wishlist:
GET    /api/wishlist
POST   /api/wishlist/add
DELETE /api/wishlist/remove/{productId}
GET    /api/wishlist/check/{productId}
POST   /api/wishlist/move-to-cart/{productId}

Address:
GET    /api/addresses
POST   /api/addresses
GET    /api/addresses/{id}
PUT    /api/addresses/{id}
DELETE /api/addresses/{id}
PATCH  /api/addresses/{id}/set-default
```

---

## [1.1.0] - 2026-01-05 (Phase 1 Week 2)

### Added

#### Product Catalog
- Product CRUD operations
- MongoDB storage for products
- Search by name, description, brand, tags
- Filter by price range, brand, category
- Pagination and sorting
- Product slug generation

#### Categories
- Category CRUD operations
- Hierarchical categories (parent-child)
- Category slug generation
- Products count per category

#### Image Management
- Cloudinary integration
- Multiple images per product
- Primary image designation
- Image deletion

### Technical
- Spring Data MongoDB integration
- Cloudinary SDK integration
- Product and Category entities
- ProductService, CategoryService
- CloudinaryService

### API Endpoints Added
```
Products:
GET    /api/products
POST   /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products/search
GET    /api/products/slug/{slug}
POST   /api/products/{id}/images

Categories:
GET    /api/categories
POST   /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}
GET    /api/categories/slug/{slug}
GET    /api/categories/{id}/products
```

---

## [1.0.0] - 2025-12-27 (Phase 1 Week 1)

### Added

#### Authentication
- User registration with validation
- JWT-based authentication
- Access token and refresh token
- Token refresh endpoint
- Password reset functionality
- Email verification (mock)

#### User Management
- User profile endpoint
- Role-based access (CUSTOMER, ADMIN)
- Account enable/disable

#### Security
- Spring Security configuration
- JWT filter for authentication
- Password encryption (BCrypt)
- CORS configuration

### Technical
- Spring Boot 3.2.0
- PostgreSQL database
- Spring Security 6.x
- JWT with jjwt 0.12.3
- Swagger/OpenAPI documentation
- Global exception handling
- Docker Compose for databases

### API Endpoints Added
```
Authentication:
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
GET    /api/auth/me
GET    /api/auth/verify-email
```

---

## Version Summary

| Version | Date       | Focus |
|---------|------------|-------|
| v1.3.0 | 2026-01-29 | Orders & Checkout |
| v1.2.0 | 2026-01-16 | Cart, Wishlist, Address |
| v1.1.0 | 2026-01-05 | Product Catalog |
| v1.0.0 | 2025-12-27 | Authentication |