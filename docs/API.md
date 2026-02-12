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

## 🛍️ Checkout Endpoints

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

## 📋 User Order Endpoints

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
    "items": [...],
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

## 💳 Payment Endpoints 🆕

### Create Payment Intent

Creates a Stripe Payment Intent for an existing order.

```http
POST /api/payments/create-intent

Request:
{
  "orderNumber": "ORD-20260129-0001",
  "savePaymentMethod": false,
  "receiptEmail": "customer@example.com"
}

Response: 200 OK
{
  "success": true,
  "message": "Payment intent created. Use clientSecret with Stripe.js to complete payment.",
  "data": {
    "paymentIntentId": "pi_3MtwBwLkdIwHu7ix28a3tqPa",
    "clientSecret": "pi_3MtwBwLkdIwHu7ix28a3tqPa_secret_YrKJUKribcBjcG8HVhfZluoGH",
    "publishableKey": "pk_test_...",
    "orderNumber": "ORD-20260129-0001",
    "amount": 99.99,
    "currency": "usd",
    "status": "AWAITING_PAYMENT"
  }
}
```

**Error Responses:**
- `400 Bad Request` - Order already paid or invalid
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Order not found

---

### Get Payment Status

```http
GET /api/payments/{orderNumber}

Response: 200 OK
{
  "success": true,
  "message": "Payment status retrieved",
  "data": {
    "id": "pay_abc123",
    "orderId": "ord_xyz789",
    "orderNumber": "ORD-20260129-0001",
    "stripePaymentIntentId": "pi_3MtwBwLkdIwHu7ix28a3tqPa",
    "stripeChargeId": "ch_3MtwBwLkdIwHu7ix0qHw",
    "amount": 99.99,
    "currency": "usd",
    "status": "PAID",
    "paymentMethod": "CARD",
    "cardLastFour": "4242",
    "cardBrand": "visa",
    "amountRefunded": 0.00,
    "refundableAmount": 99.99,
    "receiptUrl": "https://pay.stripe.com/receipts/...",
    "createdAt": "2026-01-29T10:30:00Z",
    "paidAt": "2026-01-29T10:32:00Z"
  }
}
```

---

### Get Payment History

```http
GET /api/payments/history?page=0&size=10&sortBy=createdAt&sortDir=desc

Response: 200 OK
{
  "success": true,
  "message": "Payment history retrieved",
  "data": {
    "content": [
      {
        "id": "pay_abc123",
        "orderNumber": "ORD-20260129-0001",
        "amount": 99.99,
        "currency": "usd",
        "status": "PAID",
        "paymentMethod": "CARD",
        "cardLastFour": "4242",
        "cardBrand": "visa",
        "createdAt": "2026-01-29T10:30:00Z",
        "paidAt": "2026-01-29T10:32:00Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0
  }
}
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | Page number |
| size | integer | 10 | Page size |
| sortBy | string | createdAt | Sort field (createdAt, amount, status, paidAt) |
| sortDir | string | desc | Sort direction (asc, desc) |

---

### Check Refund Eligibility

```http
GET /api/payments/{orderNumber}/refund-eligibility

Response: 200 OK
{
  "success": true,
  "message": "Refund eligibility checked",
  "data": {
    "eligible": true,
    "refundableAmount": 99.99,
    "message": "Refund available. 28 days remaining in refund window."
  }
}
```

---

## 🔗 Webhook Endpoint 🆕

### Stripe Webhook Handler

Receives events from Stripe when payment status changes.

```http
POST /api/webhooks/stripe
```

**Headers Required:**
```
Stripe-Signature: t=1234567890,v1=signature_here
Content-Type: application/json
```

**Important Notes:**
- This endpoint is **PUBLIC** (no authentication required)
- Stripe signature is verified for security
- Raw request body is required for signature verification

**Events Handled:**

| Event | Action |
|-------|--------|
| `payment_intent.succeeded` | Confirm order, reduce stock |
| `payment_intent.payment_failed` | Update status to FAILED |
| `payment_intent.canceled` | Update status to CANCELLED |
| `charge.refunded` | Track refund (audit) |
| `charge.dispute.created` | Log dispute alert |

**Response:**
```
200 OK - "Webhook processed"
```

---

## 👑 Admin Order Endpoints

### Get All Orders
```http
GET /api/admin/orders?page=0&size=20&status=PENDING&paymentStatus=PAID

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### Update Order Status
```http
PATCH /api/admin/orders/{orderNumber}/status

Request (Ship):
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
  "data": {...}
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
    "ordersByStatus": {...},
    "totalRevenue": 125000.00,
    "averageOrderValue": 950.00
  }
}
```

