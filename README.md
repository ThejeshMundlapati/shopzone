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

### Upcoming
- [ ] Week 4: Orders & Checkout
- [ ] Phase 2: Payment Integration
- [ ] Phase 3: Frontend (React)
- [ ] Phase 4: Microservices Migration

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2 |
| **Security** | Spring Security, JWT |
| **Database** | PostgreSQL (Users, Addresses) |
| **NoSQL** | MongoDB (Products, Categories) |
| **Cache** | Redis (Cart, Wishlist) |
| **Storage** | Cloudinary (Images) |
| **Docs** | OpenAPI 3.0 / Swagger UI |
| **Container** | Docker, Docker Compose |

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT                                  │
│                    (Swagger UI / React)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SPRING BOOT APP                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   Auth   │  │ Product  │  │   Cart   │  │ Address  │         │
│  │Controller│  │Controller│  │Controller│  │Controller│         │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘         │
│       │             │             │             │               │
│  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐         │
│  │   Auth   │  │ Product  │  │   Cart   │  │ Address  │         │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │         │ 
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘         │
│       │             │             │             │               │
└───────┼─────────────┼─────────────┼─────────────┼───────────────┘
        │             │             │             │
        ▼             ▼             ▼             ▼
┌──────────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│  PostgreSQL  │ │ MongoDB  │ │  Redis   │ │  PostgreSQL  │
│   (Users)    │ │(Products)│ │  (Cart)  │ │ (Addresses)  │
└──────────────┘ └──────────┘ └──────────┘ └──────────────┘
```

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- IDE (IntelliJ IDEA recommended)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/ThejeshMundlapati/shopzone.git
cd shopzone
```

2. **Start databases**
```bash
cd docker
docker-compose up -d
```

3. **Configure Cloudinary** (for image uploads)
    - Create free account at [cloudinary.com](https://cloudinary.com)
    - Update `application.yml` with your credentials

4. **Run the application**
```bash
./mvnw spring-boot:run
```

5. **Access Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

6. **Access Redis Commander** (optional)
```
http://localhost:8081
```

### Docker Services

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 5432 | Users, Addresses |
| MongoDB | 27017 | Products, Categories |
| Redis | 6379 | Cart, Wishlist |
| Redis Commander | 8081 | Redis GUI |

## 📚 API Documentation

### Authentication APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/forgot-password` | Request password reset |
| GET | `/api/auth/me` | Get current user |

### Product APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product details |
| POST | `/api/products` | Create product (Admin) |
| PUT | `/api/products/{id}` | Update product (Admin) |
| DELETE | `/api/products/{id}` | Delete product (Admin) |

### Cart APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | Get user's cart |
| POST | `/api/cart/add` | Add item to cart |
| PUT | `/api/cart/update` | Update item quantity |
| DELETE | `/api/cart/remove/{productId}` | Remove item |
| DELETE | `/api/cart/clear` | Clear cart |
| GET | `/api/cart/validate` | Validate for checkout |

### Wishlist APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wishlist` | Get wishlist |
| POST | `/api/wishlist/add/{productId}` | Add to wishlist |
| DELETE | `/api/wishlist/remove/{productId}` | Remove item |
| POST | `/api/wishlist/move-to-cart/{productId}` | Move to cart |

### Address APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/addresses` | Get all addresses |
| POST | `/api/addresses` | Create address |
| PUT | `/api/addresses/{id}` | Update address |
| DELETE | `/api/addresses/{id}` | Delete address |
| PATCH | `/api/addresses/{id}/set-default` | Set as default |

## 📁 Project Structure

```
shopzone/
├── docker/
│   └── docker-compose.yml
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── CHANGELOG.md
│   └── SETUP.md
├── src/main/java/com/shopzone/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data Transfer Objects
│   ├── exception/       # Custom exceptions
│   ├── model/           # Entity/Document models
│   ├── repository/      # Data repositories
│   ├── security/        # Security filters
│   └── service/         # Business logic
└── src/main/resources/
    └── application.yml
```

## 📈 Development Progress

| Phase | Week | Feature | Status |
|-------|------|---------|--------|
| 1 | 1 | Authentication | ✅ Complete |
| 1 | 2 | Product Catalog | ✅ Complete |
| 1 | 3 | Cart & Wishlist | ✅ Complete |
| 1 | 4 | Orders | 🔄 Next |
| 2 | 5-7 | Payment & Reviews | ⏳ Planned |
| 3 | 8-11 | Frontend | ⏳ Planned |
| 4 | 12-13 | Docker & CI/CD | ⏳ Planned |
| 5 | 14-18 | Microservices | ⏳ Planned |
| 6 | 19-21 | Kubernetes | ⏳ Planned |

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Thejesh**
- GitHub: [@ThejeshMundlapati](https://github.com/ThejeshMundlapati)
- Project: Personal portfolio project