# Smart Student Attendance

Teacher/professor-focused Android app for managing class attendance by
**Department**, **Shift** (Morning/Evening), **Semester**, **Section**,
**Course/Subject**, and **Course Code**.

The live application stores data in **on-device Room/SQLite**. Teachers register
locally, create class setups, manage students, mark attendance, review history
and reports, and import/export Excel workbooks.

| Path | Contents |
|---|---|
| [`StudentAttendanceApp/`](StudentAttendanceApp/) | Android application (primary deliverable). See [`StudentAttendanceApp/README.md`](StudentAttendanceApp/README.md). |
| [`StudentAttendanceApp/backend/`](StudentAttendanceApp/backend/) | Optional Spring Boot REST API (not used by the live Room data path). |
| [`StudentAttendanceApp/database/`](StudentAttendanceApp/database/) | Optional MySQL scripts for that backend. |
| [`StudentAttendanceApp/docs/`](StudentAttendanceApp/docs/) | Additional notes (API reference, course layer). |

## Quick start (Android)

```bash
cd StudentAttendanceApp
.\gradlew.bat assembleDebug
```

Or open `StudentAttendanceApp/` in Android Studio and run the `app` module.

Create a teacher account on the login screen, then add your own departments,
courses, and students. There is no demo class data bundled with the app.

## Documentation

Full feature, Excel, architecture, and build instructions:

→ **[`StudentAttendanceApp/README.md`](StudentAttendanceApp/README.md)**
