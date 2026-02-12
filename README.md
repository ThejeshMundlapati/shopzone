# 🛒 ShopZone - E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Stripe](https://img.shields.io/badge/Stripe-Integrated-blueviolet.svg)](https://stripe.com/)
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

### Phase 1: Foundation

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

#### Week 4: Orders & Checkout ✅
- [x] Complete checkout flow with validation
- [x] Order placement with stock management
- [x] Human-readable order numbers (ORD-YYYYMMDD-XXXX)
- [x] Order lifecycle management
- [x] User order history & tracking
- [x] Order cancellation with stock restoration
- [x] Admin order management & statistics

### Phase 2: Payment & Advanced Features

#### Week 5: Payment Integration (Stripe) ✅ 🆕
- [x] Stripe Payment Intent API integration
- [x] Secure client-side payment confirmation
- [x] Webhook handling for payment events
- [x] Full and partial refund support
- [x] Payment history and receipts
- [x] Admin payment management dashboard
- [x] 30-day configurable refund window
- [x] Stock management on payment/refund

### Upcoming
- [ ] Week 6: Reviews & Ratings
- [ ] Phase 3: Microservices Migration

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2 |
| **Security** | Spring Security, JWT |
| **Databases** | PostgreSQL 15 (Users, Orders, Payments), MongoDB 7 (Products), Redis 7 (Cart) |
| **Payments** | Stripe API (Test Mode) 🆕 |
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
│  │PostgreSQL│  │ MongoDB  │  │  Redis   │  │  Stripe  │    │
│  │  Users   │  │ Products │  │  Cart    │  │ Payments │    │
│  │  Orders  │  │Categories│  │ Wishlist │  │          │    │
│  │ Payments │  │          │  │          │  │          │    │
│  │ Addresses│  │          │  │          │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└────────────────────────────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- Stripe Account (free test mode)

### Quick Start

```bash
# Clone repository
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone

# Start databases
cd docker
docker-compose up -d

# Set Stripe environment variables
export STRIPE_SECRET_KEY=sk_test_your_key_here
export STRIPE_PUBLIC_KEY=pk_test_your_key_here
export STRIPE_WEBHOOK_SECRET=whsec_your_secret_here

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

### Checkout & Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/checkout/validate` | Validate cart for checkout |
| GET | `/api/checkout/preview` | Get order preview with totals |
| POST | `/api/checkout/place-order` | Place order |
| GET | `/api/orders` | Get my orders |
| GET | `/api/orders/{orderNumber}` | Get order details |
| GET | `/api/orders/{orderNumber}/track` | Track order |
| POST | `/api/orders/{orderNumber}/cancel` | Cancel order |

### Payments 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create-intent` | Create payment intent |
| GET | `/api/payments/{orderNumber}` | Get payment status |
| GET | `/api/payments/history` | Get payment history |
| GET | `/api/payments/{orderNumber}/refund-eligibility` | Check refund eligibility |

### Webhooks 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhooks/stripe` | Stripe webhook handler (public) |

### Admin Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/orders` | Get all orders (filtered) |
| GET | `/api/admin/orders/{orderNumber}` | Get any order details |
| PATCH | `/api/admin/orders/{orderNumber}/status` | Update order status |
| GET | `/api/admin/orders/stats` | Get order statistics |
| GET | `/api/admin/orders/search` | Search orders |

### Admin Payments 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/payments` | Get all payments |
| GET | `/api/admin/payments/{orderNumber}` | Get payment details |
| POST | `/api/admin/payments/refund` | Process refund |
| GET | `/api/admin/payments/{orderNumber}/refund-eligibility` | Check refund eligibility |
| GET | `/api/admin/payments/stats` | Get payment statistics |

## 📁 Project Structure

```
shopzone/
├── src/main/java/com/shopzone/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── OrderConfig.java
│   │   ├── StripeConfig.java         🆕
│   │   └── OpenApiConfig.java
│   ├── controller/          # REST controllers
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   ├── CheckoutController.java
│   │   ├── OrderController.java
│   │   ├── AdminOrderController.java
│   │   ├── PaymentController.java        🆕
│   │   ├── StripeWebhookController.java  🆕
│   │   └── AdminPaymentController.java   🆕
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── CreatePaymentRequest.java 🆕
│   │   │   └── RefundRequest.java        🆕
│   │   └── response/
│   │       ├── PaymentIntentResponse.java 🆕
│   │       ├── PaymentResponse.java       🆕
│   │       └── RefundResponse.java        🆕
│   ├── model/               # Entity classes
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Payment.java          🆕
│   │   └── enums/
│   │       ├── OrderStatus.java
│   │       ├── PaymentStatus.java (updated) 🆕
│   │       └── PaymentMethod.java 🆕
│   ├── repository/          # Data access layer
│   │   ├── jpa/
│   │   │   ├── UserRepository.java
│   │   │   ├── AddressRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── PaymentRepository.java 🆕
│   │   └── mongo/
│   ├── service/             # Business logic
│   │   ├── CheckoutService.java (updated)
│   │   ├── OrderService.java (updated)
│   │   ├── StripeService.java        🆕
│   │   ├── PaymentService.java       🆕
│   │   ├── RefundService.java        🆕
│   │   └── StripeWebhookService.java 🆕
│   └── exception/           # Custom exceptions
├── src/main/resources/
│   └── application.yml (updated)
├── docker/
│   └── docker-compose.yml
└── docs/
    ├── API.md (updated)
    ├── ARCHITECTURE.md (updated)
    ├── CHANGELOG.md (updated)
    └── SETUP.md (updated)
```

## 💳 Payment Flow 🆕

```
┌─────────────────────────────────────────────────────────────┐
│                    PAYMENT FLOW                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   1. Place Order                                            │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  POST /api/checkout/place-order                     │   │
│   │  → Creates order with status PENDING                │   │
│   │  → Payment status: PENDING                          │   │
│   │  → Stock NOT reduced yet                            │   │
│   └─────────────────────────────────────────────────────┘   │
│                           │                                 │
│                           ▼                                 │
│   2. Create Payment Intent                                  │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  POST /api/payments/create-intent                   │   │
│   │  → Returns clientSecret for Stripe.js               │   │
│   │  → Payment status: AWAITING_PAYMENT                 │   │
│   └─────────────────────────────────────────────────────┘   │
│                           │                                 │
│                           ▼                                 │
│   3. Frontend Payment (Stripe.js)                           │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  stripe.confirmCardPayment(clientSecret, {          │   │
│   │    payment_method: { card: cardElement }            │   │
│   │  })                                                 │   │
│   └─────────────────────────────────────────────────────┘   │
│                           │                                 │
│                           ▼                                 │
│   4. Webhook Notification                                   │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  POST /api/webhooks/stripe                          │   │
│   │  → payment_intent.succeeded received                │   │
│   │  → Order status: CONFIRMED                          │   │
│   │  → Payment status: PAID                             │   │
│   │  → Stock REDUCED now                                │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🧪 Test Cards 🆕

| Card Number | Scenario |
|-------------|----------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0000 0000 0002` | Declined |
| `4000 0025 0000 3155` | Requires 3D Secure |
| `4000 0000 0000 9995` | Insufficient funds |

Use any future date for expiry and any 3-digit CVC.

## 👨‍💻 Author

**Thejesh**
- GitHub: [@ThejeshMundlapati](https://github.com/ThejeshMundlapati)
- LinkedIn: [Thejesh Mundlapati](https://www.linkedin.com/in/thejesh-mundlapati-9245642b6/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.