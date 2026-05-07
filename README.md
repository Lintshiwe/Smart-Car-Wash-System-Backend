<p align="center">
  <img src="../Smart-Car-Wash-System-Frontend/app/public/images/logo.png" alt="INT216D Logo" width="120" />
</p>

# INT216D Smart Car Wash System — Backend

Spring Boot microservices backend for the INT216D Car Wash booking platform. Uses **PostgreSQL, Redis, Kafka, and MailHog** via Docker. Email delivery via **Gmail SMTP**.

## Modules

| Module | Port | Responsibility |
|--------|------|----------------|
| `api-gateway` | 8080 | Single entry point; routes `/api/v1/**` to services |
| `auth-service` | 8085 | Registration, OTP, login, JWT, password reset, email (async) |
| `client-service` | 8082 | Profile, vehicles, addresses, mandates |
| `booking-service` | 8083 | Bookings, catalogue, membership plans, payments, notifications, scheduling |
| `common` | — | Shared JWT, security filter, exceptions, DTOs |

## Tech Stack

- Java 21, Spring Boot 3.4, Maven
- PostgreSQL 16 (persistent), H2 (dev fallback)
- Redis 7, Kafka 7.6 (Kraft)
- Spring Security + JWT (access + refresh tokens)
- Flyway migrations, JPA/Hibernate
- Spring Mail (Gmail SMTP) with `@Async`
- Spring Scheduling (auto-renewal, expiry checks, expiry warnings)

## Quick Start

```bash
# 1. Start infrastructure
docker compose up -d postgres redis mailhog kafka

# 2. Build
mvn clean install -DskipTests

# 3. Run services
mvn -pl auth-service    spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl client-service  spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway     spring-boot:run
```

## API Endpoints

### Auth (`/api/v1/auth`)
- `POST /register` — Create account (async email OTP)
- `POST /verify-email` — Verify with OTP code
- `POST /login` — Returns JWT (8h expiry) + HttpOnly refresh cookie
- `POST /refresh` — Refresh access token
- `POST /password-reset/request` — Send reset code via email
- `POST /password-reset/confirm` — Reset password with code

### Catalogue (`/api/v1/catalogue`) — Public
- `GET /services` — All services
- `GET /addons` — Optional extras

### Bookings (`/api/v1/bookings`)
- `POST /bookings` — Create booking (authenticated CLIENT)
- `POST /bookings/guest` — Create booking (public, no auth)
- `GET /me` — My bookings
- `GET /slots` — Available time slots

### Membership (`/api/v1/membership`)
- `GET /plans/active` — Active plans (public)
- `POST /subscribe` — Subscribe to plan with payment
- `POST /renew` — Renew membership
- `POST /upgrade/{planId}` — Upgrade plan
- `GET /` — Current membership details

### Client (`/api/v1/clients/me`)
- `GET /` — Profile
- `POST/PUT /` — Create/update profile
- `GET|POST /addresses` — Saved addresses

### Payments (`/api/v1/payments`)
- `GET /me` — Payment history

### Admin (`/api/v1/admin`)
- `GET /dashboard` — Stats
- `GET /memberships` — All memberships

## Email Features
- **Async email sending** — APIs respond instantly, emails send in background
- OTP verification codes
- Password reset codes
- Booking confirmation (with service details)
- Membership subscription/renewal/upgrade confirmations
- Expiry warning reminders (1, 3, 7 days before)
- All via Gmail SMTP

## Scheduled Tasks
- **8:00 AM** — Send expiry warning emails
- **12:30 AM** — Mark expired memberships
- **3:00 AM** — Auto-renew eligible memberships
