# ShopZone API Documentation

## Base URL

```
Development: http://localhost:8080
Production:  https://api.shopzone.com
```

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <access_token>
```

---

## Auth Endpoints

### POST /api/auth/register

Register a new user account.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phone": "1234567890"
}
```

**Validation Rules:**
- `firstName`: Required, 2-50 characters
- `lastName`: Required, 2-50 characters
- `email`: Required, valid email format
- `password`: Required, min 8 chars, must contain uppercase, lowercase, digit, special char
- `phone`: Optional, max 15 characters

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "1234567890",
      "role": "CUSTOMER",
      "emailVerified": false,
      "createdAt": "2024-01-15T10:30:00"
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Email already registered",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/auth/login

Authenticate user and get tokens.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "CUSTOMER",
      "emailVerified": true
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/auth/refresh

Get a new access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/auth/forgot-password

Request a password reset email.

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "If the email exists, a password reset link has been sent",
  "timestamp": "2024-01-15T10:30:00"
}
```

> **Note:** Always returns success to prevent email enumeration attacks.

---

### POST /api/auth/reset-password

Reset password using the token from email.

**Request Body:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password reset successful",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### GET /api/auth/verify/{token}

Verify user's email address.

**Path Parameter:**
- `token`: Email verification token

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Email verified successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### GET /api/auth/me

Get current authenticated user's information.

**Headers Required:**
```
Authorization: Bearer <access_token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "role": "CUSTOMER",
    "emailVerified": true,
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/auth/logout

Logout current user (invalidates refresh token).

**Headers Required:**
```
Authorization: Bearer <access_token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Error Responses

### Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Please provide a valid email address",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Unauthorized (401)
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Forbidden (403)
```json
{
  "success": false,
  "message": "Access denied",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Not Found (404)
```json
{
  "success": false,
  "message": "Resource not found",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Internal Server Error (500)
```json
{
  "success": false,
  "message": "An unexpected error occurred. Please try again later.",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Rate Limiting

Currently not implemented. Will be added in Phase 5 with API Gateway.

## Pagination

For list endpoints (coming in Phase 2):

```
GET /api/products?page=0&size=20&sort=createdAt,desc
```

Response includes:
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```