# Student Attendance System — API (optional backend)

> **Note:** This document describes the **optional Spring Boot REST API**.
> The live Android app’s teaching workflow uses **on-device Room/SQLite**.
> See [`../README.md`](../README.md).

Base URL (local development): `http://10.0.2.2:8080/` (Android emulator) or
`http://<LAN-IP>:8080/` (physical device), when the app is pointed at a
running backend.

All endpoints are JSON (`Content-Type: application/json`) and prefixed with `/api`.

## Authentication

Public (rate-limited): `POST /api/auth/register`, `POST /api/auth/login`,
`POST /api/auth/logout`. Everything else requires:

```
Authorization: Bearer <token>
```

Roles: `TEACHER` and `ADMIN`. JWT secret is `app.jwt.secret` /
`${JWT_SECRET:...}` (do not use the local-dev default in production).
Logout blacklists the presented token in-process.

### POST /api/auth/register

`201 Created` — `{ "token", "user" }`. Password min 8 characters.

### POST /api/auth/login

```json
{ "email": "teacher@school.edu", "password": "password123" }
```

`200 OK` — `{ "token", "user" }` where `user` includes `lowAttendanceThreshold`.
`401` invalid credentials. `429` if login/register is rate-limited.

### POST /api/auth/logout

Optional `Authorization` header. Blacklists the JWT when present. `204`.

### GET /api/profile · PUT /api/profile

Profile includes `lowAttendanceThreshold` (default 75). PUT body:

```json
{ "name": "Ayesha Khan", "designation": "Associate Professor", "lowAttendanceThreshold": 80 }
```

## Ownership

Every department (and everything under it) is private to the professor who
created it. Cross-account access returns `403 Forbidden`.

## Departments / semesters / sections / subjects

| Method | Path | Notes |
|---|---|---|
| GET | `/api/departments` | Current user's departments |
| POST | `/api/departments` | `{ name, code, headOfDepartment }` |
| PUT | `/api/departments/{id}` | `{ name, code, headOfDepartment, archived }` |
| DELETE | `/api/departments/{id}` | Blocked if students/sessions exist |
| GET/POST | `/api/departments/{id}/semesters` | Unique semester number per department |
| PUT/DELETE | `/api/semesters/{id}` | |
| GET/POST | `/api/semesters/{id}/sections` | Unique section name per semester |
| PUT/DELETE | `/api/sections/{id}` | |
| GET/POST | `/api/departments/{id}/subjects?includeArchived=` | Unique code per department |
| PUT/DELETE | `/api/subjects/{id}` | |

Each department is created with a default **General** (`GEN`) subject so
section-day attendance still works. Selection flow is Department → Semester
→ Section → Subject → Date. Unique attendance session key is
`(sectionId, subjectId, date)`.

## Students

| Method | Path |
|---|---|
| GET | `/api/students?departmentId=&semesterId=&sectionId=&includeArchived=` (`sectionId` required) |
| GET | `/api/students/all?includeArchived=` |
| POST | `/api/students` |
| PUT | `/api/students/{id}` `{ name, rollNumber, email, phone, archived }` |
| POST | `/api/students/{id}/transfer` `{ sectionId }` |
| DELETE | `/api/students/{id}` (blocked if the student has attendance history — archive instead) |
| GET | `/api/students/{id}/report` |
| POST | `/api/students/{id}/adjustments` |
| GET | `/api/students/{id}/adjustments` |

### POST /api/students/import

Body: JSON array of students. Response is **not** an integer:

```json
{
  "imported": 10,
  "duplicates": 2,
  "invalid": 1,
  "errors": [
    { "row": 3, "rollNumber": "CS-001", "message": "Duplicate roll number in this section." }
  ]
}
```

Invalid and duplicate rows are reported. They are not imported silently.

## Attendance

### POST /api/attendance

```json
{
  "departmentId": "...", "departmentName": "...",
  "semesterId": "...", "semesterName": "...",
  "sectionId": "...", "sectionName": "...",
  "subjectId": "...", "subjectName": "General",
  "date": "2026-08-30",
  "records": [
    { "studentId": "...", "studentName": "...", "rollNumber": "...", "status": "PRESENT" }
  ]
}
```

Rules: every roster student must be present in `records`; every status must be
`PRESENT` / `ABSENT` / `LATE` / `LEAVE`; students must belong to the section;
future dates are rejected; re-save for the same section+subject+date replaces
the session.

### GET /api/attendance/session?departmentId=&semesterId=&sectionId=&subjectId=&date=

`200` with the session, or empty/204 if none exists (re-marking loads this).

### GET /api/attendance/sessions?departmentId=&semesterId=&sectionId=&subjectId=&fromDate=&toDate=&page=&size=

Paginated list, newest first.

### GET /api/reports/sessions

All sessions for the current user (reports feed).

## Attendance percentage policy

Used by dashboard, reports, student detail, section stats, trends,
low-attendance alerts, adjustments, and CSV summaries:

- **Present + Late** = attended
- **Absent** counts against (denominator)
- **Leave** is excused (excluded from both)
- **Adjustments** add to the attended count
- Rate is clamped 0–100; `0` if there are no countable days

Configurable low-attendance threshold is per user (`lowAttendanceThreshold`).

## Analytics / dashboard / notifications

| Method | Path |
|---|---|
| GET | `/api/dashboard/stats` |
| GET | `/api/analytics/section-stats?departmentId=&semesterId=&subjectId=` |
| GET | `/api/analytics/trend?departmentId=&subjectId=&days=` |
| GET | `/api/analytics/low-attendance?threshold=&limit=` |
| GET | `/api/notifications?page=&size=` |
| GET | `/api/notifications/unread-count` → `{ "unreadCount": n }` |
| PUT | `/api/notifications/{id}/read` |
| PUT | `/api/notifications/read-all` |

Dashboard stats include `weeklyTrend`, `lowAttendanceStudents`,
`lowAttendanceThreshold`, `lateToday`, `leaveToday`.

## Error format

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "...", "path": "/api/..." }
```

| Status | Meaning |
|---|---|
| 400 | Validation / business rule |
| 401 | Missing/invalid/expired/blacklisted token |
| 403 | Ownership violation |
| 404 | Not found |
| 429 | Auth rate limit |
| 500 | Unexpected server error |

## CORS / production

`app.cors.allowed-origins` is a comma-separated allow-list. `*` plus credentials
is not used. For production set `JWT_SECRET`, `CORS_ORIGINS`, and HTTPS.
