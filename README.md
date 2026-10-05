# 📚Optimist Smart Student Attendance

A **teacher/professor-focused Android application** for managing student attendance by **Department, Shift, Semester, Section, Course/Subject, and Course Code**.

The primary application is **offline-first** and stores attendance data on the device using **Room over SQLite**. Teachers can register locally, create class setups, manage students, mark attendance, review attendance history and reports, and import/export student data through Excel workbooks.

---

## ✨ Features

### 👨‍🏫 Teacher & Class Management

* Local teacher registration and login
* Create and manage departments
* Support for **Morning / Evening** shifts
* Organize classes by:

  * Department
  * Semester
  * Section
  * Course / Subject
  * Course Code
* User/owner-based data separation

### 👨‍🎓 Student Management

* Add and manage students
* Store student names and roll numbers
* Preserve roll numbers as strings
* Manage students within their respective class/subject setup

### 📝 Attendance

Teachers can record attendance using:

| Status    | Description                 |
| --------- | --------------------------- |
| ✅ Present | Student attended the class  |
| ❌ Absent  | Student did not attend      |
| ⏰ Late    | Student arrived late        |
| 📋 Leave  | Student was marked on leave |

The application tracks attendance percentages and uses a **75% threshold** to identify students whose attendance falls below the required level.

### 📊 History & Reports

* Review previous attendance records
* View attendance history
* Track attendance percentages
* Identify students below the attendance threshold
* Generate/use attendance reports within the application

### 📥 Excel Import

The application supports importing student information from Excel workbooks.

Supported formats include:

* Column-wise student data
* Row-wise student data
* Hybrid metadata + student data

Required information can include:

* Department
* Semester
* Course / Subject
* Course Code
* Student Name
* Roll Number

The default shift is **Morning** when a shift is not provided.

### 📤 Excel Export

Attendance/student information can also be exported to Excel workbooks for external use and record keeping.

---

## 🧠 Application Workflow

```text
Teacher Registration / Login
            │
            ▼
       Department
            │
            ▼
     Shift / Semester
            │
            ▼
         Section
            │
            ▼
    Course / Subject
            │
            ▼
         Students
            │
            ▼
    Mark Attendance
            │
     ┌──────┼──────┬──────┐
     ▼      ▼      ▼      ▼
  Present Absent  Late   Leave
            │
            ▼
     Attendance History
            │
            ▼
   Percentage / Reports
```

---

## 🛠️ Technology Stack

### Android Application

| Component            | Technology                    |
| -------------------- | ----------------------------- |
| Programming Language | Java                          |
| UI                   | XML / Android Views           |
| Architecture         | ViewModel → Repository → Room |
| Local Database       | Room / SQLite                 |
| Database             | `student_attendance.db`       |
| Database Version     | v7                            |
| Build System         | Gradle                        |
| Runtime              | Android                       |

### Optional Backend

The repository also contains an **optional Spring Boot REST API**.

The backend is separate from the live Android application's Room/SQLite data path.

| Component              | Technology    |
| ---------------------- | ------------- |
| Backend                | Spring Boot   |
| API                    | REST          |
| Database               | MySQL         |
| Android Live Data Path | Room / SQLite |

The optional backend is included for development/extension purposes and is **not required to run the primary offline-first Android application**.

---

## 🏗️ Architecture

The Android application follows a layered architecture:

```text
┌─────────────────────────────┐
│        Android UI           │
│       XML / Views            │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│          ViewModel           │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│         Repository           │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│       Room Database          │
│          SQLite              │
└─────────────────────────────┘
```

This keeps the Android application focused on local, reliable attendance management without requiring an internet connection for the core workflow.

---

## 💾 Data Storage

The primary Android application uses **Room Persistence Library** with SQLite for on-device storage.

The database contains information associated with the application's attendance workflow, including:

* Teachers/users
* Departments
* Shifts
* Semesters
* Sections
* Subjects/courses
* Students
* Attendance records

The application follows an **offline-first** approach, allowing the main attendance workflow to operate using local device storage.

---

## 📥 Excel Import

Excel import is designed to make it easier to add existing student lists without manually entering every student.

The application supports:

```text
Column-wise data
       │
       ├── Student Name
       ├── Roll Number
       └── Other supported fields

Row-wise data
       │
       └── Student information per row

Hybrid format
       │
       ├── Class metadata
       └── Student records
```

Roll numbers are treated as **strings** so that values such as leading-zero roll numbers are not unintentionally converted into numbers.

---

## 🔐 Data & Privacy

The application is designed around local/on-device attendance management.

Before sharing screenshots, sample databases, Excel files, or recordings publicly:

* Use fictional student names
* Use fictional roll numbers
* Remove real teacher information
* Do not publish real attendance records
* Do not commit passwords, API keys, or private credentials

---

## 📱 Screenshots
<img width="300" height="600" alt="Image" src="https://github.com/user-attachments/assets/7c8435f0-9952-4d32-b09a-702b2e765b14" />

<img width="300" height="600" alt="Image" src="https://github.com/user-attachments/assets/f359e3cd-cc86-4075-944c-52f0d0846ce9" />

<img width="300" height="600" alt="Image" src="https://github.com/user-attachments/assets/33e944c1-6552-4eed-8a10-195d02d99f53" />

Screenshots can be added here to demonstrate the application.


---
##Video 

https://github.com/user-attachments/assets/9881f09a-aba2-46be-8764-2ffd32485bc9

---

##Live demo APK

StudentAttendance\StudentAttendanceApp\app\build\outputs\apk\debug\app-debug.apk


## 🚀 Getting Started

### Requirements

* Android Studio
* JDK 17
* Android SDK
* Gradle
* Android emulator or physical Android device

### Clone the repository

```bash
git clone https://github.com/codewithsami1234/Optimist-Student-Attendance.git
```

### Open the Android project

Open:

```text
StudentAttendanceApp/
```

in Android Studio.

Allow Gradle to synchronize, then run the `app` module on an Android emulator or connected Android device.

### Build from the command line

From inside `StudentAttendanceApp/`:

**Windows:**

```powershell
.\gradlew.bat assembleDebug
```

The generated debug APK will be available under the project's Gradle build output directory.

---

## 🧪 Testing & Verification

The Android project includes Gradle/Android tests for validating application functionality.

Run the available project checks from Android Studio or use the appropriate Gradle tasks from the `StudentAttendanceApp/` directory.

Example:

```powershell
.\gradlew.bat test
```

For Android build verification:

```powershell
.\gradlew.bat assembleDebug
```

---

## 📁 Repository Structure

```text
Optimist-Student-Attendance/
│
├── StudentAttendanceApp/
│   ├── app/
│   ├── backend/
│   ├── database/
│   ├── docs/
│   └── README.md
│
└── README.md
```

### Main paths

| Path                                                               | Purpose                                       |
| ------------------------------------------------------------------ | --------------------------------------------- |
| [`StudentAttendanceApp/`](StudentAttendanceApp/)                   | Primary Android application                   |
| [`StudentAttendanceApp/backend/`](StudentAttendanceApp/backend/)   | Optional Spring Boot REST API                 |
| [`StudentAttendanceApp/database/`](StudentAttendanceApp/database/) | Optional MySQL scripts for the backend        |
| [`StudentAttendanceApp/docs/`](StudentAttendanceApp/docs/)         | Additional API and course-layer documentation |

---

## 📚 Documentation

Detailed Android application documentation is available here:

**[`StudentAttendanceApp/README.md`](StudentAttendanceApp/README.md)**

It contains additional information about the application's features, architecture, Excel handling, database, and build process.

---

## 🎯 Project Purpose

This project was developed as a practical Android application to explore:

* Android application development
* Java
* XML-based Android UI
* Room / SQLite database management
* MVVM-style architecture
* Repository pattern
* Offline-first application design
* Student data management
* Attendance tracking
* Excel import/export
* REST API integration concepts

---

## 📌 Future Improvements

Possible future improvements include:

* More detailed attendance analytics
* Improved report generation
* PDF report export
* Additional Excel reporting options
* Backup and restore
* Cloud synchronization
* Multi-device synchronization
* More advanced teacher/admin roles
* Improved UI/UX
* Additional attendance statistics

---

## 📄 License

This project is provided for educational and development purposes.
