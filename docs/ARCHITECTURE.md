# ShopZone Architecture Documentation

## System Overview

ShopZone uses a **polyglot persistence** architecture with multiple specialized databases for different data types.

```
                                    ┌─────────────────┐
                                    │   Cloudinary    │
                                    │  (Image CDN)    │
                                    └────────▲────────┘
                                             │
┌──────────┐      ┌──────────────────────────┴───────────────────────────┐
│  Client  │────▶ │                   Spring Boot API                    │
└──────────┘      │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
                  │  │   Auth      │  │  Product    │  │  Category   │   │
                  │  │  Service    │  │  Service    │  │  Service    │   │
                  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘   │
                  └─────────┼────────────────┼────────────────┼──────────┘
                            │                │                │
                            ▼                └───────┬────────┘
                    ┌──────────────┐                 ▼
                    │  PostgreSQL  │         ┌──────────────┐
                    │   (Users)    │         │   MongoDB    │
                    └──────────────┘         │  (Products)  │
                                             └──────────────┘
```

---

## Database Strategy

### Why Two Databases?

| Database | Use Case | Reason |
|----------|----------|--------|
| **PostgreSQL** | Users, Orders, Transactions | ACID compliance, relational integrity, secure transactions |
| **MongoDB** | Products, Categories | Flexible schema, nested documents, fast catalog reads |

### PostgreSQL Schema (Users)

```sql
users
├── id (UUID, PK)
├── email (UNIQUE)
├── password (BCrypt)
├── first_name
├── last_name
├── phone
├── role (ENUM: CUSTOMER, ADMIN)
├── email_verified
├── verification_token
├── reset_token
├── reset_token_expiry
├── created_at
└── updated_at
```

### MongoDB Collections

#### products

```javascript
{
  _id: ObjectId,
  name: String,
  description: String,
  slug: String (indexed, unique),
  sku: String (indexed, unique),
  price: Decimal128,
  discountPrice: Decimal128,
  discountPercentage: Number,
  stock: Number,
  categoryId: String (indexed),
  brand: String (indexed),
  images: [String],
  tags: [String],
  active: Boolean (indexed),
  featured: Boolean (indexed),
  details: {
    weight: String,
    dimensions: String,
    color: String,
    size: String,
    material: String,
    specifications: Object
  },
  createdAt: Date,
  updatedAt: Date
}

// Indexes
{ slug: 1 }
{ sku: 1 }
{ categoryId: 1, active: 1 }
{ brand: 1, active: 1 }
{ price: 1, active: 1 }
{ featured: 1, active: 1 }
{ name: "text", description: "text", brand: "text" }
```

#### categories

```javascript
{
  _id: ObjectId,
  name: String,
  description: String,
  slug: String (indexed, unique),
  parentId: String (indexed),
  level: Number,
  path: String,
  imageUrl: String,
  active: Boolean,
  displayOrder: Number,
  createdAt: Date,
  updatedAt: Date
}

// Indexes
{ slug: 1 }
{ parentId: 1, active: 1 }
{ level: 1 }
```

---

## Application Layers

```
┌─────────────────────────────────────────────────────────────┐
│                      Controller Layer                       │
│  - REST endpoints                                           │
│  - Request validation                                       │
│  - Response formatting                                      │
│  - @PreAuthorize for security                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                         │
│  - Business logic                                           │
│  - Transaction management                                   │
│  - DTO mapping                                              │
│  - Validation rules                                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                       │
│  - JpaRepository (PostgreSQL)                               │
│  - MongoRepository (MongoDB)                                │
│  - Custom queries with @Query                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  - PostgreSQL (Users, Auth)                                 │
│  - MongoDB (Products, Categories)                           │
│  - Cloudinary (Images)                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## Security Architecture

### Authentication Flow

```
1. User Login
   └─▶ AuthController.login()
       └─▶ AuthService.authenticate()
           ├─▶ Validate credentials
           ├─▶ Generate Access Token (24h)
           ├─▶ Generate Refresh Token (7d)
           └─▶ Return tokens

2. Protected Request
   └─▶ JwtAuthenticationFilter
       ├─▶ Extract token from header
       ├─▶ Validate token signature
       ├─▶ Check expiration
       ├─▶ Load user details
       └─▶ Set SecurityContext
