Understood — below is the **full `README.md`** with everything you asked for preserved, and **detailed Docker +
Kubernetes steps included**, **but with no Dockerfile or Kubernetes manifest contents pasted inline**. Instead the
README references the filenames (`Dockerfile`, `docker-compose.yml`, `k8s/*.yml`, `.env.example`) and gives exact
commands and sequence to follow.

Copy the text below into your `README.md`.

````markdown
# workplace-tracker-service

A concise README for the **workplace-tracker-service** backend. It describes purpose, quick setup, configuration,
available endpoints, and common operational tasks.

---

## **📝 Last Updated :** **`2025-09-06`**

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
    * [Docker Deployment (overview & steps)](#docker-deployment-overview--steps)
    * [Docker Compose (dev)](#docker-compose-dev)
    * [Build & Push Image (CI / Production)](#build--push-image-ci--production)
    * [Kubernetes Deployment (overview & steps)](#kubernetes-deployment-overview--steps)
* [Troubleshooting](#troubleshooting)
* [Security Notice](#security-notice)
* [Contact & Contributing](#contact--contributing)
* [License](#license)

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
````

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

    * Default API root: `http://localhost:8010`
    * Swagger/OpenAPI (if included): `http://localhost:8010/swagger-ui.html` or `/v3/api-docs`

---

## Environment Variables (example)

Set via `application.yml`/`application.properties` or environment variables.

* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
* `SERVER_PORT` — default `8010`
* `ADMIN_USERNAME`, `ADMIN_PASSWORD`
* `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS`
* `AES_SECRET_KEY`, `AES_ALGORITHM`
* `UI_HOST`, `UI_PORT`

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

This project supports both **Docker** (single host / dev) and **Kubernetes** (production) deployments.
All files referenced below are expected to be in the repository root or `k8s/` folder:

* `Dockerfile` — build image (multi-stage recommended)
* `docker-compose.yml` — dev composition (app + Postgres)
* `.env.example` — sample environment file
* `k8s/configmap-workplace-tracker.yml` — non-sensitive config
* `k8s/secret-workplace-tracker.yml` — sensitive values (or create secrets via CLI)
* `k8s/deployment.yml`, `k8s/service.yml`, `k8s/hpa.yml`, `k8s/ingress.yml` — Kubernetes manifests

Below are **detailed steps** and commands — **file contents are intentionally not included here**; check the indicated
filenames for the actual manifests.

---

### Docker Deployment (overview & steps)

#### 1. Prepare `.env`

Copy `.env.example` to `.env` and edit values (DB creds, secrets, ports). **Do not commit** `.env`.

```bash
cp .env.example .env
# edit .env with your editor
```

#### 2. Build Docker image (local)

From repo root (where `Dockerfile` is located):

```bash
docker build -t siddhantpatni0407/workplace-tracker-service:latest .
```

#### 3. Run container (example)

Run the container exposing port 8010 and passing environment variables (you can use the `.env` file):

```bash
docker run -d \
  -p 8010:8010 \
  --name workplace-tracker \
  --env-file .env \
  siddhantpatni0407/workplace-tracker-service:latest
```

#### 4. Verify container is healthy

Check logs and health endpoint:

```bash
docker logs -f workplace-tracker
curl http://localhost:8010/actuator/health
```

---

### Docker Compose (dev)

A `docker-compose.yml` is provided (see repository root). It runs Postgres + app and uses healthchecks and a `.env`
file.

Commands:

```bash
cp .env.example .env
docker-compose up --build
# or detached:
docker-compose up --build -d
```

Verify:

```bash
docker-compose logs -f workplace-tracker
curl http://localhost:8010/actuator/health
```

Notes:

* The compose file uses `depends_on` with healthchecks to wait for Postgres readiness.
* For development you can create `docker-compose.override.yml` to mount source folders and enable hot-reload (Spring
  Devtools). Do not mount secrets into containers in shared environments.

---

### Build & Push Image (CI / Production)

Recommended approach for production: build in CI, tag using commit or semantic version, scan image, push to Docker Hub,
and deploy from registry.

Example commands:

```bash
# build locally
docker build -t siddhantpatni0407/workplace-tracker-service:latest .

# login & push
docker login
docker push siddhantpatni0407/workplace-tracker-service:latest
```

CI tip: in GitHub Actions / GitLab CI, build, run tests, tag image (e.g., `v1.2.3`, or commit SHA), push to registry and
then trigger Kubernetes deployment.

---

### Kubernetes Deployment (overview & steps)

**Pre-reqs:** Kubernetes cluster (Minikube / Kind / cloud), `kubectl` configured for the cluster, optional ingress
controller (nginx) for Ingress.

**Files (place these under `k8s/`):**

* `k8s/configmap-workplace-tracker.yml`
* `k8s/secret-workplace-tracker.yml` (or create secrets via CLI)
* `k8s/deployment.yml`
* `k8s/service.yml`
* `k8s/hpa.yml` (optional)
* `k8s/ingress.yml` (optional)

> The manifests in `k8s/` reference the Docker image `siddhantpatni0407/workplace-tracker-service:latest` by default —
> change tag for production.

#### 1. Apply ConfigMap (non-sensitive)

Make sure ConfigMap is created before Deployment:

```bash
kubectl apply -f k8s/configmap-workplace-tracker.yml
```

#### 2. Apply Secret (sensitive)

Create secret from manifest or via CLI (recommended):

```bash
# Option A: from manifest
kubectl apply -f k8s/secret-workplace-tracker.yml

# Option B: safer - create via CLI (replace values)
kubectl create secret generic workplace-tracker-secret \
  --from-literal=DB_USERNAME=postgres \
  --from-literal=DB_PASSWORD=root \
  --from-literal=APP_JWT_SECRET='replace-with-long-secret'
```

#### 3. Deploy application

```bash
kubectl apply -f k8s/deployment.yml
```

#### 4. Create Service

```bash
kubectl apply -f k8s/service.yml
```

#### 5. (Optional) Create HPA

```bash
kubectl apply -f k8s/hpa.yml
```

#### 6. (Optional) Ingress

If you want domain-based access and have an ingress controller:

```bash
kubectl apply -f k8s/ingress.yml
```

#### Recommended `kubectl` apply order (one-liner sequence)

```bash
kubectl apply -f k8s/configmap-workplace-tracker.yml
kubectl apply -f k8s/secret-workplace-tracker.yml   # or create secret via CLI
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/hpa.yml     # optional
kubectl apply -f k8s/ingress.yml # optional
```

---

### Verification & useful commands

```bash
# check resources
kubectl get configmap,secret,deploy,svc,hpa,ingress -o wide

# watch pods start
kubectl get pods -l app=workplace-tracker -w

# view pod logs
kubectl logs -f deploy/workplace-tracker-deployment -c workplace-tracker

# check rollout
kubectl rollout status deployment/workplace-tracker-deployment

# describe service
kubectl describe svc workplace-tracker-service

# if ingress: check ingress address
kubectl get ingress workplace-tracker-ingress
```

---

## Troubleshooting

* **401/403 responses after token expiry** — token expired. Either re-login or implement refresh token flow.
* **DB connection errors** — verify `SPRING_DATASOURCE_URL`, credentials, and DB server availability.
* **Password encryption errors** — check AES key configuration and `EncryptionKeyService`.
* **Account locked** — unlock in DB or use admin endpoint (if available) to reset `accountLocked` and `loginAttempts`.
* **Pods crashlooping** — check `kubectl logs` for DB/secret issues.
* **Readiness/Liveness probe failing** — ensure `/actuator/health` is accessible.
* **Ingress not working** — confirm ingress controller is installed (e.g., `nginx-ingress`).
* **Docker Compose container not starting** — check `docker-compose logs` and ensure `.env` values are correct.

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
