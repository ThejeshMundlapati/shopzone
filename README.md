# 🛒 ShopZone - E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A full-featured e-commerce platform built with Spring Boot, demonstrating industry-standard practices for building scalable web applications.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Development Progress](#-development-progress)

## ✨ Features

### Phase 1: Foundation (Current)

#### Week 1: Authentication ✅
- [x] User registration with email verification
- [x] JWT-based authentication
- [x] Token refresh mechanism
- [x] Password reset functionality
- [x] Role-based access control (CUSTOMER, ADMIN)

#### Week 2: Product Catalog ✅
- [x] Product CRUD with MongoDB
- [x] Hierarchical categories (parent-child)
- [x] Image upload via Cloudinary
- [x] Search & filtering (name, price, brand, tags)
- [x] Pagination & sorting

#### Week 3: Cart & Wishlist ✅
- [x] Shopping cart with Redis storage
- [x] Stock validation & quantity limits
- [x] Wishlist with move-to-cart
- [x] Address management (CRUD)
- [x] Default address selection

#### Week 4: Orders & Checkout ✅ 🆕
- [x] Complete checkout flow with validation
- [x] Order placement with stock management
- [x] Human-readable order numbers (ORD-YYYYMMDD-XXXX)
- [x] Order lifecycle management
- [x] User order history & tracking
- [x] Order cancellation with stock restoration
- [x] Admin order management & statistics

### Upcoming
- [ ] Week 5: Payment Integration (Stripe)
- [ ] Phase 2: Reviews & Ratings

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2 |
| **Security** | Spring Security, JWT |
| **Databases** | PostgreSQL 15 (Users, Orders), MongoDB 7 (Products), Redis 7 (Cart) |
| **Image Storage** | Cloudinary |
| **Documentation** | Swagger/OpenAPI 3.0 |
| **Containerization** | Docker, Docker Compose |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (Browser/App)                   │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                 │
├────────────────────────────────────────────────────────────┤
│  Controllers → Services → Repositories                     │
├────────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │PostgreSQL│  │ MongoDB  │  │  Redis   │  │Cloudinary│    │
│  │  Users   │  │ Products │  │  Cart    │  │  Images  │    │
│  │  Orders  │  │Categories│  │ Wishlist │  │          │    │
│  │ Addresses│  │          │  │          │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└────────────────────────────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Quick Start

```bash
# Clone repository
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone

# Start databases
cd docker
docker-compose up -d

# Run application
./mvnw spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📚 API Documentation

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/me` | Get current user |

### Products & Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/categories` | List all categories |
| POST | `/api/products` | Create product (Admin) |

### Cart & Wishlist
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | Get cart |
| POST | `/api/cart/add` | Add item to cart |
| PUT | `/api/cart/update` | Update quantity |
| DELETE | `/api/cart/remove/{productId}` | Remove item |
| GET | `/api/wishlist` | Get wishlist |
| POST | `/api/wishlist/move-to-cart/{productId}` | Move to cart |

### Checkout & Orders 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/checkout/validate` | Validate cart for checkout |
| GET | `/api/checkout/preview` | Get order preview with totals |
| POST | `/api/checkout/place-order` | Place order |
| GET | `/api/orders` | Get my orders |
| GET | `/api/orders/{orderNumber}` | Get order details |
| GET | `/api/orders/{orderNumber}/track` | Track order |
| POST | `/api/orders/{orderNumber}/cancel` | Cancel order |

### Admin Orders 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/orders` | Get all orders (filtered) |
| GET | `/api/admin/orders/{orderNumber}` | Get any order details |
| PATCH | `/api/admin/orders/{orderNumber}/status` | Update order status |
| GET | `/api/admin/orders/stats` | Get order statistics |
| GET | `/api/admin/orders/search` | Search orders |

## 📁 Project Structure

```
shopzone/
├── src/main/java/com/shopzone/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── OrderConfig.java      🆕
│   │   └── OpenApiConfig.java
│   ├── controller/          # REST controllers
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   ├── CheckoutController.java   🆕
│   │   ├── OrderController.java      🆕
│   │   └── AdminOrderController.java 🆕
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── model/               # Entity classes
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java           🆕
│   │   ├── OrderItem.java       🆕
│   │   └── enums/
│   │       ├── OrderStatus.java     🆕
│   │       └── PaymentStatus.java   🆕
│   ├── repository/          # Data access layer
│   │   ├── jpa/
│   │   │   ├── UserRepository.java
│   │   │   ├── AddressRepository.java
│   │   │   └── OrderRepository.java 🆕
│   │   └── mongo/
│   ├── service/             # Business logic
│   │   ├── CheckoutService.java     🆕
│   │   ├── OrderService.java        🆕
│   │   └── OrderNumberGenerator.java 🆕
│   └── exception/           # Custom exceptions
├── src/main/resources/
│   └── application.yml
├── docker/
│   └── docker-compose.yml
└── docs/
    ├── API.md
    ├── ARCHITECTURE.md
    ├── CHANGELOG.md
    └── SETUP.md
```


## 👨‍💻 Author

**Thejesh**
- GitHub: [@ThejeshMundlapati](https://github.com/ThejeshMundlapati)
- LinkedIn: [Thejesh Mundlapati](https://www.linkedin.com/in/thejesh-mundlapati-9245642b6/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.