---

## 👑 Admin Payment Endpoints 🆕

### Get All Payments (Admin)

```http
GET /api/admin/payments?status=PAID&page=0&size=20&sortBy=createdAt&sortDir=desc

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "pay_abc123",
        "orderNumber": "ORD-20260129-0001",
        "userId": "user-uuid",
        "amount": 99.99,
        "currency": "usd",
        "status": "PAID",
        "paymentMethod": "CARD",
        "cardLastFour": "4242",
        "cardBrand": "visa",
        "amountRefunded": 0.00,
        "createdAt": "2026-01-29T10:30:00Z",
        "paidAt": "2026-01-29T10:32:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

### Process Refund (Admin)

Process a full or partial refund.

```http
POST /api/admin/payments/refund

Request (Full Refund):
{
  "orderNumber": "ORD-20260129-0001",
  "reason": "Customer requested cancellation",
  "restoreStock": true
}

Request (Partial Refund):
{
  "orderNumber": "ORD-20260129-0001",
  "amount": 25.00,
  "reason": "Item damaged in shipping",
  "restoreStock": false
}

Response: 200 OK
{
  "success": true,
  "message": "Full refund processed successfully",
  "data": {
    "refundId": "re_3MtwBwLkdIwHu7ix0qHw",
    "orderNumber": "ORD-20260129-0001",
    "amountRefunded": 99.99,
    "totalRefunded": 99.99,
    "remainingRefundable": 0.00,
    "status": "succeeded",
    "currency": "usd",
    "reason": "Customer requested cancellation",
    "stockRestored": true,
    "orderStatus": "CANCELLED",
    "paymentStatus": "REFUNDED",
    "refundedAt": "2026-01-29T14:00:00Z"
  }
}
```

**Error Responses:**
- `400 Bad Request` - Cannot refund (not paid, window expired, amount exceeds refundable)
- `404 Not Found` - Order/payment not found

---

### Get Payment Statistics (Admin)

```http
GET /api/admin/payments/stats

Response: 200 OK
{
  "success": true,
  "data": {
    "totalPayments": 150,
    "successfulPayments": 140,
    "failedPayments": 10,
    "totalRevenue": 125000.00,
    "totalRefunded": 5000.00
  }
}
```

---

## 📊 Order & Payment Status Flow 🆕

### Order Status Flow
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
    ↓         ↓           ↓           ↓
CANCELLED CANCELLED   CANCELLED   RETURNED → REFUNDED
```

### Payment Status Flow
```
PENDING → AWAITING_PAYMENT → PAID → PARTIALLY_REFUNDED → REFUNDED
              ↓                ↓
           FAILED          CANCELLED
```

### Valid Status Transitions

| From | Can Transition To |
|------|-------------------|
| PENDING | AWAITING_PAYMENT, CANCELLED |
| AWAITING_PAYMENT | PAID, FAILED, CANCELLED |
| PAID | PARTIALLY_REFUNDED, REFUNDED |
| PARTIALLY_REFUNDED | REFUNDED |
| FAILED | (terminal) |
| CANCELLED | (terminal) |
| REFUNDED | (terminal) |

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
  "message": "Order is already paid"
}
```

---

## 🧪 Test Cards 🆕

| Card Number | Scenario |
|-------------|----------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0000 0000 0002` | Card declined |
| `4000 0025 0000 3155` | Requires 3D Secure authentication |
| `4000 0000 0000 9995` | Insufficient funds |
| `4000 0000 0000 0069` | Expired card |

Use any future date for expiry and any 3-digit CVC.