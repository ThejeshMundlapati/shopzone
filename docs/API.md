# ShopZone API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your_token>
```

---

## 🔐 Authentication APIs

### Register User
```http
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

### Login
```http
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Get Current User 🔒
```http
GET /api/auth/me
```

---

## 📦 Product APIs

### List Products (Public)
```http
GET /api/products?page=0&size=10&sort=createdAt,desc
```

**Query Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| page | int | Page number (0-indexed) |
| size | int | Items per page |
| sort | string | Sort field and direction |
| search | string | Search in name, description, brand |
| categoryId | string | Filter by category |
| minPrice | decimal | Minimum price |
| maxPrice | decimal | Maximum price |
| brand | string | Filter by brand |

### Get Product (Public)
```http
GET /api/products/{id}
```

### Create Product 🔒 (Admin)
```http
POST /api/products
```

**Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Apple's latest flagship",
  "price": 999.99,
  "discountPrice": 949.99,
  "stock": 50,
  "categoryId": "category-id",
  "brand": "Apple",
  "tags": ["smartphone", "apple", "5g"]
}
```

### Update Product 🔒 (Admin)
```http
PUT /api/products/{id}
```

### Delete Product 🔒 (Admin)
```http
DELETE /api/products/{id}
```

### Upload Product Images 🔒 (Admin)
```http
POST /api/products/{id}/images
Content-Type: multipart/form-data
```

---

## 📂 Category APIs

### List Categories (Public)
```http
GET /api/categories
```

### Get Category (Public)
```http
GET /api/categories/{id}
```

### Create Category 🔒 (Admin)
```http
POST /api/categories
```

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices",
  "parentId": null
}
```

---

## 🛒 Cart APIs (All require authentication)

### Get Cart 🔒
```http
GET /api/cart
```

**Response:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "userId": "user-id",
    "items": [
      {
        "productId": "product-id",
        "productName": "iPhone 15 Pro",
        "productSlug": "iphone-15-pro",
        "price": 999.99,
        "discountPrice": 949.99,
        "effectivePrice": 949.99,
        "quantity": 2,
        "imageUrl": "https://...",
        "availableStock": 50,
        "subtotal": 1899.98,
        "savings": 100.00,
        "isValid": true,
        "addedAt": "2024-01-01T10:00:00"
      }
    ],
    "totalItems": 2,
    "uniqueItemCount": 1,
    "subtotal": 1899.98,
    "totalSavings": 100.00,
    "isEmpty": false,
    "hasInvalidItems": false,
    "invalidItems": []
  }
}
```

### Add to Cart 🔒
```http
POST /api/cart/add
```

**Request Body:**
```json
{
  "productId": "product-id",
  "quantity": 2
}
```

### Update Cart Item 🔒
```http
PUT /api/cart/update
```

**Request Body:**
```json
{
  "productId": "product-id",
  "quantity": 3
}
```

### Remove from Cart 🔒
```http
DELETE /api/cart/remove/{productId}
```

### Clear Cart 🔒
```http
DELETE /api/cart/clear
```

### Validate Cart for Checkout 🔒
```http
GET /api/cart/validate
```

---

## ❤️ Wishlist APIs (All require authentication)

### Get Wishlist 🔒
```http
GET /api/wishlist
```

**Response:**
```json
{
  "success": true,
  "message": "Wishlist retrieved successfully",
  "data": {
    "userId": "user-id",
    "items": [
      {
        "productId": "product-id",
        "productName": "Samsung Galaxy S24",
        "productSlug": "samsung-galaxy-s24",
        "price": 899.99,
        "discountPrice": 849.99,
        "effectivePrice": 849.99,
        "imageUrl": "https://...",
        "inStock": true,
        "hasDiscount": true,
        "discountPercentage": 6,
        "addedAt": "2024-01-01T10:00:00"
      }
    ],
    "itemCount": 1,
    "inStockCount": 1,
    "onSaleCount": 1,
    "isEmpty": false
  }
}
```

### Add to Wishlist 🔒
```http
POST /api/wishlist/add/{productId}
```

### Remove from Wishlist 🔒
```http
DELETE /api/wishlist/remove/{productId}
```

### Move to Cart 🔒
```http
POST /api/wishlist/move-to-cart/{productId}
```

### Move All to Cart 🔒
```http
POST /api/wishlist/move-all-to-cart
```

### Check if in Wishlist 🔒
```http
GET /api/wishlist/check/{productId}
```

### Clear Wishlist 🔒
```http
DELETE /api/wishlist/clear
```

---

## 📍 Address APIs (All require authentication)

### Get All Addresses 🔒
```http
GET /api/addresses
```

**Response:**
```json
{
  "success": true,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "id": "address-id",
      "userId": "user-id",
      "fullName": "John Doe",
      "phoneNumber": "+1234567890",
      "addressLine1": "123 Main Street",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA",
      "landmark": "Near Central Park",
      "addressType": "HOME",
      "isDefault": true,
      "formattedAddress": "123 Main Street, Apt 4B (Near Central Park), New York, NY - 10001, USA"
    }
  ]
}
```

### Get Address by ID 🔒
```http
GET /api/addresses/{id}
```

### Get Default Address 🔒
```http
GET /api/addresses/default
```

### Create Address 🔒
```http
POST /api/addresses
```

**Request Body:**
```json
{
  "fullName": "John Doe",
  "phoneNumber": "+1234567890",
  "addressLine1": "123 Main Street",
  "addressLine2": "Apt 4B",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "landmark": "Near Central Park",
  "addressType": "HOME",
  "isDefault": true
}
```

**Address Types:** `HOME`, `WORK`, `OTHER`

### Update Address 🔒
```http
PUT /api/addresses/{id}
```

### Delete Address 🔒
```http
DELETE /api/addresses/{id}
```

### Set as Default 🔒
```http
PATCH /api/addresses/{id}/set-default
```

---

## ❌ Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "must be a valid email address",
    "password": "must be at least 8 characters"
  }
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied. Admin role required."
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Product not found"
}
```

---

## 📝 Notes

- 🔒 = Requires authentication
- (Admin) = Requires ADMIN role
- All dates are in ISO 8601 format
- Prices are in BigDecimal with 2 decimal places
- Cart expires after 30 days of inactivity
- Maximum 50 unique items in cart
- Maximum 10 quantity per item
- Maximum 10 addresses per user