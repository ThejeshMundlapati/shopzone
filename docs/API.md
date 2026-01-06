# ShopZone API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All protected endpoints require JWT Bearer token:
```
Authorization: Bearer <access_token>
```

---

# Authentication Endpoints

## Register User
```http
POST /auth/register
```

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

**Response (201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

## Login
```http
POST /auth/login
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

## Get Current User
```http
GET /auth/me
Authorization: Bearer <token>
```

---

# Category Endpoints

## List All Categories
```http
GET /categories
```

**Response (200):**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "6951a0011e8dfc98ca885ecf",
      "name": "Electronics",
      "description": "Electronic devices",
      "slug": "electronics",
      "parentId": null,
      "level": 0,
      "active": true,
      "productCount": 15
    }
  ]
}
```

## Get Category Tree
```http
GET /categories/tree
```

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "name": "Electronics",
      "children": [
        {
          "id": "...",
          "name": "Smartphones",
          "children": []
        },
        {
          "id": "...",
          "name": "Laptops",
          "children": []
        }
      ]
    }
  ]
}
```

## Get Category by ID
```http
GET /categories/{id}
```

**Response includes breadcrumbs:**
```json
{
  "data": {
    "id": "...",
    "name": "Smartphones",
    "breadcrumbs": [
      { "id": "...", "name": "Electronics", "slug": "electronics" },
      { "id": "...", "name": "Smartphones", "slug": "smartphones" }
    ]
  }
}
```

## Create Category (Admin)
```http
POST /categories
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Smartphones",
  "description": "Mobile phones and accessories",
  "parentId": "electronics_id_here",
  "active": true,
  "displayOrder": 1
}
```

## Update Category (Admin)
```http
PUT /categories/{id}
Authorization: Bearer <admin_token>
```

## Delete Category (Admin)
```http
DELETE /categories/{id}
Authorization: Bearer <admin_token>
```

---

# Product Endpoints

## List All Products
```http
GET /products?page=0&size=12&sortBy=createdAt&sortDir=desc
```

**Query Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 12 | Items per page |
| sortBy | string | createdAt | Sort field |
| sortDir | string | desc | asc or desc |

**Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "6951b20c1e8dfc98ca885ed7",
        "name": "iPhone 15 Pro",
        "description": "Latest Apple iPhone...",
        "slug": "iphone-15-pro",
        "sku": "APPL-IPH15P-256",
        "price": 999.99,
        "discountPrice": 949.99,
        "discountPercentage": 5,
        "stock": 50,
        "inStock": true,
        "categoryId": "...",
        "categoryName": "Smartphones",
        "brand": "Apple",
        "images": ["https://res.cloudinary.com/..."],
        "tags": ["smartphone", "apple", "iphone"],
        "active": true,
        "featured": true,
        "details": {
          "weight": "187g",
          "color": "Natural Titanium"
        }
      }
    ],
    "page": 0,
    "size": 12,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## Get Product by ID
```http
GET /products/{id}
```

## Get Product by Slug
```http
GET /products/slug/{slug}
```

Example: `GET /products/slug/iphone-15-pro`

## Search Products
```http
GET /products/search?query=apple&page=0&size=12
```

Searches in: name, description, brand, tags

## Get Featured Products
```http
GET /products/featured?page=0&size=12
```

## Get Products by Category
```http
GET /products/category/{categoryId}?page=0&size=12
```

## Filter by Price Range
```http
GET /products/filter/price?minPrice=500&maxPrice=1500&page=0&size=12
```

## Filter by Brand
```http
GET /products/filter/brand?brand=Apple&page=0&size=12
```

## Create Product (Admin)
```http
POST /products
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest Apple iPhone with A17 Pro chip",
  "sku": "APPL-IPH15P-256",
  "price": 999.99,
  "discountPrice": 949.99,
  "stock": 50,
  "categoryId": "smartphones_category_id",
  "brand": "Apple",
  "tags": ["smartphone", "apple", "iphone", "5g"],
  "active": true,
  "featured": true,
  "details": {
    "weight": "187g",
    "dimensions": "146.6 x 70.6 x 8.25 mm",
    "color": "Natural Titanium",
    "material": "Titanium"
  }
}
```

## Update Product (Admin) - Partial Update
```http
PUT /products/{id}
Authorization: Bearer <admin_token>
```

**Request Body (only fields to update):**
```json
{
  "price": 899.99,
  "featured": false
}
```

Note: categoryId is NOT required for partial updates.

## Delete Product (Admin)
```http
DELETE /products/{id}
Authorization: Bearer <admin_token>
```

## Upload Product Images (Admin)
```http
POST /products/{id}/images
Authorization: Bearer <admin_token>
Content-Type: multipart/form-data
```

**Form Data:**
- `files`: Image files (JPEG, PNG, WebP, GIF)

## Remove Product Image (Admin)
```http
DELETE /products/{id}/images?imageUrl=https://res.cloudinary.com/...
Authorization: Bearer <admin_token>
```

---

# Error Responses

## 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "name": "Product name is required",
    "price": "Price must be greater than 0"
  }
}
```

## 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

## 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied. Admin role required."
}
```

## 404 Not Found
```json
{
  "success": false,
  "message": "Product not found with ID: xyz"
}
```

## 409 Conflict
```json
{
  "success": false,
  "message": "Product with SKU 'APPL-IPH15P-256' already exists"
}
```