# Smart Student Attendance

Teacher/professor Android application for organizing and recording student
attendance by academic class.

**Who it is for:** instructors who need offline, per-class attendance with
history, reports, and Excel import/export — suitable for college/university
use, FYP evaluation, and further development.

---

## Project overview

The app lets a teacher:

1. Register / sign in (local hashed account on device)
2. Set up a class: **Department + Shift + Semester + Section (optional) + Course + Course Code**
3. Add students (name + roll number) to that class
4. Mark attendance for a session (**Present / Absent / Late / Leave**)
5. Review **History** and **Reports**
6. **Import** student lists and **export** professional Excel reports

All live academic and attendance data is stored in **Room / SQLite** on the
device, scoped by `ownerUserId` (the signed-in teacher).

---

## Features

### Academic organization

| Concept | Notes |
|---|---|
| **Department** | Academic department name (e.g. BS Information Technology) |
| **Shift** | **Morning** or **Evening** — persisted on the department; same name can exist once per shift |
| **Semester** | Semester number 1–8 |
| **Section** | Optional section name (blank allowed; editable later) |
| **Course / Subject** | Course name |
| **Course Code** | Unique within a section (stored uppercase) |

Typical hierarchy used by the app:

```text
Department (+ Shift)
  └── Semester
        └── Section (optional name)
              └── Course / Subject (+ Course Code)
                    └── Students
                          └── Attendance sessions / records
```

Departments are created through **Add Department / Set up class**, which saves
department, semester, section, and course together as a **class setup** card.

### Student management

- Add / edit students with **Student Name** and **Roll Number**
- Roll number is an **identifier string** (not a floating-point number)
- Duplicate roll numbers are rejected within the same section
- Students belong to a department / semester / section under the current teacher
- Import students from Excel/CSV; export roster or download a template

### Attendance

Statuses (enum labels):

| Status | Used in percentage? |
|---|---|
| **Present** | Yes — counts as attended |
| **Late** | Yes — counts as attended |
| **Absent** | Yes — counts against the rate |
| **Leave** | Excused — **excluded** from the percentage denominator |
| **Not marked** | UI placeholder before save; not a saved marked status |

**Policy** (`AttendancePolicy`):

- Attended = Present + Late (+ optional adjustment delta)
- Countable total = Present + Late + Absent  
- Leave does not reduce or inflate the rate
- Default low-attendance threshold: **75%**
- Rate formula: `(present + late + adjustment) / (present + late + absent) × 100`, clamped to 0–100

### History

- Session cards grouped by date
- Filters (chips / dialog): Department, Semester, Section, Course, Course Code, date range
- Open a session for detail view
- **Export Excel** from History with filters including Department, Shift, Section, Course, Course Code, and dates (optional filters may be left blank = no restriction on that axis; department and dates are required for the consolidated History export)
- **Specific-day export** from session detail: only that session’s records, **no Attendance %**

### Reports

- Summary cards, student lists, trends
- Filters: Department, **Shift** (All / Morning / Evening), Semester, Section, Course, Course Code, date range, status chips, sort
- Export semester report workbook from the Reports screen

---

## Excel import & export

Excel uses a built-in **Office Open XML** helper (`XlsxIo`) — **not Apache POI**.
Cells are written as text (`inlineStr`) so roll numbers stay identifiers.

### Import — students

- Expected columns: **Student Name**, **Roll Number** (header names containing `name` / `roll`)
- Roll numbers are treated as text/identifiers
- `123.0` / `123.00` normalize to `123` (Excel numeric artifact)
- Text values such as `00123`, `BSIT-001`, `23-IT-045` are preserved
- Title/metadata rows above the real header are skipped until Name + Roll headers are found
- **Limitation:** if the source workbook already stored a roll as a pure Excel **number**, leading zeros cannot be recovered (Excel itself dropped them)

### Import — attendance

- Parses rows with Roll Number, Date, and Status (and optional name/course code)
- Roll numbers use the same identifier normalization

### Export — professional layout

Class/report-level fields appear **once** above the table (not on every student row), for example:

- Department, Shift, Semester, Section, Course, Course Code, Date / Date Range

Then a student table with only row-specific columns, such as:

- `#`, Student Name, Roll No, Attendance Status  
- Date (when the export spans multiple dates)  
- Present / Late / Absent / Leave counts and Attendance % (for summary-style reports)

Exact columns depend on the export type.

### Export types (implemented)

| Export | What it provides |
|---|---|
| **Daily session** (History → open card → Export) | One session/day; metadata + `#` / Name / Roll / Status; **no %** |
| **History consolidated report** | Filtered class + date range; one row per student with period totals and % |
| **Attendance + Summary workbook** | Detail records sheet + summary sheet |
| **Attendance % summary** | Per-student present/late/absent/leave/adjustment and % |
| **Student roster** | Name + Roll Number |
| **Student import template** | Header + example row (XLSX or CSV) |
| **Semester report** (Reports) | Semester summary sheet, student summary, per-course date matrix |

