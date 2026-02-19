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

#### Week 5: Payment Integration (Stripe) ✅
- [x] Stripe Payment Intent API integration
- [x] Secure client-side payment confirmation
- [x] Webhook handling for payment events
- [x] Full and partial refund support
- [x] Payment history and receipts
- [x] Admin payment management dashboard
- [x] 30-day configurable refund window
- [x] Stock management on payment/refund


#### Week 6: Reviews & Search ✅
- [x] Product reviews with ratings (1-5 stars)
- [x] Verified purchase badges
- [x] Review statistics & distribution
- [x] Elasticsearch full-text search
- [x] Multi-field search (name, description, brand, tags)
- [x] Advanced filters (price, category, brand, rating)
- [x] Autocomplete suggestions
- [x] Similar products recommendations
- [x] MongoDB to Elasticsearch sync

#### Week 7: Email Notifications & Admin Dashboard ✅ 🆕
- [x] Email notifications for order lifecycle events
- [x] Welcome email on user registration
- [x] Thymeleaf HTML email templates with responsive design
- [x] Async email sending with logging
- [x] Mailtrap integration for email testing
- [x] Admin dashboard with comprehensive statistics
- [x] Sales reports (daily, weekly, monthly)
- [x] Revenue analytics with period comparisons
- [x] User management (view, enable/disable, role updates)
- [x] Top products and category sales breakdown

### Upcoming
- [ ] Week 8: Coupons & Promotions

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2 |
| **Security** | Spring Security, JWT |
| **Databases** | PostgreSQL 15 (Users, Orders, Payments), MongoDB 7 (Products), Redis 7 (Cart) |
| **Payments** | Stripe API (Test Mode) |
| **Search** | Elasticsearch 8.11 |
| **Email** | Spring Mail, Thymeleaf, Mailtrap 🆕 |
| **Image Storage** | Cloudinary |
| **Documentation** | Swagger/OpenAPI 3.0 |
| **Containerization** | Docker, Docker Compose |


## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                            │
│                    (Web Browser / Mobile App)                   │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                    │
├─────────────────────────────────────────────────────────────────┤
│  Auth │ Products │ Cart │ Orders │ Payments │ Reviews │ Search  │
│       │ Email 🆕 │ Dashboard 🆕 │ Reports 🆕 │ User Mgmt 🆕    │
└───┬───────┬─────────┬───────┬────────┬─────────┬────────┬───────┘
    │       │         │       │        │         │        │
    ▼       ▼         ▼       ▼        ▼         ▼        ▼
┌───────┐┌───────┐┌───────┐┌───────┐┌───────┐┌───────┐┌──────────┐
│Postgre││MongoDB││ Redis ││Postgre││ Stripe││Postgre││Elastic   │
│  SQL  ││       ││       ││  SQL  ││  API  ││  SQL  ││  Search  │
└───────┘└───────┘└───────┘└───────┘└───────┘└───────┘└──────────┘
 Users    Products  Cart    Orders   Payments Reviews   Search
 EmailLogs Categories Wishlist                          Index
    │
    ▼
┌───────┐
│SMTP 🆕│  Mailtrap (Dev) / Gmail, SendGrid (Prod)
└───────┘
```


# 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- Stripe Account (for payments)
- Cloudinary Account (for images)
- Mailtrap Account (for email testing) 🆕

### 1. Clone Repository
```bash
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone
```

### 2. Start Databases
```bash
cd docker
docker-compose up -d
```

### 3. Verify Services
```bash
docker ps
# Should see: postgres, mongodb, redis, elasticsearch
```

### 4. Check Elasticsearch
```bash
curl http://localhost:9200/_cluster/health
```

### 5. Configure Environment
```bash
# Set environment variables or create application-local.yml
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLISHABLE_KEY=pk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
export CLOUDINARY_CLOUD_NAME=...
export CLOUDINARY_API_KEY=...
export CLOUDINARY_API_SECRET=...
export MAILTRAP_USERNAME=...            # 🆕
export MAILTRAP_PASSWORD=...            # 🆕
```

### 6. Run Application
```bash
./mvnw spring-boot:run
```

### 7. Access Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 8. Initial Sync (Admin)
After creating products, trigger Elasticsearch sync:
```bash
POST /api/search/admin/sync
Authorization: Bearer {admin_token}
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

