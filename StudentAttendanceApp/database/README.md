# Database setup (optional backend)

> **Note:** The **live Android app** stores data in **Room/SQLite on device**,
> not in this MySQL schema. These scripts support the optional Spring Boot API
> under `../backend/`. See [`../README.md`](../README.md) for the Android app.

These scripts create the MySQL schema used by the Spring Boot backend
(`backend/`) for optional cloud-style deployments.

## Prerequisites
- MySQL 8.0+ (or MariaDB 10.6+, which is wire-compatible)
- A MySQL user with permission to create databases

## Steps

```bash
mysql -u root -p < 01_schema.sql
mysql -u root -p < 02_seed_data.sql
```

Existing databases created before the subject/archive upgrade:

```bash
mysql -u root -p student_attendance < 03_upgrade.sql
```

This creates the `student_attendance` database, all tables, and seed data for
backend trials. Then copy `backend/src/main/resources/application.properties.example`
to `application.properties` and fill in DB credentials and a real JWT secret
before starting the backend.

## Alternative: auto-seed on boot

If you would rather not run these scripts by hand, start the backend with
`spring.jpa.hibernate.ddl-auto=update` against an *empty* database — Hibernate
will create the tables and `DataSeeder` will populate demo data on first boot.
The SQL scripts here are for deployments where DDL is applied explicitly and
`ddl-auto=validate` is used instead.
