# 🔐 PricePulse — Auth Service

> Production-grade authentication and authorization microservice built with **Java 21 + Spring Boot 3.5**, designed for security, scalability, and observability.

---

## 🧠 What Makes This Different

Most auth services stop at "issue a JWT." This one goes further:

- **Argon2id password hashing** — memory-hard, GPU-resistant (19MB memory cost, 2 iterations)
- **Refresh token rotation with reuse detection** — compromised tokens are invalidated on reuse
- **Redis sliding window rate limiting** — brute-force protection scoped per email (5 attempts / 15 min), with fail-open fallback if Redis is unavailable
- **Custom `PulseContext`** — propagates IP, User-Agent, Trace ID, and Request ID across all layers without repeated parsing
- **Structured audit logging** — every critical security event logged as JSON for fraud detection and observability
- **Generic error messages** — prevents user enumeration attacks

---

## 🏗️ Architecture Highlights

### 1. Stateless JWT Authentication
- Short-lived access tokens (15 min) for API authorization
- No server-side session storage — scales horizontally without sticky sessions

### 2. Refresh Token Rotation
- Refresh tokens stored as hashed values in PostgreSQL
- Each use invalidates the previous token
- Reuse of a revoked token triggers full session invalidation

### 3. Custom Security Layer
- `JwtAuthenticationToken` replaces Spring's default token
- Carries `PulseContext` directly in the `SecurityContext`
- Eliminates redundant DB calls per request

### 4. Request Context Propagation (`PulseContext`)
Carries per-request metadata across all layers:
- IP Address
- User Agent
- Trace ID
- Request ID

### 5. Audit Logging
Structured JSON logs for every critical event:
- Login success / failure
- Token refresh
- Logout

Designed for integration with log aggregators (e.g. ELK, Grafana Loki) and fraud detection pipelines.

### 6. Redis Sliding Window Rate Limiting
- Implemented using Redis `ZSet` — no external rate limiting library
- Each failed attempt stored as a timestamped entry (score = epoch ms)
- Stale entries outside the 15-min window evicted before each check
- Scoped per email — harder to bypass than IP-based limiting
- Fail-open on Redis unavailability — preserves service availability as a deliberate tradeoff

---

## 🚀 Features

| Feature | Status |
|---|---|
| User registration & login | ✅ |
| JWT access + refresh tokens | ✅ |
| Refresh token rotation & reuse detection | ✅ |
| Argon2id password hashing | ✅ |
| Role-based authorization (USER, ADMIN) | ✅ |
| Redis sliding window rate limiting | ✅ |
| Request context propagation (PulseContext) | ✅ |
| Structured audit logging (JSON) | ✅ |
| Flyway schema migrations | ✅ |
| Swagger / OpenAPI docs | ✅ |
| Dockerized deployment | ✅ |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 |
| Password Hashing | Argon2id (Spring Security Crypto) |
| Token | JWT (JJWT) |
| Rate Limiting | Redis 7 + ZSet (custom sliding window, no external library) |
| Persistence | PostgreSQL 17 + Spring Data JPA |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito |
| Containerization | Docker + Docker Compose |

---

## ⚙️ Prerequisites

- Java 21+
- Apache Maven 3.9+
- Docker & Docker Compose (recommended)
- PostgreSQL 17 (if running locally)
- Redis 7 (if running locally)

---

## 📦 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Gsuyash20/PricePulse.git
cd PricePulse/auth-service
```

### 2. Configure Environment

Create a `.env` file in the root:

```env
DB_NAME=auth_db
DB_USER=auth_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_randomly_generated_secret_min_32_chars
REDIS_HOST=localhost
REDIS_PORT=6379
```

> ⚠️ Use a cryptographically random JWT secret in production (min. 256-bit).

### 3. Run with Docker (Recommended)

```bash
docker-compose up -d
```

This starts PostgreSQL, Redis, and the Auth Service together.

### 4. Run Locally

```bash
mvn clean compile
mvn spring-boot:run
```

Service available at:
```
http://localhost:8080/auth/v1
```

---

## 📡 API Endpoints

Base path: `/auth/v1/users`

### Public

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/public/register` | Register a new user |
| `POST` | `/public/login` | Login and receive access + refresh tokens |

### Protected (requires Bearer token)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/refresh-token` | Rotate refresh token |
| `POST` | `/logout` | Invalidate session |
| `GET` | `/profile` | Fetch authenticated user profile |
| `PATCH` | `/roles` | Update user role (ADMIN only) |

### Swagger UI

```
http://localhost:8080/auth/v1/swagger-ui.html
```

---

## 🗄️ Database Schema

| Table | Purpose |
|---|---|
| `users` | User accounts (UUID, email, Argon2id hash, role) |
| `refresh_tokens` | Hashed refresh tokens with expiry |
| `audit_log` | Structured security event log |

Migrations managed by **Flyway** — versioned and reproducible.

---

## 🔐 Security Decisions

| Decision | Rationale |
|---|---|
| **Argon2id over BCrypt** | Memory-hard hashing (19MB cost) kills GPU-based brute-force; winner of the Password Hashing Competition (2015) |
| **Short-lived access tokens (15 min)** | Limits blast radius of a leaked token |
| **Refresh token rotation** | Reuse of a revoked token triggers full session invalidation |
| **Sliding window rate limiting** | No boundary burst vulnerability unlike fixed window; 5 failed attempts per 15-min rolling window per email |
| **Email-scoped rate limiting** | Harder to bypass than IP-based; targets the actual attack vector |
| **Fail-open on Redis unavailability** | Deliberate tradeoff — availability over strict rate limiting when Redis is down |
| **Generic error messages** | `"Invalid credentials"` for both wrong email and wrong password — prevents user enumeration |
| **Token type separation** | ACCESS and REFRESH tokens validated differently — prevents cross-type misuse |
| **PulseContext in SecurityContext** | Request metadata (IP, trace ID, user agent) available in all layers without repeated parsing |

---

## 🧪 Testing

```bash
mvn test
```

Covers:
- Unit tests with **JUnit 5 + Mockito**
- Service layer logic (token generation, rotation, rate limiting, audit events)
- Security filter chain behavior

---

## 🐳 Docker

```bash
# Build image
mvn spring-boot:build-image

# Start all services
docker-compose up
```

---

## 🔗 Related Services

| Service | Description |
|---|---|
| [Catalog Service](../catalog-service) | Price ingestion engine, anomaly detection, Kafka producer |
| [Watch & Notification Service](../watch-service) | Watchlists, price drop alerts, email delivery |

---

## 📄 License

MIT License
