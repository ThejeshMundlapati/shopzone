# ShopZone Architecture

## System Overview


---

## Frontend Architecture 🆕 (Phase 3)

### Overview
```
┌─────────────────────────────────────────────────────────────────┐
│                   REACT FRONTEND (Vite)                         │
│                   http://localhost:5173                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                     App.jsx (Router)                    │   │
│   │                                                         │   │
│   │   Customer Routes (with Header/Footer):                 │   │
│   │   /, /products, /cart, /checkout, /orders, /profile     │   │
│   │                                                         │   │
│   │   Admin Routes (with AdminLayout/Sidebar):              │   │
│   │   /admin, /admin/products, /admin/orders, /admin/users  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│   ┌──────────────────────────┼──────────────────────────────┐   │
│   │                   Redux Store                           │   │
│   │  ┌────────┐ ┌───────┐ ┌─────────┐ ┌───────┐ ┌───────┐   │   │
│   │  │  auth  │ │ cart  │ │products │ │orders │ │ admin │   │   │
│   │  │ Slice  │ │ Slice │ │  Slice  │ │ Slice │ │ Slice │   │   │
│   │  └────────┘ └───────┘ └─────────┘ └───────┘ └───────┘   │   │
│   └──────────────────────────┼──────────────────────────────┘   │
│                              │                                  │
│   ┌──────────────────────────┼──────────────────────────────┐   │
│   │                  Service Layer (Axios)                  │   │
│   │  api.js → JWT interceptors, auto token refresh          │   │
│   │  authService, productService, cartService               │   │
│   │  orderService, adminService                             │   │
│   └──────────────────────────┼──────────────────────────────┘   │
│                              │                                  │
│                              ▼ HTTP/REST                        │
│                 Spring Boot API (port 8080)                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Admin Dashboard Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                  ADMIN DASHBOARD FLOW                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   AdminRoute (role check)                                       │
│       │                                                         │
│       ▼                                                         │
│   AdminLayout                                                   │
│   ┌───────────────────┬─────────────────────────────────────┐   │
│   │   AdminSidebar    │        <Outlet /> (page content)    │   │
│   │                   │                                     │   │
│   │  Dashboard        │   Dashboard.jsx:                    │   │
│   │  Products         │   ├── fetchDashboardStats()         │   │
│   │  Categories       │   ├── fetchRecentOrders()           │   │
│   │  Orders           │   ├── fetchTopProducts()            │   │
│   │  Users            │   └── getRevenueReport()            │   │
│   │  Reviews          │         │                           │   │
│   │  Reports          │         ▼                           │   │
│   │  Settings         │   Recharts: Area, Bar, Pie, Line    │   │
│   │  ─────────────    │                                     │   │
│   │  Back to Store    │   adminService.js → Axios → API     │   │
│   └───────────────────┴─────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Route Protection
```
┌─────────────────────────────────────────────────────────────────┐
│                    ROUTE PROTECTION                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Public Routes (no auth):                                      │
│   /, /products, /products/:id, /login, /register                │
│                                                                 │
│   ProtectedRoute (authentication required):                     │
│   /checkout, /orders, /profile, /addresses, /wishlist           │
│   → Checks: isAuthenticated                                     │
│   → Redirect: /login                                            │
│                                                                 │
│   AdminRoute (ADMIN role required):                             │
│   /admin, /admin/*, all admin sub-routes                        │
│   → Checks: isAuthenticated AND user.role === 'ADMIN'           │
│   → Shows: Access Denied page if not admin                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Frontend Design Patterns

| # | Pattern | Where Used |
|---|---------|-----------|
| 11 | **Nested Routes + Outlet** 🆕 | Admin layout with persistent sidebar |
| 12 | **Role-Based Route Guard** 🆕 | AdminRoute checks user.role |
| 13 | **Render Props (columns)** 🆕 | DataTable component for flexible tables |
| 14 | **Service Layer** 🆕 | Centralized API calls in service files |
| 15 | **Slice Pattern** 🆕 | Redux Toolkit slices for state management |
| 16 | **Composable Charts** 🆕 | Recharts with responsive containers |


```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                            │
│                    (Web Browser / Mobile App)                   │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/HTTPS
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                         │
│                    (Spring Boot Application)                   │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   Auth      │  │  Product    │  │   Order     │             │
│  │ Controller  │  │ Controller  │  │ Controller  │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Payment    │  │  Webhook    │  │   Admin     │             │
│  │ Controller  │  │ Controller  │  │ Controller  │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Review     │  │   Search    │  │    Sync     │             │
│  │ Controller  │  │ Controller  │  │  Service    │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Admin      │  │ Admin       │  │  Admin User │             │
│  │ Dashboard   │  │ Report      │  │ Controller  │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  (Week 7)   │
│         │                │                │                    │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐             │
│  │   Auth      │  │  Product    │  │   Order     │             │
│  │  Service    │  │  Service    │  │  Service    │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Payment    │  │  Refund     │  │  Stripe     │             │
│  │  Service    │  │  Service    │  │  Service    │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Review     │  │   Search    │  │    Sync     │             │
│  │  Service    │  │  Service    │  │  Service    │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Email      │  │ Dashboard   │  │  Report     │             │
│  │  Service    │  │  Service    │  │  Service    │  (Week 7)   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
├─────────┼────────────────┼────────────────┼────────────────────┤
│         ▼                ▼                ▼                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────┐ │
│  │ PostgreSQL  │  │  MongoDB    │  │   Redis     │  │ Stripe │ │
│  │   Users     │  │  Products   │  │   Cart      │  │  API   │ │
│  │   Orders    │  │ Categories  │  │  Wishlist   │  │        │ │
│  │  Payments   │  │             │  │  Sessions   │  │        │ │
│  │  Addresses  │  │             │  │             │  │        │ │
│  │  Reviews    │  │             │  │             │  │        │ │
│  │ EmailLogs   │  │             │  │             │  │        │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────┘ │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Elasticsearch                        │   │
│  │                    Product Search Index                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Mailtrap SMTP    (Week 7)              │   │
│  │              Email Testing / Delivery Service           │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

---

## Database Architecture

### Polyglot Persistence Strategy

We use different databases for different purposes:

| Database | Use Case | Why |
|----------|----------|-----|
| **PostgreSQL** | Users, Orders, Payments, Addresses, Reviews, Email Logs  | ACID compliance, relational data, transactions |
| **MongoDB** | Products, Categories | Flexible schema, nested data, fast reads |
| **Redis** | Cart, Wishlist, Sessions | In-memory speed, TTL support, temporary data |
| **Stripe** | Payment Processing | PCI compliance, secure payment handling |
| **Elasticsearch** | Product Search | Full-text search, filters, autocomplete |
| **Mailtrap**  | Email Testing | Safe email testing without sending to real inboxes |

---

## PostgreSQL Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    email_verified BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Addresses Table
```sql
CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    landmark VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE NOT NULL,
    user_id UUID REFERENCES users(id),
    user_email VARCHAR(255),
    user_full_name VARCHAR(200),
    
    -- Status
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    
    -- Shipping Address Snapshot
    shipping_address_id UUID,
    shipping_full_name VARCHAR(100),
    shipping_phone_number VARCHAR(20),
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    shipping_landmark VARCHAR(255),
    
    -- Shipping Info
    tracking_number VARCHAR(100),
    shipping_carrier VARCHAR(50),
    
    -- Amounts
    subtotal DECIMAL(10,2),
    tax_rate DECIMAL(5,4),
    tax_amount DECIMAL(10,2),
    shipping_cost DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    amount_refunded DECIMAL(10,2),
    
    -- Notes
    customer_notes TEXT,
    admin_notes TEXT,
    cancellation_reason TEXT,
    cancelled_by VARCHAR(20),
    
    -- Payment (Stripe)
    payment_method VARCHAR(50),
    payment_id VARCHAR(100),
    stripe_payment_intent_id VARCHAR(100),
    stripe_charge_id VARCHAR(100),
    receipt_url VARCHAR(500),
    
    -- Timestamps
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(id),
    
    -- Product Snapshot
    product_id VARCHAR(50),
    product_name VARCHAR(255),
    product_slug VARCHAR(255),
    product_sku VARCHAR(100),
    product_image VARCHAR(500),
    product_brand VARCHAR(100),
    
    -- Pricing
    unit_price DECIMAL(10,2),
    discount_price DECIMAL(10,2),
    effective_price DECIMAL(10,2),
    quantity INTEGER,
    total_price DECIMAL(10,2)
);
```

### Payments Table
```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    
    -- Order Reference
    order_id VARCHAR(50) NOT NULL,
    order_number VARCHAR(20) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    
    -- Stripe References
    stripe_payment_intent_id VARCHAR(100) UNIQUE,
    stripe_charge_id VARCHAR(100),
    stripe_customer_id VARCHAR(100),
    client_secret VARCHAR(500),
    
    -- Payment Details
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30),
    card_last_four VARCHAR(4),
    card_brand VARCHAR(20),
    
    -- Failure Information
    failure_code VARCHAR(100),
    failure_message VARCHAR(500),
    
    -- Refund Information
    amount_refunded DECIMAL(10,2) DEFAULT 0,
    stripe_refund_id VARCHAR(100),
    refund_reason VARCHAR(500),
    
    -- Metadata
    receipt_url VARCHAR(500),
    statement_descriptor VARCHAR(22),
    
    -- Timestamps
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_payment_order_id (order_id),
    INDEX idx_payment_intent_id (stripe_payment_intent_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_user_id (user_id)
);
```

### Reviews Table
```sql
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    
    -- Review Content
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(100),
    comment TEXT,
    
    -- Verification
    verified_purchase BOOLEAN DEFAULT FALSE,
    order_number VARCHAR(20),
    
    -- Engagement
    helpful_count INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT uk_user_product_review UNIQUE (user_id, product_id),
    
    -- Indexes
    INDEX idx_review_product (product_id),
    INDEX idx_review_user (user_id),
    INDEX idx_review_rating (rating),
    INDEX idx_review_created (created_at)
);
```

### Email Logs Table  (Week 7)
```sql
CREATE TABLE email_logs (
    id UUID PRIMARY KEY,
    
    -- Recipient
    user_id UUID REFERENCES users(id),
    recipient_email VARCHAR(255) NOT NULL,
    
    -- Email Details
    email_type VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    
    -- Reference
    order_number VARCHAR(20),
    
    -- Timestamps
    created_at TIMESTAMP,
    sent_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_email_log_user (user_id),
    INDEX idx_email_log_status (status),
    INDEX idx_email_log_type (email_type)
);
```

---

## MongoDB Schema

### Products Collection
```javascript
{
  "_id": ObjectId,
  "name": "iPhone 15 Pro",
  "slug": "iphone-15-pro",
  "description": "Latest Apple smartphone",
  "price": 999.99,
  "discountPrice": 949.99,
  "stock": 100,
  "sku": "IPHONE-15-PRO",
  "brand": "Apple",
  "categoryId": ObjectId,
  "images": [
    {
      "url": "https://cloudinary.com/...",
      "publicId": "products/abc123",
      "isPrimary": true
    }
  ],
  "tags": ["smartphone", "apple", "5g"],
  "specifications": {
    "color": "Space Black",
    "storage": "256GB"
  },
  "active": true,
  "averageRating": 4.5,
  "reviewCount": 128,
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

### Categories Collection
```javascript
{
  "_id": ObjectId,
  "name": "Smartphones",
  "slug": "smartphones",
  "description": "Mobile phones and accessories",
  "parentId": ObjectId | null,
  "imageUrl": "https://cloudinary.com/...",
  "active": true,
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

---

## Elasticsearch Schema

### Products Index
```javascript
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "name": { "type": "text", "analyzer": "standard" },
      "description": { "type": "text", "analyzer": "standard" },
      "sku": { "type": "keyword" },
      "slug": { "type": "keyword" },
      "brand": { "type": "keyword" },
      "price": { "type": "double" },
      "salePrice": { "type": "double" },
      "stock": { "type": "integer" },
      "active": { "type": "boolean" },
      "categoryId": { "type": "keyword" },
      "categoryName": { "type": "keyword" },
      "categorySlug": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "images": { "type": "keyword" },
      "averageRating": { "type": "double" },
      "reviewCount": { "type": "integer" },
      "createdAt": { "type": "date" },
      "updatedAt": { "type": "date" }
    }
  }
}
```

---

## Redis Data Structure

### Cart
```
Key: cart:{userId}
Type: String (JSON)
TTL: 30 days

{
  "userId": "uuid",
  "items": [
    {
      "productId": "product-id",
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 999.99,
      "effectivePrice": 949.99,
      "addedAt": "2026-01-21T10:00:00"
    }
  ],
  "updatedAt": "2026-01-21T10:30:00"
}
```

### Wishlist
```
Key: wishlist:{userId}
Type: String (JSON)
TTL: 90 days
```

---

## Email Notification Architecture  (Week 7)

### Email Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                      EMAIL NOTIFICATION FLOW                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Trigger Events                                                │
│       │                                                         │
│       ├──► User Registration ──► Welcome Email                  │
│       │                                                         │
│       ├──► Payment Success ──► Order Confirmation Email         │
│       │    (Webhook)                                            │
│       │                                                         │
│       ├──► Status → SHIPPED ──► Shipping Notification Email     │
│       │    (Admin action)       (includes tracking number)      │
│       │                                                         │
│       ├──► Status → DELIVERED ──► Delivery Confirmation Email   │
│       │    (Admin action)                                       │
│       │                                                         │
│       ├──► Order Cancelled ──► Cancellation Email               │
│       │                        (includes refund info)           │
│       │                                                         │
│       └──► Password Reset ──► Password Reset Email              │
│            (User request)      (includes reset link)            │
│                                                                 │
│   Processing                                                    │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────────┐                                       │
│   │   EmailService      │                                       │
│   │   (@Async)          │                                       │
│   │                     │                                       │
│   │ 1. Build context    │                                       │
│   │    (order details,  │                                       │
│   │     user info)      │                                       │
│   │                     │                                       │
│   │ 2. Resolve template │───────► Thymeleaf Template Engine     │
│   │    (HTML rendering) │         src/main/resources/templates/ │
│   │                     │         email/*.html                  │
│   │                     │                                       │
│   │ 3. Send via SMTP    │───────► Mailtrap (Dev) / Gmail (Prod) │
│   │    (JavaMailSender) │                                       │
│   │                     │                                       │
│   │ 4. Log result       │───────► PostgreSQL (email_logs)       │
│   │    (SENT/FAILED)    │                                       │
│   └─────────────────────┘                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Email Templates
```
src/main/resources/templates/email/
├── welcome.html              # New user registration
├── order-confirmation.html   # Order confirmed after payment
├── order-shipped.html        # Order shipped with tracking
├── order-delivered.html      # Order delivered
├── order-cancelled.html      # Order cancelled (with refund info)
└── password-reset.html       # Password reset link
```

### Email Provider Configuration
```
┌─────────────────────────────────────────────────────────────────┐
│              EMAIL PROVIDER SWITCHING                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Development (Mailtrap):                                       │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  host: sandbox.smtp.mailtrap.io                         │   │
│   │  port: 2525                                             │   │
│   │  All emails caught in Mailtrap inbox                    │   │
│   │  No real emails sent                                    │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│   Production (Gmail/SendGrid/AWS SES):                          │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  host: smtp.gmail.com (or provider SMTP)                │   │
│   │  port: 587                                              │   │
│   │  Real emails delivered to recipients                    │   │
│   │  NO CODE CHANGES NEEDED - only application.yml          │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Admin Dashboard Architecture  (Week 7)

### Dashboard Data Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                  ADMIN DASHBOARD FLOW                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   GET /api/admin/dashboard/stats                                │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────────┐                                       │
│   │  DashboardService   │                                       │
│   │                     │                                       │
│   │  Aggregates from:   │                                       │
│   │  ├── OrderRepository│──► Order counts, revenue totals       │
│   │  ├── UserRepository │──► User registration stats            │
│   │  ├── ProductRepo    │──► Product/stock counts (MongoDB)     │
│   │  └── ReviewRepo     │──► Average ratings                    │
│   └─────────────────────┘                                       │
│                                                                 │
│   GET /api/admin/reports/sales?period=weekly                    │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────────┐                                       │
│   │   ReportService     │                                       │
│   │                     │                                       │
│   │  Generates:         │                                       │
│   │  ├── Daily revenue  │──► JPA aggregate queries              │
│   │  ├── Weekly summary │──► Date range calculations            │
│   │  ├── Monthly report │──► Period comparisons                 │
│   │  ├── Category sales │──► Cross-database joins               │
│   │  └── Top customers  │──► User spending aggregation          │
│   └─────────────────────┘                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Search Architecture

### Search Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                      SEARCH FLOW                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   User Query: "gaming laptop"                                   │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────┐                                           │
│   │SearchController │                                           │
│   │ GET /api/search │                                           │
│   └────────┬────────┘                                           │
│            │                                                    │
│            ▼                                                    │
│   ┌─────────────────────┐                                       │
│   │ ProductSearchService│                                       │
│   │                     │                                       │
│   │ • Build bool query  │                                       │
│   │ • Add filters       │                                       │
│   │ • Apply sorting     │                                       │
│   │ • Execute search    │                                       │
│   └────────┬────────────┘                                       │
│            │                                                    │
│            ▼                                                    │
│   ┌─────────────────────┐                                       │
│   │    Elasticsearch    │                                       │
│   │                     │                                       │
│   │ Multi-match query:  │                                       │
│   │ • name^3            │  (boosted)                            │
│   │ • description^2     │  (boosted)                            │
│   │ • brand^2           │  (boosted)                            │
│   │ • tags              │                                       │
│   │                     │                                       │
│   │ Filters:            │                                       │
│   │ • price range       │                                       │
│   │ • category          │                                       │
│   │ • brand             │                                       │
│   │ • rating            │                                       │
│   │ • in stock          │                                       │
│   └─────────────────────┘                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Data Sync Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                 MongoDB → Elasticsearch Sync                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Product Changes in MongoDB                                    │
│       │                                                         │
│       ├──► Create Product ──► syncProduct() ──► ES Index        │
│       │                                                         │
│       ├──► Update Product ──► syncProduct() ──► ES Update       │
│       │                                                         │
│       ├──► Delete Product ──► removeProduct() ──► ES Delete     │
│       │                                                         │
│       └──► Rating Change ──► updateProductRating() ──► ES Update│
│                                                                 │
│   Admin Manual Sync                                             │
│       │                                                         │
│       ├──► Full Sync ──► syncAllProducts() ──► Reindex All      │
│       │                                                         │
│       ├──► Single Product ──► syncProductById() ──► ES Update   │
│       │                                                         │
│       └──► Category Reindex ──► reindexCategory() ──► ES Update │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Review System Architecture

### Review Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                      REVIEW FLOW                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   User creates review                                           │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────┐                                           │
│   │ ReviewController│                                           │
│   │ POST /api/reviews│                                          │
│   └────────┬────────┘                                           │
│            │                                                    │
│            ▼                                                    │
│   ┌─────────────────────┐                                       │
│   │   ReviewService     │                                       │
│   │                     │                                       │
│   │ 1. Verify product   │───────► MongoDB (Products)            │
│   │    exists           │                                       │
│   │                     │                                       │
│   │ 2. Check duplicate  │───────► PostgreSQL (Reviews)          │
│   │    review           │                                       │
│   │                     │                                       │
│   │ 3. Verify purchase  │───────► PostgreSQL (Orders)           │
│   │    (for badge)      │         - Check DELIVERED orders      │
│   │                     │         - Match product ID            │
│   │                     │                                       │
│   │ 4. Create review    │───────► PostgreSQL (Reviews)          │
│   │                     │                                       │
│   │ 5. Update rating    │───────► ProductSyncService            │
│   │    in search index  │         - MongoDB (averageRating)     │
│   │                     │         - Elasticsearch (update)      │
│   └─────────────────────┘                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Verified Purchase Logic
```
┌─────────────────────────────────────────────────────────────────┐
│              VERIFIED PURCHASE DETERMINATION                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   When user submits review:                                     │
│                                                                 │
│   1. Query: Find user's DELIVERED orders                        │
│      SELECT * FROM orders                                       │
│      WHERE user_id = :userId AND status = 'DELIVERED'           │
│                                                                 │
│   2. For each order, check if product exists in order_items     │
│      SELECT * FROM order_items                                  │
│      WHERE order_id = :orderId AND product_id = :productId      │
│                                                                 │
│   3. If found:                                                  │
│      ┌─────────────────────────────────────────┐                │
│      │ verifiedPurchase = true                 │                │
│      │ orderNumber = found order's number      │                │
│      │ Badge: ✓ Verified Purchase              │                │
│      └─────────────────────────────────────────┘                │
│                                                                 │
│   4. If not found:                                              │
│      ┌─────────────────────────────────────────┐                │
│      │ verifiedPurchase = false                │                │
│      │ orderNumber = null                      │                │
│      │ No badge displayed                      │                │
│      └─────────────────────────────────────────┘                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Payment Flow Architecture

### Payment Intent Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                      PAYMENT FLOW                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   1. Place Order (POST /api/checkout/place-order)               │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Create order with status PENDING                      │  │
│   │  • Set payment_status to PENDING                         │  │
│   │  • Stock NOT reduced yet                                 │  │
│   │  • Cart cleared                                          │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   2. Create Payment Intent (POST /api/payments/create-intent)   │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Call Stripe PaymentIntent.create()                    │  │
│   │  • Store Payment record in PostgreSQL                    │  │
│   │  • Update order with stripePaymentIntentId               │  │
│   │  • Set payment_status to AWAITING_PAYMENT                │  │
│   │  • Return clientSecret to frontend                       │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   3. Frontend Payment Confirmation (Stripe.js)                  │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  stripe.confirmCardPayment(clientSecret, {               │  │
│   │    payment_method: { card: cardElement }                 │  │
│   │  });                                                     │  │
│   │  • User enters card details                              │  │
│   │  • Stripe handles 3D Secure if required                  │  │
│   │  • Payment processed by Stripe                           │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   4. Webhook Notification (POST /api/webhooks/stripe)           │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Stripe sends payment_intent.succeeded                 │  │
│   │  • Verify webhook signature                              │  │
│   │  • Update Payment record (PAID, chargeId, receiptUrl)    │  │
│   │  • Update Order (CONFIRMED, paidAt)                      │  │
│   │  • REDUCE STOCK NOW                                      │  │
│   │  • 📧 Send order confirmation email                      │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Why Webhooks?

```
┌──────────────────────────────────────────────────────────────┐
│                WHY WEBHOOKS ARE CRITICAL                     │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Without Webhooks (UNRELIABLE):                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  User → Stripe → Success → Redirect → Your Server      │  │
│  │                      ↓                                 │  │
│  │              User closes browser                       │  │
│  │              Network fails                             │  │
│  │              ❌ Payment received but order not updated │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  With Webhooks (RELIABLE):                                   │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Stripe → Webhook → Your Server                        │  │
│  │    │                                                   │  │
│  │    └── Guaranteed delivery (with retries)              │  │
│  │    └── Signature verification for security             │  │
│  │    └── Source of truth for payment status              │  │
│  │    ✅ Payment always properly recorded                 │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Refund Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                       REFUND FLOW                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   1. Check Eligibility                                          │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Payment status must be PAID                           │  │
│   │  • Within refund window (30 days default)                │  │
│   │  • Has refundable amount remaining                       │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   2. Process Refund (POST /api/admin/payments/refund)           │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Call Stripe Refund.create()                           │  │
│   │  • Update Payment record (amountRefunded, status)        │  │
│   │  • Update Order (status, amountRefunded)                 │  │
│   │  • Optionally restore stock                              │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│               ┌──────────────┴──────────────┐                   │
│               ▼                             ▼                   │
│   ┌─────────────────────┐      ┌─────────────────────┐          │
│   │   Partial Refund    │      │    Full Refund      │          │
│   │   ───────────────   │      │    ───────────      │          │
│   │   Status: PARTIAL   │      │   Status: REFUNDED  │          │
│   │   More refundable   │      │   Order: CANCELLED  │          │
│   └─────────────────────┘      └─────────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Order Status State Machine

```
                    ┌─────────────┐
                    │   PENDING   │ ◄── Order Created
                    └──────┬──────┘
                           │
              ┌────────────┼───────────┐
              │            │           │
              ▼            ▼           │
        ┌──────────┐ ┌──────────┐      │
        │CANCELLED │ │CONFIRMED │      │ (After payment)
        └──────────┘ └────┬─────┘      │  📧 Order Confirmation 
                          │            │
                          ▼            │
                   ┌────────────┐      │
                   │ PROCESSING │      │
                   └─────┬──────┘      │
                         │             │
            ┌────────────┼─────────┐   │
            │            │         │   │
            ▼            ▼         │   │
      ┌──────────┐ ┌──────────┐    │   │
      │CANCELLED │ │  SHIPPED │    │   │
      └──────────┘ └────┬─────┘    │   │
       📧 Cancel        │  📧 Ship    │
              ┌─────────┼─────────┐│   │
              │         │         ││   │
              ▼         ▼         ││   │
        ┌──────────┐ ┌──────────┐ ││   │
        │DELIVERED │ │ RETURNED │ ││   │
        └──────────┘ └────┬─────┘ ││   │
      📧 Deliver          │       ││   │
                          ▼       ││   │
                    ┌──────────┐  ││   │
                    │ REFUNDED │◄─┴┴───┘
                    └──────────┘
```

---

## Payment Status State Machine

```
                    ┌─────────────┐
                    │   PENDING   │ ◄── Order Created
                    └──────┬──────┘
                           │
                           ▼
               ┌───────────────────────┐
               │   AWAITING_PAYMENT    │ ◄── Payment Intent Created
               └───────────┬───────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  FAILED  │ │   PAID   │ │CANCELLED │
        └──────────┘ └────┬─────┘ └──────────┘
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
     ┌─────────────── ──┐      ┌──────────┐
     │PARTIALLY_REFUNDED│      │ REFUNDED │
     └────────┬────── ──┘      └──────────┘
              │
              ▼
        ┌──────────┐
        │ REFUNDED │
        └──────────┘
```

---

## Cross-Database Transaction Handling

Since we use multiple databases, we handle distributed transactions carefully:

```
┌────────────────────────────────────────────────────────────────┐
│              Payment Success Transaction (Webhook)             │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  @Transactional (PostgreSQL)                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  1. Update Payment → 2. Update Order → 3. Reduce Stock  │   │
│  │     (PostgreSQL)       (PostgreSQL)       (MongoDB)     │   │
│  │                                                         │   │
│  │  4. Send Confirmation Email (@Async)                    │   │
│  │     (Non-blocking, failure doesn't affect transaction)  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                 │
│                              ▼                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  If MongoDB stock reduction fails:                      │   │
│  │  - PostgreSQL changes are rolled back automatically     │   │
│  │  - Webhook returns error (Stripe will retry)            │   │
│  │  - Log error for manual investigation                   │   │
│  │                                                         │   │
│  │  If Email sending fails:                                │   │
│  │  - Order still processed successfully                   │   │
│  │  - Email failure logged to email_logs table             │   │
│  │  - Does NOT affect order/payment transaction            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

### Compensation Pattern
For refunds, we use compensation:
```
Process Refund:
1. Call Stripe Refund API (external)
2. Update Payment record (PostgreSQL)
3. Update Order status (PostgreSQL)
4. Restore stock in MongoDB (optional, compensation)
5. Send cancellation email (@Async) 
6. All succeed → Success
7. Stock restore fails → Log error, manual intervention needed
8. Email fails → Logged, doesn't affect refund
```

---

## Security Architecture

### Authentication Flow
```
┌──────────┐    1. Login     ┌──────────┐
│  Client  │ ──────────────► │  Server  │
└──────────┘                 └────┬─────┘
                                  │
     2. JWT Token                 │
◄─────────────────────────────────┘
     
┌──────────┐  3. Request+JWT  ┌──────────┐
│  Client  │ ───────────────► │  Server  │
└──────────┘                  └────┬─────┘
                                   │
     4. Protected Resource         │
◄──────────────────────────────────┘
```

### Webhook Security
```
┌──────────┐   Webhook + Signature   ┌──────────┐
│  Stripe  │ ──────────────────────► │  Server  │
└──────────┘                         └────┬─────┘
                                          │
                                   Verify Signature
                                   using Webhook Secret
                                          │
                              ┌───────────┴───────────┐
                              │                       │
                          Valid                   Invalid
                              │                       │
                              ▼                       ▼
                        Process Event           Reject (401)
```

### Authorization Levels
| Role | Permissions |
|------|-------------|
| PUBLIC | View products, categories, search, reviews (read), Stripe webhooks |
| CUSTOMER | Cart, wishlist, orders, addresses, payments, reviews (write) |
| ADMIN | All + product/category CRUD + order/payment management + refunds + search sync + dashboard  + reports + user management  |

---

## Configuration Management

```yaml
shopzone:
  order:
    tax-rate: 0.08                    # 8% tax
    free-shipping-threshold: 50.00    # Free shipping over $50
    flat-shipping-rate: 5.99          # Otherwise $5.99
    cancellation-window-hours: 24     # Cancel within 24hrs
  
  payment:
    refund-window-days: 30            # Refund within 30 days

stripe:
  secret-key: ${STRIPE_SECRET_KEY}
  public-key: ${STRIPE_PUBLIC_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  currency: usd

spring:
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 5s
    socket-timeout: 30s

  mail:                               #  Week 7
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: ${MAILTRAP_USERNAME}
    password: ${MAILTRAP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

---

## Design Patterns Used

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

### 5. Snapshot Pattern
- Order preserves address/product data at order time
- Protects against future changes
- Maintains historical accuracy

### 6. State Machine Pattern
- Order status transitions validated
- Payment status transitions validated
- Invalid transitions rejected

### 7. Webhook Handler Pattern
- Idempotent event processing
- Signature verification
- Event type routing

### 8. Sync Service Pattern
- Keeps Elasticsearch in sync with MongoDB
- Handles create/update/delete events
- Supports full and incremental sync

### 9. Template Method Pattern (Week 7)
- Thymeleaf templates for email rendering
- Consistent email structure with variable content
- Separation of email design from business logic

### 10. Async Processing Pattern  (Week 7)
- Email sending is non-blocking (@Async)
- Doesn't affect main transaction flow
- Failures logged independently

---

## Future Architecture (Microservices - Phase 3+)

```
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
     └────────────┴────────────┴────────────┴────────────┘
                              │
                    ┌─────────┴─────────┐
                    │   Apache Kafka    │
                    │   (Event Bus)     │
                    └───────────────────┘
```