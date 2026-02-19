# Changelog

All notable changes to ShopZone will be documented in this file.



---

## [1.6.0] - 2026-02-19 (Phase 2 Week 7)

### Added - Email Notifications & Admin Dashboard 🆕

#### Email Notification System
- Integrated Spring Mail with Mailtrap for email testing
- Thymeleaf HTML email templates with responsive design
- Async email sending for non-blocking performance
- Email logging with status tracking (PENDING, SENT, FAILED)
- EmailLog entity for persistence in PostgreSQL
- EmailType and EmailStatus enums for categorization

#### Email Templates
- `welcome.html` - Welcome email on user registration
- `order-confirmation.html` - Order confirmation after payment
- `order-shipped.html` - Shipping notification with tracking info
- `order-delivered.html` - Delivery confirmation
- `order-cancelled.html` - Cancellation notice with refund details
- `password-reset.html` - Password reset link email
- All templates feature modern, responsive HTML/CSS design

#### Email Integration Points
- User registration → Welcome email
- Payment success (webhook) → Order confirmation email
- Order status → SHIPPED → Shipping notification email
- Order status → DELIVERED → Delivery confirmation email
- Order cancellation → Cancellation email with refund info
- Password reset request → Reset link email

#### Admin Dashboard
- `GET /api/admin/dashboard/stats` - Comprehensive statistics overview
- Order counts by status (pending, confirmed, processing, shipped, delivered, cancelled)
- Revenue totals (today, this week, this month)
- User registration statistics (today, this week, this month)
- Product inventory stats (total, low stock, out of stock)
- Average order value and average rating
- `GET /api/admin/dashboard/recent-orders` - Recent orders summary
- `GET /api/admin/dashboard/top-products` - Top selling products

#### Reports & Analytics
- `GET /api/admin/reports/sales` - Sales reports with period support (daily, weekly, monthly)
- Custom date range support with startDate/endDate parameters
- Orders by status breakdown
- Top-selling products per period
- `GET /api/admin/reports/users` - User statistics and top customers
- Total users, active users, new user trends
- Top customers by order count and spending
- `GET /api/admin/reports/revenue` - Revenue analytics with trends
- Current vs previous period comparison
- Revenue growth percentage
- Daily revenue breakdown
- Category sales distribution

#### Admin User Management
- `GET /api/admin/users` - List all users with pagination and filters
- `PATCH /api/admin/users/{id}/status` - Enable/disable/lock user accounts
- `PATCH /api/admin/users/{id}/role` - Update user roles
- Search users by name or email
- Filter by role (CUSTOMER, ADMIN)

### Technical
- Added spring-boot-starter-mail dependency
- Added spring-boot-starter-thymeleaf dependency
- Created MailConfig for Thymeleaf template engine configuration
- Created EmailService with @Async for non-blocking email sending
- Created EmailLog entity with JPA mappings
- Created EmailLogRepository for email log persistence
- Created EmailType enum (WELCOME, ORDER_CONFIRMATION, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, PASSWORD_RESET)
- Created EmailStatus enum (PENDING, SENT, FAILED)
- Created DashboardService for statistics aggregation
- Created ReportService for analytics generation
- Created DashboardStatsResponse, RecentOrderSummaryResponse, TopProductResponse DTOs
- Created SalesReportResponse, UserManagementResponse, RevenueReportResponse DTOs
- Created DailyRevenueEntry, CategorySalesResponse DTOs
- Created AdminDashboardController with stats, recent-orders, top-products endpoints
- Created AdminReportController with sales, users, revenue endpoints
- Created AdminUserController with user management endpoints
- Updated OrderRepository with aggregate query methods for reporting
- Updated UserRepository with registration statistics methods
- Updated ReviewRepository with average rating method
- Updated OrderService to trigger email on status changes
- Updated UserService/AuthService to trigger welcome email on registration
- Updated PaymentService to trigger order confirmation email
- Updated SecurityConfig for admin dashboard, reports, and user management endpoints

### API Endpoints Added
```
Admin Dashboard:
GET    /api/admin/dashboard/stats           - Dashboard statistics
GET    /api/admin/dashboard/recent-orders   - Recent orders
GET    /api/admin/dashboard/top-products    - Top selling products

Admin Reports:
GET    /api/admin/reports/sales             - Sales report (daily/weekly/monthly)
GET    /api/admin/reports/users             - User statistics
GET    /api/admin/reports/revenue           - Revenue analytics

Admin User Management:
GET    /api/admin/users                     - List all users
PATCH  /api/admin/users/{id}/status         - Update user status
PATCH  /api/admin/users/{id}/role           - Update user role
```

