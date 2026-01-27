# ShopZone Architecture

## System Overview

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
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐             │
│  │   Auth      │  │  Product    │  │   Order     │             │
│  │  Service    │  │  Service    │  │  Service    │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
├─────────┼────────────────┼────────────────┼────────────────────┤
│         ▼                ▼                ▼                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ PostgreSQL  │  │  MongoDB    │  │   Redis     │             │
│  │   Users     │  │  Products   │  │   Cart      │             │
│  │   Orders    │  │ Categories  │  │  Wishlist   │             │
│  │  Addresses  │  │             │  │  Sessions   │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
└────────────────────────────────────────────────────────────────┘
```

---

## Database Architecture

### Polyglot Persistence Strategy

We use different databases for different purposes:

| Database | Use Case | Why |
|----------|----------|-----|
| **PostgreSQL** | Users, Orders, Addresses | ACID compliance, relational data, transactions |
| **MongoDB** | Products, Categories | Flexible schema, nested data, fast reads |
| **Redis** | Cart, Wishlist, Sessions | In-memory speed, TTL support, temporary data |

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

### Orders Table 🆕
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
    
    -- Notes
    customer_notes TEXT,
    admin_notes TEXT,
    cancellation_reason TEXT,
    cancelled_by VARCHAR(20),
    
    -- Payment
    payment_method VARCHAR(50),
    payment_id VARCHAR(100),
    
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

### Order Items Table 🆕
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

## Order Flow Architecture 🆕

### Checkout Process
```
┌─────────────────────────────────────────────────────────────────┐
│                      CHECKOUT FLOW                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   1. Validate Cart                                              │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Check cart not empty                                  │  │
│   │  • Verify products still exist                           │  │
│   │  • Check stock availability                              │  │
│   │  • Validate prices haven't changed significantly         │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   2. Calculate Totals                                           │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Subtotal = Σ(effectivePrice × quantity)               │  │
│   │  • Tax = subtotal × taxRate (8%)                         │  │
│   │  • Shipping = $0 if subtotal > $50, else $5.99           │  │
│   │  • Total = subtotal + tax + shipping                     │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   3. Create Order (Transactional)                               │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  • Generate order number (ORD-YYYYMMDD-XXXX)             │  │
│   │  • Snapshot address and product data                     │  │
│   │  • Save order to PostgreSQL                              │  │
│   │  • Reduce stock in MongoDB                               │  │
│   │  • Clear cart in Redis                                   │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Order Status State Machine
```
                    ┌─────────────┐
                    │   PENDING   │ ◄── Order Created
                    └──────┬──────┘
                           │
              ┌────────────┼───────────┐
              │            │           │
              ▼            ▼           │
        ┌──────────┐ ┌──────────┐      │
        │CANCELLED │ │CONFIRMED │      │
        └──────────┘ └────┬─────┘      │
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
                        │          │   │
              ┌─────────┼─────────┐│   │
              │         │         ││   │
              ▼         ▼         ││   │
        ┌──────────┐ ┌──────────┐ ││   │
        │DELIVERED │ │ RETURNED │ ││   │
        └──────────┘ └────┬─────┘ ││   │
                          │       ││   │
                          ▼       ││   │
                    ┌──────────┐  ││   │
                    │ REFUNDED │◄─┴┴───┘
                    └──────────┘
```

---

## Cross-Database Transaction Handling 🆕

Since we use multiple databases, we handle distributed transactions carefully:

```
┌────────────────────────────────────────────────────────────────┐
│                  Order Placement Transaction                   │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  @Transactional (PostgreSQL)                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  1. Create Order ──► 2. Reduce Stock ──► 3. Clear Cart  │   │
│  │     (PostgreSQL)        (MongoDB)          (Redis)      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                 │
│                              ▼                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  If MongoDB stock reduction fails:                      │   │
│  │  - PostgreSQL order is rolled back automatically        │   │
│  │  - Application throws exception                         │   │
│  │  - User sees error message                              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

### Compensation Pattern
For order cancellation, we use compensation:
```
Cancel Order:
1. Update order status to CANCELLED (PostgreSQL)
2. Restore stock in MongoDB (compensation)
3. Both operations succeed → Success
4. Stock restore fails → Log error, manual intervention needed
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

### Authorization Levels
| Role | Permissions |
|------|-------------|
| PUBLIC | View products, categories |
| CUSTOMER | Cart, wishlist, orders, addresses |
| ADMIN | All + product/category CRUD + order management |

---

## Configuration Management

```yaml
shopzone:
  order:
    tax-rate: 0.08                    # 8% tax
    free-shipping-threshold: 50.00    # Free shipping over $50
    flat-shipping-rate: 5.99          # Otherwise $5.99
    cancellation-window-hours: 24     # Cancel within 24hrs
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

### 5. Snapshot Pattern 🆕
- Order preserves address/product data at order time
- Protects against future changes
- Maintains historical accuracy

### 6. State Machine Pattern 🆕
- Order status transitions validated
- Invalid transitions rejected
- Clear workflow enforcement

---

## Future Architecture (Microservices - Phase 2+)

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