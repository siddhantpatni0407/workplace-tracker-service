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
    * [Minikube / Kubernetes Runbook](#minikube--kubernetes-runbook)
    * [Stopping & Cleanup](#stopping--cleanup)
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

* Java 21
* Spring Boot (Web, Security, Data JPA)
* Spring Security (JWT)
* JPA / Hibernate
* PostgreSQL
* Gradle build
* Lombok
* Liquibase for DB migrations

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
   git clone https://github.com/siddhantpatni0407/workplace-tracker-service.git
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

Set via `application.yml` or environment variables.

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
* `POST /login` — Login
* `POST /forgot/reset` — Reset password
* `GET /user/fetch` — Admin: fetch all users
* `GET /user/{id}` — Get user by id
* `PATCH/PUT /user/{id}` — Update user
* `POST /attendance/log` — Log attendance
* `GET /attendance/summary` — Attendance summary
* `POST /db-backup` — Trigger DB backup

---

## Authentication

* JWT tokens issued on login/registration.
* Use `Authorization: Bearer <token>` header.
* Tokens expire (default 1h).
* Account lockout after multiple failed logins.

---

## Database & Migrations

* JPA repositories + Liquibase for migrations.
* Local: embedded H2 or PostgreSQL.
* Production: PostgreSQL/MySQL recommended.

---

## Testing

```bash
mvn test
mvn verify
```

Example curl login:

```bash
curl -X POST http://localhost:8010/api/v1/workplace-tracker-service/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'
```

---

## Logging & Monitoring

* Logs via SLF4J/Logback.
* `/actuator/health` for liveness/readiness probes.
* Extend with Prometheus/Grafana in production.

---

## Deployment

This project supports both **Docker** (local/dev) and **Kubernetes** (production) deployments.

* `Dockerfile`
* `docker-compose.yml`
* `k8s/configmap-workplace-tracker.yml`
* `k8s/secret-workplace-tracker.yml`
* `k8s/deployment.yml`, `k8s/service.yml`

---

### Docker Deployment (overview & steps)

```bash
cp .env.example .env
docker build -t siddhantpatni0407/workplace-tracker-service:latest .
docker run -d -p 8010:8010 --env-file .env --name workplace-tracker siddhantpatni0407/workplace-tracker-service:latest
docker logs -f workplace-tracker
curl http://localhost:8010/actuator/health
```

---

### Docker Compose (dev)

```bash
cp .env.example .env
docker-compose up --build
# or
docker-compose up --build -d
```

Check:

```bash
docker-compose logs -f workplace-tracker
```

---

### Build & Push Image (CI / Production)

```bash
docker build -t siddhantpatni0407/workplace-tracker-service:latest .
docker login
docker push siddhantpatni0407/workplace-tracker-service:latest
```

---

### Kubernetes Deployment (overview & steps)

```bash
kubectl apply -f k8s/secret-workplace-tracker.yml
kubectl apply -f k8s/configmap-workplace-tracker.yml
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
```

Verify:

```bash
kubectl get pods
kubectl logs -f deployment/workplace-tracker-deployment
```

---

## Minikube / Kubernetes Runbook

### 1. Build Image

Option A — local & push to Docker Hub:

```bash
docker build -t siddhantpatni0407/workplace-tracker-service:latest .
docker push siddhantpatni0407/workplace-tracker-service:latest
```

Option B — build directly inside Minikube:

```bash
minikube start --driver=docker
eval $(minikube -p minikube docker-env)
docker build -t siddhantpatni0407/workplace-tracker-service:latest .
eval $(minikube -p minikube docker-env --unset)
```

### 2. Start Minikube & dashboard

```bash
minikube start --driver=docker
minikube dashboard --url
```

### 3. Apply manifests

```bash
kubectl apply -f k8s/secret-workplace-tracker.yml
kubectl apply -f k8s/configmap-workplace-tracker.yml
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
```

### 4. Verify

```bash
kubectl get pods
kubectl get svc
kubectl logs -f deployment/workplace-tracker-deployment
```

Access service:

```bash
minikube service workplace-tracker-service --url
# or
http://localhost:30080
```

---

## Stopping & Cleanup

### Stop Minikube

```bash
minikube stop
```

### Delete Minikube cluster

```bash
minikube delete
```

### Delete Kubernetes resources

```bash
kubectl delete -f k8s/ingress.yml   --ignore-not-found
kubectl delete -f k8s/hpa.yml       --ignore-not-found
kubectl delete -f k8s/service.yml   --ignore-not-found
kubectl delete -f k8s/deployment.yml --ignore-not-found
kubectl delete -f k8s/configmap-workplace-tracker.yml --ignore-not-found
kubectl delete -f k8s/secret-workplace-tracker.yml --ignore-not-found
kubectl delete pvc postgres-pvc --ignore-not-found
```

### Docker cleanup

```bash
docker stop workplace-tracker || true
docker rm workplace-tracker || true
docker rmi siddhantpatni0407/workplace-tracker-service:latest || true
docker system prune -af
```

---

## Troubleshooting

* **401/403** → Token expired.
* **DB errors** → Check `SPRING_DATASOURCE_URL`, DB service.
* **Probes failing** → Confirm `/actuator/health` exposed.
* **Pods crashloop** → Check secrets/config.
* **Ingress not working** → Ensure ingress controller installed.

---

## Security Notice

* `forgot/reset` is insecure without OTP/email verification.
* Keep secrets in Kubernetes Secrets or vault, not git.
* Always use HTTPS in production.

---

## Contact & Contributing

* Maintainer: **Siddhant Patni**
* Issues/PRs welcome.

---

## License

Specify license in repository root (MIT/Apache-2.0).

---