### Configuration Added
```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: ${MAILTRAP_USERNAME}
    password: ${MAILTRAP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

### Database Changes
- Added `email_logs` table with columns:
  - id, user_id, recipient_email
  - email_type, subject, status, error_message
  - order_number
  - created_at, sent_at
- Added indexes on: user_id, status, email_type

### Security
- Admin dashboard endpoints require ADMIN role
- Admin report endpoints require ADMIN role
- Admin user management endpoints require ADMIN role
- Email sending is asynchronous and does not affect main transaction flow

### Fixed
- Thymeleaf template resolution for email templates (prefix/suffix configuration)
- Template engine conflict between web and email template resolvers

---

## [1.5.0] - 2026-02-17

### Added - Reviews & Elasticsearch Search

#### Product Reviews (PostgreSQL)
- Review entity with user-product relationship
- One review per user per product constraint
- Rating validation (1-5 stars)
- Verified purchase badge (based on delivered orders)
- Review statistics with rating distribution
- Helpful count feature
- Masked username display for privacy

#### API Endpoints - Reviews
- `GET /api/reviews/product/{productId}` - Get product reviews with pagination
- `GET /api/reviews/product/{productId}/stats` - Get review statistics
- `POST /api/reviews` - Create review (authenticated)
- `PUT /api/reviews/{id}` - Update own review
- `DELETE /api/reviews/{id}` - Delete own review
- `GET /api/reviews/my-reviews` - Get user's reviews
- `GET /api/reviews/product/{id}/can-review` - Check eligibility
- `POST /api/reviews/{id}/helpful` - Mark review helpful
- `DELETE /api/reviews/admin/{id}` - Admin delete any review

#### Elasticsearch Integration
- Elasticsearch 8.11.0 Docker setup
- ProductDocument for search index
- Full-text search across name, description, brand, tags
- Fuzzy matching for typo tolerance
- Field boosting for relevance

#### Search Features
- Multi-field search with relevance scoring
- Price range filtering
- Category filtering
- Brand filtering
- Minimum rating filtering
- In-stock filtering
- Tag filtering
- Multiple sort options (relevance, price, rating, newest)
- Pagination support

#### Autocomplete
- Prefix-based suggestions
- Product name and brand matching
- Fuzzy matching support
- Configurable result limit

#### Similar Products
- Category-based recommendations
- Brand matching
- Price range similarity
- Configurable limit

#### Admin Sync Operations
- `POST /api/search/admin/sync` - Full MongoDB to ES sync
- `GET /api/search/admin/sync/status` - Sync status check
- `POST /api/search/admin/sync/product/{id}` - Single product sync
- `POST /api/search/admin/sync/category/{id}` - Category reindex

#### Product Model Updates
- Added averageRating field to Product
- Added reviewCount field to Product
- Auto-sync to Elasticsearch on product changes
- Rating updates trigger ES sync

#### Infrastructure
- Elasticsearch 8.11.0 in Docker Compose
- Spring Data Elasticsearch configuration
- Product index settings with analyzers
- Async sync service

---


## [1.4.0] - 2026-02-12 (Phase 2 Week 5)

### Added

#### Stripe Payment Integration
- Payment Intent creation for secure payment processing
- Client secret returned to frontend for Stripe.js confirmation
- Automatic payment method detection (cards, wallets, etc.)
- Order metadata included in payment for tracking
- Publishable key returned for frontend initialization

#### Webhook Handling
- Stripe webhook endpoint for payment events
- Signature verification for security (webhook secret)
- Fallback deserialization for API version mismatches
- Event handlers for:
  - `payment_intent.succeeded` - Confirms order, reduces stock
  - `payment_intent.payment_failed` - Updates order status
  - `payment_intent.canceled` - Handles cancellation
  - `charge.refunded` - Tracks refunds (audit)
  - `charge.dispute.created` - Logs disputes

#### Refund Processing
- Full refund support via Stripe Refund API
- Partial refund support with amount validation
- Refund reason tracking
- Optional stock restoration on refund
- 30-day configurable refund window
- Refund eligibility checking

#### Payment History
- User payment history endpoint with pagination
- Detailed payment information including:
  - Card details (last 4, brand)
  - Receipt URLs from Stripe
  - Refund amounts and status
- Valid sort fields: createdAt, amount, status, paidAt

#### Admin Features
- View all payments with filtering by status
- Process refunds (full or partial)
- Check refund eligibility for any order
- Payment statistics dashboard:
  - Total payments count
  - Successful/failed payments
  - Total revenue
  - Total refunded amount

### Technical
- Added Stripe Java SDK (v24.18.0)
- Created Payment entity (PostgreSQL) with indexes
- Updated PaymentStatus enum with Stripe states:
  - Added: AWAITING_PAYMENT, PARTIALLY_REFUNDED
- Added PaymentMethod enum (CARD, BANK_TRANSFER, WALLET, OTHER)
- Created StripeConfig for SDK initialization
- Created StripeService for low-level Stripe operations
- Created PaymentService for payment management
- Created RefundService for refund processing
- Created StripeWebhookService for event handling
- Updated Order entity with payment fields:
  - stripePaymentIntentId, stripeChargeId
  - receiptUrl, paidAt, amountRefunded
- Updated CheckoutService to set PENDING payment status
- Updated OrderService with:
  - reduceStockForOrder (on payment success)
  - restoreStockForOrder (on refund)
- Added PaymentRepository with search queries
- Updated SecurityConfig for webhook endpoint (public)

### API Endpoints Added
```
Payments:
POST   /api/payments/create-intent        - Create payment intent
GET    /api/payments/{orderNumber}        - Get payment status
GET    /api/payments/history              - Get payment history
GET    /api/payments/{orderNumber}/refund-eligibility - Check refund eligibility

