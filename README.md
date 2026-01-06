# ShopZone - E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive e-commerce platform built with Spring Boot, demonstrating industry-level development practices.

## 🚀 Features

### Phase 1 - Week 1: Authentication ✅
- User registration with validation
- JWT-based authentication (access + refresh tokens)
- Password reset functionality
- Email verification (mock)
- Role-based access control (CUSTOMER, ADMIN)

### Phase 1 - Week 2: Product Catalog ✅
- Product management (CRUD)
- Category hierarchy (parent-child)
- Image upload via Cloudinary
- Search (name, description, brand, tags)
- Filter by price range, brand, category
- Pagination and sorting
- Featured products
- Public read / Admin write access

### Upcoming
- Week 3: Shopping Cart & Wishlist
- Week 4: Checkout & Orders
- Phase 2: Microservices Architecture

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.5, Java 17 |
| Security | Spring Security, JWT |
| Database (Users) | PostgreSQL 16 |
| Database (Products) | MongoDB 7.0 |
| Image Storage | Cloudinary |
| API Docs | Swagger/OpenAPI 3.0 |
| Build | Maven |
| Container | Docker, Docker Compose |

## 📁 Project Structure

```
shopzone/
├── src/main/java/com/shopzone/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── dto/              # Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   ├── exception/        # Custom exceptions
│   ├── model/            # Entity/Document classes
│   ├── repository/       # Data repositories
│   ├── security/         # JWT & Security
│   └── service/          # Business logic
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
├── docker/
│   └── docker-compose.yml
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── SETUP.md
│   └── CHANGELOG.md
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Cloudinary account (free tier)

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

### 3. Configure Cloudinary
Add to environment variables or `application.yml`:
```yaml
cloudinary:
  cloud-name: your-cloud-name
  api-key: your-api-key
  api-secret: your-api-secret
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```

### 5. Access Swagger UI
Open: http://localhost:8080/swagger-ui.html

## 📚 API Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/auth/register | Register user | No |
| POST | /api/auth/login | Login | No |
| POST | /api/auth/refresh | Refresh token | No |
| GET | /api/auth/me | Current user | Yes |
| POST | /api/auth/forgot-password | Request reset | No |
| POST | /api/auth/reset-password | Reset password | No |

### Categories
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/categories | List all | No |
| GET | /api/categories/{id} | Get by ID | No |
| GET | /api/categories/tree | Category tree | No |
| GET | /api/categories/roots | Root categories | No |
| POST | /api/categories | Create | Admin |
| PUT | /api/categories/{id} | Update | Admin |
| DELETE | /api/categories/{id} | Delete | Admin |

### Products
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/products | List all | No |
| GET | /api/products/{id} | Get by ID | No |
| GET | /api/products/slug/{slug} | Get by slug | No |
| GET | /api/products/search | Search | No |
| GET | /api/products/featured | Featured | No |
| GET | /api/products/category/{id} | By category | No |
| GET | /api/products/filter/price | By price range | No |
| GET | /api/products/filter/brand | By brand | No |
| POST | /api/products | Create | Admin |
| PUT | /api/products/{id} | Update | Admin |
| DELETE | /api/products/{id} | Delete | Admin |
| POST | /api/products/{id}/images | Upload images | Admin |

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Test with Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Login with admin: `admin@shopzone.com` / `Admin@123`
3. Copy token and authorize
4. Test endpoints

## 📖 Documentation

- [API Documentation](docs/API.md)
- [Architecture Guide](docs/ARCHITECTURE.md)
- [Setup Guide](docs/SETUP.md)
- [Changelog](docs/CHANGELOG.md)


## 👨‍💻 Author

**Your Name**
- GitHub: [@Thejesh Mundlapati](https://github.com/ThejeshMundlapati/)
- LinkedIn: [Thejesh](https://www.linkedin.com/in/thejesh-mundlapati-9245642b6/)
- Email: mthejesh361@gmail.com

---

⭐ **Star this repository if you find it helpful!**