# ShopZone API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Get Token
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

---

## 🔐 Authentication Endpoints

### Register User
```http
POST /api/auth/register

Request:
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "Password123!",
  "phone": "1234567890"
}

Response: 201 Created
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }
}
```

### Login
```http
POST /api/auth/login

Request:
{
  "email": "john@example.com",
  "password": "Password123!"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

## 📦 Product Endpoints

### List Products
```http
GET /api/products?page=0&size=12&sortBy=createdAt&sortDir=desc

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 9,
    "number": 0
  }
}
```

### Search Products
```http
GET /api/products/search?q=iphone&minPrice=500&maxPrice=1500&brand=Apple
```

### Get Product
```http
GET /api/products/{id}
```

---

## 🛒 Cart Endpoints

### Get Cart
```http
GET /api/cart

Response: 200 OK
{
  "success": true,
  "data": {
    "userId": "uuid",
    "items": [
      {
        "productId": "product-id",
        "productName": "iPhone 15",
        "quantity": 2,
        "unitPrice": 999.99,
        "effectivePrice": 949.99,
        "totalPrice": 1899.98
      }
    ],
    "totalItems": 2,
    "subtotal": 1899.98
  }
}
```

### Add to Cart
```http
POST /api/cart/add

Request:
{
  "productId": "product-id",
  "quantity": 2
}
```

### Update Cart Item
```http
PUT /api/cart/update

Request:
{
  "productId": "product-id",
  "quantity": 3
}
```

### Remove from Cart
```http
DELETE /api/cart/remove/{productId}
```

### Clear Cart
```http
DELETE /api/cart/clear
```

---

## 📍 Address Endpoints

### List Addresses
```http
GET /api/addresses
```

### Create Address
```http
POST /api/addresses

Request:
{
  "fullName": "John Doe",
  "phone": "1234567890",
  "addressLine1": "123 Main Street",
  "addressLine2": "Apt 4B",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "isDefault": true
}
```

### Set Default Address
```http
PATCH /api/addresses/{id}/set-default
```

---

## 🛍️ Checkout Endpoints 🆕

### Validate Cart
```http
GET /api/checkout/validate

Response: 200 OK
{
  "success": true,
  "data": {
    "valid": true,
    "hasWarnings": false,
    "errors": [],
    "warnings": [],
    "cart": {...},
    "message": "Cart is ready for checkout"
  }
}
```

### Checkout Preview
```http
GET /api/checkout/preview?addressId={addressId}

Response: 200 OK
{
  "success": true,
  "data": {
    "cart": {...},
    "shippingAddress": {...},
    "subtotal": 1899.98,
    "itemSavings": 100.00,
    "taxRate": 0.08,
    "taxAmount": 151.99,
    "shippingCost": 0.00,
    "freeShipping": true,
    "totalAmount": 2051.97,
    "totalItems": 2
  }
}
```

### Place Order
```http
POST /api/checkout/place-order

Request:
{
  "shippingAddressId": "address-uuid",
  "customerNotes": "Please leave at door"
}

Response: 201 Created
{
  "success": true,
  "message": "Order placed successfully! Order number: ORD-20260121-XXXX",
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD-20260121-XXXX",
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "items": [...],
    "subtotal": 1899.98,
    "taxAmount": 151.99,
    "shippingCost": 0.00,
    "totalAmount": 2051.97,
    "canCancel": true,
    "createdAt": "2026-01-21T10:30:00"
  }
}
```

---

## 📋 User Order Endpoints 🆕

### Get My Orders
```http
GET /api/orders?page=0&size=10&status=PENDING

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "orderNumber": "ORD-20260121-XXXX",
        "status": "PENDING",
        "statusDisplayName": "Pending",
        "totalAmount": 2051.97,
        "totalItems": 2,
        "createdAt": "2026-01-21T10:30:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}
```

### Get Order Details
```http
GET /api/orders/{orderNumber}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": "uuid",
    "orderNumber": "ORD-20260121-XXXX",
    "status": "SHIPPED",
    "statusDisplayName": "Shipped",
    "statusDescription": "Your order is on its way",
    "paymentStatus": "PAID",
    "shippingAddress": {
      "fullName": "John Doe",
      "addressLine1": "123 Main Street",
      "city": "New York",
      "formattedAddress": "123 Main Street, Apt 4B, New York, NY - 10001, USA"
    },
    "trackingNumber": "1Z999AA10123456784",
    "shippingCarrier": "UPS",
    "items": [
      {
        "productId": "product-id",
        "productName": "iPhone 15",
        "quantity": 2,
        "unitPrice": 999.99,
        "effectivePrice": 949.99,
        "totalPrice": 1899.98,
        "hasDiscount": true,
        "discountPercentage": 5
      }
    ],
    "subtotal": 1899.98,
    "taxAmount": 151.99,
    "shippingCost": 0.00,
    "totalAmount": 2051.97,
    "canCancel": false,
    "createdAt": "2026-01-21T10:30:00",
    "shippedAt": "2026-01-22T14:00:00"
  }
}
```

### Track Order
```http
GET /api/orders/{orderNumber}/track