### Search
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/search` | Search products |
| GET | `/api/search/autocomplete` | Get suggestions |
| GET | `/api/search/similar/{id}` | Similar products |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reviews/product/{id}` | Get reviews |
| GET | `/api/reviews/product/{id}/stats` | Review statistics |
| POST | `/api/reviews` | Create review |
| PUT | `/api/reviews/{id}` | Update review |
| DELETE | `/api/reviews/{id}` | Delete review |

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

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create-intent` | Create payment intent |
| GET | `/api/payments/{orderNumber}` | Get payment status |
| GET | `/api/payments/history` | Get payment history |
| GET | `/api/payments/{orderNumber}/refund-eligibility` | Check refund eligibility |

### Webhooks
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

### Admin Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/payments` | Get all payments |
| GET | `/api/admin/payments/{orderNumber}` | Get payment details |
| POST | `/api/admin/payments/refund` | Process refund |
| GET | `/api/admin/payments/{orderNumber}/refund-eligibility` | Check refund eligibility |
| GET | `/api/admin/payments/stats` | Get payment statistics |

### Admin Dashboard 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard/stats` | Comprehensive statistics |
| GET | `/api/admin/dashboard/recent-orders` | Recent orders summary |
| GET | `/api/admin/dashboard/top-products` | Top selling products |

### Admin Reports 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/reports/sales` | Sales report (daily/weekly/monthly) |
| GET | `/api/admin/reports/users` | User statistics & top customers |
| GET | `/api/admin/reports/revenue` | Revenue analytics & trends |

### Admin User Management 🆕
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| PATCH | `/api/admin/users/{id}/status` | Enable/disable user |
| PATCH | `/api/admin/users/{id}/role` | Update user role |

## 📁 Project Structure

```
shopzone/
├── src/main/java/com/shopzone/
│   ├── config/              # Configuration classes (MailConfig 🆕)
│   ├── controller/          # REST controllers (AdminDashboard, AdminReport, AdminUser 🆕)
│   ├── dto/                 # Request/Response DTOs (Dashboard, Report DTOs 🆕)
│   ├── exception/           # Custom exceptions
│   ├── model/
│   │   ├── elasticsearch/   # ES documents
│   │   ├── enums/           # Enums (EmailType, EmailStatus 🆕)
│   │   └── mongo/           # MongoDB documents
│   ├── repository/
│   │   ├── elasticsearch/   # ES repositories
│   │   ├── jpa/             # PostgreSQL repositories (EmailLogRepository 🆕)
│   │   └── mongo/           # MongoDB repositories
│   ├── security/            # JWT filter
│   └── service/             # Business logic (EmailService, DashboardService, ReportService 🆕)
├── src/main/resources/
│   ├── elasticsearch/       # ES index settings
│   ├── templates/
│   │   └── email/           # Thymeleaf email templates 🆕
│   └── application.yml
├── docker/
│   └── docker-compose.yml
└── docs/
    ├── API.md
    ├── ARCHITECTURE.md
    ├── CHANGELOG.md
    └── SETUP.md
```

## 💳 Payment Flow

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
│   │  → 📧 Order confirmation email sent 🆕              │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 📧 Email Notification Flow 🆕

```
┌─────────────────────────────────────────────────────────────┐
│               EMAIL NOTIFICATION TRIGGERS                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   📧 Welcome Email        ← User Registration              │
│   📧 Order Confirmation   ← Payment Success (Webhook)      │
│   📧 Shipping Notice      ← Admin Ships Order              │
│   📧 Delivery Confirm     ← Admin Delivers Order           │
│   📧 Cancellation Notice  ← Order Cancelled                │
│   📧 Password Reset       ← User Requests Reset            │
│                                                             │
│   All emails: Async (@Async) → Thymeleaf → SMTP → Mailtrap  │
│   All emails: Logged to email_logs table                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🧪 Test Cards

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