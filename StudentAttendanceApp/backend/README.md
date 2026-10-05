# Student Attendance System — Backend (optional)

> **Note:** The **live Android client** uses **Room/SQLite** via
> `RepositoryProvider` and does **not** require this API for normal teaching
> use. This Spring Boot service is an optional/legacy cloud companion. See
> [`../README.md`](../README.md) for the Android app.

A Spring Boot 3 / Java 17 REST API (MySQL or H2, Spring Security + JWT) that
can back experiments with the Android networking layer.

## Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8.0+ (or MariaDB 10.6+), **or** the bundled H2 in-memory database

## Quick start (H2)

```bash
cd backend
mvn spring-boot:run
```

API: `http://localhost:8080`. H2 data is lost on restart.

## MySQL setup

1. Apply scripts under `../database/`.
2. Copy `application.properties.example` → `application.properties`; set
   datasource credentials and a strong `app.jwt.secret`
   (`openssl rand -base64 48`).
3. Prefer `spring.jpa.hibernate.ddl-auto=validate` when schema was applied by hand.
4. `mvn spring-boot:run` or `mvn package` and run the jar.

**Never commit real credentials** — keep secrets out of source control.

## Smoke test

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@school.edu","password":"password123"}'
```

Full endpoint list: [`../docs/API.md`](../docs/API.md).

## Package layout (summary)

`entity` → `repository` → `service` → `controller`, plus `security`, `config`,
`dto`, and `exception` packages under `com.studentattendance.backend`.