```

### Endpoint Security Matrix

| Endpoint Pattern | Public | Customer | Admin |
|-----------------|--------|----------|-------|
| POST /api/auth/** | ✅ | ✅ | ✅ |
| GET /api/products/** | ✅ | ✅ | ✅ |
| GET /api/categories/** | ✅ | ✅ | ✅ |
| POST /api/products | ❌ | ❌ | ✅ |
| PUT /api/products/* | ❌ | ❌ | ✅ |
| DELETE /api/products/* | ❌ | ❌ | ✅ |
| POST /api/categories | ❌ | ❌ | ✅ |
| PUT /api/categories/* | ❌ | ❌ | ✅ |
| DELETE /api/categories/* | ❌ | ❌ | ✅ |

---

## Image Storage Architecture

### Why Cloudinary?

- **Free tier**: 25GB storage, 25GB bandwidth/month
- **CDN**: Global content delivery
- **Transformations**: Resize, crop, optimize on-the-fly
- **No server storage**: Offload storage to cloud

### Upload Flow

```
1. Client uploads image
   └─▶ ProductController.uploadImages()
       └─▶ CloudinaryService.uploadImage()
           ├─▶ Validate file type & size
           ├─▶ Upload to Cloudinary
           │   └─▶ Folder: products/{product_id}
           ├─▶ Get secure URL
           └─▶ Add URL to product.images[]

2. Image URL structure
   https://res.cloudinary.com/{cloud}/image/upload/v{version}/{folder}/{public_id}.{format}
```

---

## Category Hierarchy Design

### Tree Structure

```
Electronics (level: 0, path: "/electronics")
├── Smartphones (level: 1, path: "/electronics/smartphones")
│   ├── Android (level: 2, path: "/electronics/smartphones/android")
│   └── iPhone (level: 2, path: "/electronics/smartphones/iphone")
├── Laptops (level: 1, path: "/electronics/laptops")
└── Headphones (level: 1, path: "/electronics/headphones")
```

### Breadcrumb Generation

```java
// For category: iPhone
breadcrumbs = [
  { name: "Electronics", slug: "electronics" },
  { name: "Smartphones", slug: "smartphones" },
  { name: "iPhone", slug: "iphone" }
]
```

---

## Search Implementation

### Search Strategy

Uses MongoDB regex for flexible matching:

```javascript
// Search query
{
  "$or": [
    { "name": { "$regex": "apple", "$options": "i" } },
    { "description": { "$regex": "apple", "$options": "i" } },
    { "brand": { "$regex": "apple", "$options": "i" } },
    { "tags": { "$regex": "apple", "$options": "i" } }
  ],
  "active": true
}
```

### Why Regex over Text Index?

- **Flexibility**: Partial matching works
- **No index setup**: Works out of the box
- **Good enough**: For current scale

### Future Improvement

For larger scale, consider:

- MongoDB Atlas Search
- Elasticsearch
- Algolia

---

## Error Handling Strategy

### Global Exception Handler

```
Exception
├── ResourceNotFoundException → 404
├── BadRequestException → 400
├── UnauthorizedException → 401
├── AccessDeniedException → 403
├── DuplicateResourceException → 409
└── Exception → 500
```

### Standard Error Response

```json
{
  "success": false,
  "message": "Human-readable error message",
  "data": {
    "field": "Specific field error"
  },
  "timestamp": "2024-12-28T10:30:00Z"
}
```

---

## Performance Considerations

### Indexes Created

- `products.slug` - Unique, for URL lookups
- `products.sku` - Unique, for inventory
- `products.categoryId` - For category filtering
- `products.brand` - For brand filtering
- `products.price` - For price range queries
- `products.active` - For filtering active products
- `categories.slug` - For URL lookups
- `categories.parentId` - For tree queries

### Pagination

All list endpoints support pagination:

- Default page size: 12
- Maximum page size: 100
- Sorted by createdAt DESC by default

---

## Future Architecture (Phase 2)

```
                    ┌─────────────┐
                    │ API Gateway │
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │ Auth Service│ │Product Svc  │ │ Order Svc   │
    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │ PostgreSQL  │ │  MongoDB    │ │ PostgreSQL  │
    └─────────────┘ └─────────────┘ └─────────────┘
```