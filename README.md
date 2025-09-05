# workplace-tracker-service

A concise README for the **workplace-tracker-service** backend. It describes purpose, quick setup, configuration,
available endpoints, and common operational tasks.

---

## **📝 Last Updated :** **`2025-09-05`**

## Table of Contents

* [Project Overview](#project-overview)
* [Features](#features)
* [Tech Stack](#tech-stack)
* [Prerequisites](#prerequisites)
* [Quick Start (Local)](#quick-start-local)
* [Environment Variables](#environment-variables)
* [API Overview](#api-overview)
* [Authentication](#authentication)
* [Database & Migrations](#database--migrations)
* [Testing](#testing)
* [Logging & Monitoring](#logging--monitoring)
* [Deployment](#deployment)
* [Troubleshooting](#troubleshooting)
* [License & Contact](#license--contact)

---

## Project Overview

`workplace-tracker-service` is the backend for the Workplace Tracker application. It handles user registration,
authentication, user management, attendance logging, reports and administrative operations. Built as a Spring Boot REST
API.

---

## Features

* User registration and login (JWT-based)
* Account lockout & login attempts tracking
* Password reset endpoint (server-side flow)
* User listing and management (admin)
* Attendance endpoints (log/summary)
* Reports & DB backup endpoints (stubs or real implementations)
* Simple token-based session handling

---

## Tech Stack

* Java 17+ (or compatible)
* Spring Boot (Web, Security, Data JPA)
* Spring Security (JWT)
* JPA / Hibernate
* H2 (for dev) / PostgreSQL / MySQL (production)
* Maven or Gradle build
* Lombok
* Optional: Flyway or Liquibase for DB migrations

---

## Prerequisites

* Java 17+ installed
* Maven (or Gradle) installed
* PostgreSQL/MySQL (if not using embedded H2)
* Optional: Docker (for DB containers)

---

## Quick Start (Local)

1. **Clone repo**

   ```bash
   git clone <repo-url>
   cd workplace-tracker-service
   ```

2. **Configure environment** — see [Environment Variables](#environment-variables).

3. **Run with Maven**

   ```bash
   # build
   mvn clean package -DskipTests

   # run
   mvn spring-boot:run
   ```

   Or run the packaged jar:

   ```bash
   java -jar target/workplace-tracker-service-<version>.jar
   ```

4. **Access**

    * Default API root: `http://localhost:8010` (example based on the UI)
    * Swagger/OpenAPI (if included): `http://localhost:8010/swagger-ui.html` or `/v3/api-docs`

---

## Environment Variables (example)

Set via `application.yml`/`application.properties` or environment variables.

* `SPRING_DATASOURCE_URL` — JDBC URL (e.g. `jdbc:postgresql://localhost:5432/workplace`)
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `SPRING_PROFILES_ACTIVE` — `dev` | `prod`
* `JWT_SECRET` — secret used to sign tokens
* `JWT_EXPIRATION_MS` — token expiry (ms)
* `SERVER_PORT` — default `8010`
* `MAIL_HOST`, `MAIL_USER`, `MAIL_PASS` — if email used for password reset (optional)
* `ENCRYPTION_KEY_SERVICE_*` — if using external key manager

> Keep secrets out of source control. Use `.env`, Kubernetes secrets, or other secret manager in production.

---

## API Overview (important endpoints)

> Base path used in the UI is `/api/v1/workplace-tracker-service` (adjustable in `application.yml`).

* `POST /register` — Register new user
  Request: `{ name, email, mobileNumber, password, role }`
  Response: `AuthResponse` with `token` on success

* `POST /login` — Login
  Request: `{ email, password }`
  Response: `AuthResponse` with `token`, `lastLoginTime`, `userId`, `role`

* `POST /forgot/reset` — Reset password (current flow: accepts `{ email, newPassword }`)
  Response: success/failure message

* `GET /user/fetch` — Admin: fetch all users
  Response:
  `{ status, message, data: [ { userId, username, email, mobileNumber, role, lastLoginTime, loginAttempts, isAccountLocked, isActive } ] }`

* `GET /user/{id}` — Get user by id

* `PATCH/PUT /user/{id}` — Update user (active/lock/role)

* `POST /attendance/log` — Log attendance (if implemented)

* `GET /attendance/summary` — Attendance summary (if implemented)

* `POST /db-backup` — Trigger DB backup (if implemented)

> See controllers for exact URLs and payloads.

---

## Authentication

* JWT tokens are issued on successful login/registration.
* Token must be included in `Authorization: Bearer <token>` header for protected endpoints.
* Token expiry by default is one hour. Consider implementing refresh-token flow for seamless re-authentication (note:
  not enabled by default).
* Account lockout: backend increments `loginAttempts` and locks account after threshold (e.g., 5).

---

## Database & Migrations

* Use JPA entities & repository patterns.
* Local development may use embedded H2 (configure `spring.datasource.url`).
* For production, configure PostgreSQL/MySQL and run migrations.
* If using Flyway/Liquibase: place scripts in `resources/db/migration`.

---

## Testing

* Unit tests: `mvn test`
* Integration tests: configure test DB and run `mvn verify`
* Manual API testing: Postman / curl.

Example curl to login:

```bash
curl -X POST http://localhost:8010/api/v1/workplace-tracker-service/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'
```

---

## Logging & Monitoring

* Logs via SLF4J / Logback. Configure log level in `application.yml`.
* Add metrics/Prometheus/Kibana integration for production monitoring.

---

## Deployment

* Build JAR and deploy to your JVM host:

  ```bash
  mvn clean package -DskipTests
  scp target/*.jar server:/opt/app/
  ssh server "java -Xms256m -Xmx1g -jar /opt/app/workplace-tracker-service.jar &"
  ```
* For Docker: create `Dockerfile`, build image and run container. Use external DB connection strings / secrets.

---

## Troubleshooting

* **401/403 responses after token expiry** — token expired. Either re-login or implement refresh token flow.
* **DB connection errors** — verify `SPRING_DATASOURCE_URL`, credentials, and DB server availability.
* **Password encryption errors** — check AES key configuration and `EncryptionKeyService`.
* **Account locked** — unlock in DB or use admin endpoint (if available) to reset `accountLocked` and `loginAttempts`.

---

## Security Notice

* Current `forgot/reset` endpoint resets password without OTP — this is insecure. Add a verification step (email token)
  before using in production.
* Keep `JWT_SECRET` and encryption keys safe.
* Use HTTPS in production.

---

## Contact & Contributing

* Maintainer: **Siddhant Patni** (or project owner)
* Contributions: open PRs, follow repository contribution guidelines.
* Issues: open GitHub issues for bugs/feature requests.

---

## License

Specify your license in the repository root (e.g., MIT, Apache-2.0).

---
