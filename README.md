# Auth Microservice

A production-grade authentication and authorization microservice built with Spring Boot, designed for **secure, scalable, and observable systems**.

This service goes beyond basic JWT authentication by incorporating **refresh token rotation, audit logging, request context propagation, and security-focused design patterns** used in real-world backend systems.

---

## 🧠 Architecture Highlights

### 1. Stateless Authentication with JWT

- Access tokens used for API authorization
- No server-side session storage
- Improves scalability in distributed systems

### 2. Refresh Token Rotation

- Refresh tokens stored securely in DB (hashed)
- Each refresh invalidates the previous token
- Prevents replay attacks and token reuse

### 3. Custom Security Layer

- Implemented custom `JwtAuthenticationToken`
- Integrated with Spring Security context
- Eliminated redundant DB calls in request flow

### 4. Request Context Propagation

- Built `PulseContext` to carry:
  - IP address
  - User agent
  - Trace ID
  - Request ID
- Attached to SecurityContext for cross-layer access

### 5. Audit Logging System

- Tracks critical security events:
  - Login success/failure
  - Token refresh
  - Logout
- Structured logs with metadata (JSON)
- Designed for fraud detection and observability

### 6. Security-First Design Decisions

- Token type separation (ACCESS vs REFRESH)
- Password hashing using secure algorithms
- Generic error messages to prevent user enumeration
- Prepared for rate limiting and account lock mechanisms

---

## 🚀 Features

- Secure user registration and login
- JWT-based stateless authentication
- Refresh token rotation with reuse detection
- Role-based authorization
- Custom authentication token implementation
- Request context propagation across layers
- Structured audit logging for security events
- Swagger API documentation
- Flyway-based schema migration
- Dockerized deployment

---

## 🛠️ Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- JWT (JJWT)
- Lombok
- SpringDoc OpenAPI
- Docker Compose

---

## ⚙️ Prerequisites

- Java 21 or higher
- Apache Maven 3.6+
- PostgreSQL database (or Docker)
- Git

---

## 📦 Installation and Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd auth
```

### 2. Environment Configuration

Create a `.env` file:

```env
DB_NAME=auth_db
DB_USER=auth_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key_here
```

⚠️ Use a strong, randomly generated JWT secret in production.

---

### 3. Database Setup

#### Option A: Docker (Recommended)

```bash
docker-compose up -d
```

#### Option B: Local Setup

- Install PostgreSQL
- Create database manually

---

### 4. Build and Run

```bash
mvn clean compile
mvn spring-boot:run
```

Service runs at:

```
http://localhost:8080/auth/v1
```

---

## 📡 API Endpoints

Base path: `/auth/v1/users`

### Public Endpoints

- `POST /public/register` → Register user
- `POST /public/login` → Login and get tokens

### Protected Endpoints

- `POST /refresh-token` → Refresh access token
- `POST /logout` → Logout user
- `GET /profile` → Fetch user profile
- `PATCH /roles` → Update role (admin/internal)

---

## 📘 API Documentation

Swagger UI:

```
http://localhost:8080/auth/v1/swagger-ui.html
```

---

## 🗄️ Database Schema

Key tables:

- `users` → user account data
- `refresh_token` → refresh token storage
- `audit_log` → security event logs

---

## 🧪 Testing

```bash
mvn test
```

---

## ⚙️ Configuration

Defined in `application.yml`:

- Database connection
- JWT expiration (default: 3600s)
- Server config (`/auth/v1`)
- Flyway migrations

---

## 🔐 Security Considerations

- Passwords hashed using secure algorithms
- JWT tokens signed and validated
- Access and refresh tokens separated
- Refresh token rotation prevents replay attacks
- Audit logs enable tracking suspicious activity
- Designed for rate limiting and brute-force protection
- No sensitive data exposed in logs

---

## ⚖️ Design Decisions & Trade-offs

- **Why JWT?**
  Stateless authentication reduces server-side overhead

- **Why Refresh Tokens?**
  Enables short-lived access tokens without frequent logins

- **Why Token Rotation?**
  Prevents reuse of compromised tokens

- **Why Audit Logging?**
  Essential for monitoring, debugging, and fraud detection

- **Why Custom Authentication Token?**
  Enables attaching request context and avoiding repeated parsing

---

## 🐳 Docker Deployment

```bash
mvn spring-boot:build-image
docker-compose up
```

---

## 🤝 Contributing

1. Fork the repo
2. Create feature branch
3. Make changes
4. Add tests
5. Submit PR

---

## 📄 License

MIT License

---

## 📬 Support

For issues or questions, open a GitHub issue.