Webhooks:
POST   /api/webhooks/stripe               - Stripe webhook handler (public)

Admin Payments:
GET    /api/admin/payments                - Get all payments
GET    /api/admin/payments/{orderNumber}  - Get payment details
POST   /api/admin/payments/refund         - Process refund
GET    /api/admin/payments/{orderNumber}/refund-eligibility - Check refund eligibility
GET    /api/admin/payments/stats          - Get payment statistics
```

### Security
- Webhook endpoint accessible without authentication
- Webhook signature verification for security
- Payment endpoints require user authentication
- Admin payment endpoints require ADMIN role
- Users can only access their own payments

### Configuration Added
```yaml
stripe:
  secret-key: ${STRIPE_SECRET_KEY}
  public-key: ${STRIPE_PUBLIC_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  currency: usd

shopzone:
  payment:
    refund-window-days: 30
```

### Database Changes
- Added `payments` table with columns:
  - id, order_id, order_number, user_id
  - stripe_payment_intent_id, stripe_charge_id, stripe_customer_id
  - client_secret, amount, currency, status, payment_method
  - card_last_four, card_brand
  - failure_code, failure_message
  - amount_refunded, stripe_refund_id, refund_reason
  - receipt_url, statement_descriptor
  - created_at, updated_at, paid_at, refunded_at
- Added indexes on: order_id, stripe_payment_intent_id, status, user_id
- Updated `orders` table:
  - Added: stripe_payment_intent_id, stripe_charge_id, receipt_url
  - Added: paid_at, amount_refunded
- Updated `payment_status` check constraint to include new statuses

### Fixed
- Webhook deserialization issues with API version mismatches
- Added fallback using `deserializeUnsafe()` for Stripe events
- Invalid sort field handling in payment history endpoint
- Sort field validation with whitelist (createdAt, amount, status, paidAt)

---

## [1.3.0] - 2026-01-29 (Phase 1 Week 4)

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

| Version | Date       | Phase | Focus |
|---------|------------|-------|-------|
| v1.6.0  | 2026-02-19 | 2     | Email Notifications & Admin Dashboard 🆕 |
| v1.5.0  | 2026-02-17 | 2     | Reviews & Elasticsearch |
| v1.4.0  | 2026-02-12 | 2     | Stripe Payment Integration |
| v1.3.0  | 2026-01-29 | 1     | Orders & Checkout |
| v1.2.0  | 2026-01-16 | 1     | Cart, Wishlist, Address |
| v1.1.0  | 2026-01-05 | 1     | Product Catalog |
| v1.0.0  | 2025-12-27 | 1     | Authentication |