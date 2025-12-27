# 🛒 ShopZone - E-Commerce Platform

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

A full-featured, production-ready e-commerce platform built with Spring Boot microservices architecture.

## 🎯 Project Overview

ShopZone is a comprehensive e-commerce solution demonstrating modern software development practices including microservices, event-driven architecture, containerization, and CI/CD pipelines.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                           │
│                   (Spring Cloud Gateway)                     │
└─────────────────────────┬───────────────────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────┐         ┌─────────┐          ┌─────────┐
│  User   │         │ Product │          │  Order  │
│ Service │         │ Service │          │ Service │
└────┬────┘         └────┬────┘          └────┬────┘
     │                   │                    │
     ▼                   ▼                    ▼
┌─────────┐         ┌─────────┐          ┌─────────┐
│PostgreSQL│        │ MongoDB │          │PostgreSQL│
└─────────┘         └─────────┘          └─────────┘
```

## 🚀 Tech Stack

| Category | Technology |
|----------|------------|
| Backend | Java 17, Spring Boot 3.5 |
| Database | PostgreSQL, MongoDB, Redis |
| Authentication | JWT, Spring Security |
| Documentation | OpenAPI 3.0 (Swagger) |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Cloud | Railway.app / Render |

## 📋 Features

### Phase 1 - Foundation (Current)
- [x] User registration and authentication
- [x] JWT-based security with refresh tokens
- [x] Email verification (mock)
- [x] Password reset functionality
- [x] API documentation (Swagger UI)
- [x] Global exception handling

### Phase 2 - E-Commerce Core (Upcoming)
- [ ] Product catalog (MongoDB)
- [ ] Category management
- [ ] Shopping cart (Redis)
- [ ] Wishlist functionality
- [ ] Order management
- [ ] Payment integration (Stripe)

### Phase 3 - Advanced Features
- [ ] Search with Elasticsearch
- [ ] Email notifications
- [ ] Admin dashboard
- [ ] Reviews and ratings

## 🛠️ Getting Started

### Prerequisites

- Java 17 or higher
- Docker & Docker Compose
- Maven 3.8+
- Git

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/shopzone.git
cd shopzone
```

2. **Start the database**
```bash
cd docker
docker-compose up -d
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

4. **Access the application**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/refresh` | Refresh access token | No |
| POST | `/api/auth/forgot-password` | Request password reset | No |
| POST | `/api/auth/reset-password` | Reset password with token | No |
| GET | `/api/auth/verify/{token}` | Verify email address | No |
| GET | `/api/auth/me` | Get current user info | Yes |
| POST | `/api/auth/logout` | Logout user | Yes |

### Sample API Calls

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "phone": "1234567890"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Access protected endpoint:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <your-access-token>"
```

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=AuthServiceTest
```

## 📁 Project Structure

```
shopzone/
├── src/
│   ├── main/
│   │   ├── java/com/shopzone/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data transfer objects
│   │   │   │   ├── request/      # Request DTOs
│   │   │   │   └── response/     # Response DTOs
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── model/            # Entity classes
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── security/         # Security components
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/com/shopzone/
│           ├── controller/
│           └── service/
├── docker/
│   └── docker-compose.yml
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── SETUP.md
├── .gitignore
├── pom.xml
└── README.md
```

## 🐳 Docker Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove volumes (reset database)
docker-compose down -v
```

## 🔧 Configuration

Environment variables can be set in `application.yml` or via environment:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://localhost:5432/shopzone` |
| `JWT_SECRET` | JWT signing key | (configured in yml) |
| `JWT_EXPIRATION` | Token expiry (ms) | `86400000` (24h) |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Changelog

### v1.0.0 (Phase 1 Week 1)
- Initial project setup
- User authentication with JWT
- Spring Security configuration
- Swagger API documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@Thejesh Mundlapati](https://github.com/ThejeshMundlapati/)
- LinkedIn: [Thejesh](https://www.linkedin.com/in/thejesh-mundlapati-9245642b6/)
- Email: mthejesh361@gmail.com

---

⭐ **Star this repository if you find it helpful!**