Response: 200 OK
{
  "success": true,
  "data": {
    "orderNumber": "ORD-20260121-XXXX",
    "status": "SHIPPED",
    "trackingNumber": "1Z999AA10123456784",
    "shippingCarrier": "UPS",
    "estimatedDelivery": null,
    "shippedAt": "2026-01-22T14:00:00"
  }
}
```

### Get Order Count
```http
GET /api/orders/count

Response: 200 OK
{
  "success": true,
  "data": 5
}
```

### Cancel Order
```http
POST /api/orders/{orderNumber}/cancel

Request:
{
  "reason": "Changed my mind"
}

Response: 200 OK
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "orderNumber": "ORD-20260121-XXXX",
    "status": "CANCELLED",
    "cancellationReason": "Changed my mind",
    "cancelledBy": "USER",
    "cancelledAt": "2026-01-21T11:00:00"
  }
}
```

---

## 👑 Admin Order Endpoints 🆕

> **Note:** All admin endpoints require `ADMIN` role

### Get All Orders
```http
GET /api/admin/orders?page=0&size=20&status=PENDING&paymentStatus=PAID

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "orderNumber": "ORD-20260121-XXXX",
        "userEmail": "john@example.com",
        "userFullName": "John Doe",
        "status": "PENDING",
        "paymentStatus": "PAID",
        "totalAmount": 2051.97,
        "totalItems": 2,
        "createdAt": "2026-01-21T10:30:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### Get Order Details (Admin)
```http
GET /api/admin/orders/{orderNumber}
```

### Update Order Status
```http
PATCH /api/admin/orders/{orderNumber}/status

Request (Confirm):
{
  "status": "CONFIRMED",
  "adminNotes": "Payment verified"
}

Request (Ship - requires tracking):
{
  "status": "SHIPPED",
  "trackingNumber": "1Z999AA10123456784",
  "shippingCarrier": "UPS",
  "adminNotes": "Shipped via UPS Ground"
}

Response: 200 OK
{
  "success": true,
  "message": "Order status updated to SHIPPED",
  "data": {
    "orderNumber": "ORD-20260121-XXXX",
    "status": "SHIPPED",
    "previousStatus": "PROCESSING",
    "trackingNumber": "1Z999AA10123456784",
    "shippingCarrier": "UPS"
  }
}
```

### Get Order Statistics
```http
GET /api/admin/orders/stats

Response: 200 OK
{
  "success": true,
  "data": {
    "totalOrders": 150,
    "ordersToday": 12,
    "ordersThisWeek": 45,
    "ordersThisMonth": 150,
    "ordersByStatus": {
      "PENDING": 10,
      "CONFIRMED": 15,
      "PROCESSING": 8,
      "SHIPPED": 25,
      "DELIVERED": 80,
      "CANCELLED": 12
    },
    "totalRevenue": 125000.00,
    "averageOrderValue": 950.00
  }
}
```

### Search Orders
```http
GET /api/admin/orders/search?q=john

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 5
  }
}
```

---

## 📊 Order Status Flow

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
    ↓         ↓           ↓           ↓
CANCELLED CANCELLED   CANCELLED   RETURNED → REFUNDED
```

### Valid Status Transitions

| From | Can Transition To |
|------|-------------------|
| PENDING | CONFIRMED, CANCELLED |
| CONFIRMED | PROCESSING, CANCELLED |
| PROCESSING | SHIPPED, CANCELLED |
| SHIPPED | DELIVERED, RETURNED |
| DELIVERED | RETURNED |
| RETURNED | REFUNDED |
| CANCELLED | - (terminal) |
| REFUNDED | - (terminal) |

---

## ❌ Error Responses

### Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "must be a valid email"
  }
}
```

### Not Found (404)
```json
{
  "success": false,
  "message": "Order not found: ORD-20260121-XXXX"
}
```

### Unauthorized (401)
```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

### Forbidden (403)
```json
{
  "success": false,
  "message": "You don't have permission to access this order"
}
```

### Bad Request (400)
```json
{
  "success": false,
  "message": "Order cannot be cancelled. Current status: Shipped"
}
```