### Spreadsheet formatting (implemented)

For professional report sheets (`XlsxIo` professional mode):

- Report title styling  
- Bold metadata labels  
- Bold shaded table headers with borders  
- Bordered body cells  
- Column widths  
- Frozen header row + AutoFilter  
- Landscape / fit-to-width page setup  
- Roll No written as text  

---

## Reports & History filters

| Filter | History list | History Excel export | Reports |
|---|---|---|---|
| Department | ✓ | ✓ (required) | ✓ |
| Shift | — | ✓ (optional) | ✓ (All / Morning / Evening) |
| Semester | ✓ | derived from section when selected | ✓ |
| Section | ✓ | ✓ (optional) | ✓ |
| Course / Subject | ✓ | ✓ (optional) | ✓ |
| Course Code | ✓ | ✓ (optional) | ✓ |
| Date / range | ✓ | ✓ (required From/To) | ✓ |

Blank / **All** on optional filters means *do not filter* on that field.

---

## Security & data ownership

**Implemented:**

- Local teacher registration / login (hashed passwords via local auth)
- Data scoped by `ownerUserId` in Room
- `android:allowBackup="false"` in the manifest
- Offline-first: live repositories are Room implementations (`RepositoryProvider`)

**Not claimed as production cloud security:**

- The repo also contains an optional Spring Boot API and MySQL scripts. The
  **shipped app path does not use them** for live data; Retrofit-related code
  remains in the tree for optional/legacy cloud wiring, not as the active
  source of truth.

---

## Technology stack

Verified from `app/build.gradle` and source:

| Area | Technology |
|---|---|
| Language | **Java 17** |
| UI | XML layouts, **ViewBinding**, Material Components |
| Architecture | Activities / Fragments → ViewModel → Repository → **Room** |
| Database | Room 2.6.1 / SQLite (`student_attendance.db`, version **8**) |
| Navigation | AndroidX Navigation |
| Networking deps | Retrofit / OkHttp present; **live data path is Room** |
| Excel | Custom **`XlsxIo`** (OOXML ZIP), not Apache POI |
| Tests | JUnit 4 unit tests |
| minSdk / targetSdk / compileSdk | 24 / 34 / 34 |
| applicationId | `com.example.studentattendance` |

---

## Architecture

```text
UI (Activities / Fragments / Adapters)
        ↓
ViewModels (LiveData)
        ↓
Repositories (interfaces)
        ↓
Room* implementations  ←── RepositoryProvider (live)
        ↓
AppDatabase (Room) → DAOs → Entities
        ↓
CsvExcelService / XlsxIo / AttendancePolicy / HistoryFilter / SemesterReport
```

Important packages under `com.example.studentattendance`:

- `ui` — screens (departments, students, attendance, history, reports, …)
- `repository` / `repository.impl` — data access
- `data.local` — Room database, entities, DAOs, migrations
- `service` — Excel (`CsvExcelService`, `XlsxIo`), `RepositoryProvider`
- `utils` — `AttendancePolicy`, export row builders, filters
- `model` — domain models (`Department`, `DepartmentShift`, `AttendanceStatus`, …)

---

## Project structure

```text
StudentAttendanceApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/studentattendance/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/          # unit tests
│   │   └── androidTest/   # instrumentation stubs
│   └── build.gradle
├── backend/               # optional Spring Boot API (not live Room path)
├── database/              # optional MySQL scripts for backend
├── docs/                  # API.md, SUBJECTS.md
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md              # this file
```

---

## Setup

1. Install Android Studio with JDK 17 and Android SDK 34.
2. Open the `StudentAttendanceApp` folder.
3. Sync Gradle; resolve SDKs if prompted.
4. Run the `app` configuration on an emulator or device (API 24+).

Optional: `local.properties` may define `API_BASE_URL` / `GOOGLE_WEB_CLIENT_ID`
for experimental cloud/Google features; they are **not required** for the
local Room workflow.

---

## Build & test

From `StudentAttendanceApp/` (Windows examples):

```bash
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:compileDebugJavaWithJavac
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

---

## Current validation

Last verified on this codebase:

| Check | Result |
|---|---|
| `:app:compileDebugJavaWithJavac` | Passed |
| `:app:testDebugUnitTest` | Passed |
| `:app:lintDebug` | Passed |

---

## Limitations

- Offline / single-device focus: no multi-device live sync in the active Room path
- Excel cannot restore leading zeros if the source file stored the roll as a number
- Optional backend/MySQL under `backend/` and `database/` are separate from the live app data store
- Release signing / Play Store publishing configuration is not productized in-repo (`minifyEnabled false`)

---

## Future enhancements

Ideas only (not implemented): cloud sync for the live path, Play Store release
hardening, richer multi-lecture UX beyond the current session model.
