-- Student Attendance System
-- MySQL schema. Run this once against a fresh MySQL/MariaDB server.

CREATE DATABASE IF NOT EXISTS student_attendance
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE student_attendance;

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                         VARCHAR(64)  NOT NULL PRIMARY KEY,
    name                       VARCHAR(255) NOT NULL,
    email                      VARCHAR(255) NOT NULL,
    password_hash              VARCHAR(255) NOT NULL,
    role                       VARCHAR(32)  NOT NULL,
    designation                VARCHAR(255),
    low_attendance_threshold   DOUBLE       NOT NULL DEFAULT 75,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- departments
-- Each department (and everything under it) is private to the professor
-- who created it - see owner_id / fk_departments_owner.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    id                  VARCHAR(64)  NOT NULL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    code                VARCHAR(16)  NOT NULL,
    total_students      INT          NOT NULL DEFAULT 0,
    head_of_department  VARCHAR(255),
    owner_id            VARCHAR(64)  NOT NULL,
    archived            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_departments_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_departments_owner_code UNIQUE (owner_id, code),
    INDEX idx_departments_owner (owner_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- subjects / courses (department-scoped). Every department gets a default
-- "General" subject so existing section-day attendance keeps working.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subjects (
    id             VARCHAR(64)  NOT NULL PRIMARY KEY,
    department_id  VARCHAR(64)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    code           VARCHAR(32)  NOT NULL,
    archived       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_subjects_department FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE CASCADE,
    CONSTRAINT uq_subjects_department_code UNIQUE (department_id, code),
    INDEX idx_subjects_department (department_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- semesters
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS semesters (
    id               VARCHAR(64)  NOT NULL PRIMARY KEY,
    department_id    VARCHAR(64)  NOT NULL,
    name             VARCHAR(255) NOT NULL,
    semester_number  INT          NOT NULL,
    archived         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_semesters_department FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE CASCADE,
    CONSTRAINT uq_semesters_department_number UNIQUE (department_id, semester_number),
    INDEX idx_semesters_department (department_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- sections
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sections (
    id             VARCHAR(64)  NOT NULL PRIMARY KEY,
    semester_id    VARCHAR(64)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    student_count  INT          NOT NULL DEFAULT 0,
    archived       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sections_semester FOREIGN KEY (semester_id)
        REFERENCES semesters (id) ON DELETE CASCADE,
    CONSTRAINT uq_sections_semester_name UNIQUE (semester_id, name),
    INDEX idx_sections_semester (semester_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- students
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id               VARCHAR(64)  NOT NULL PRIMARY KEY,
    roll_number      VARCHAR(32)  NOT NULL,
    name             VARCHAR(255) NOT NULL,
    department_id    VARCHAR(64)  NOT NULL,
    semester_id      VARCHAR(64)  NOT NULL,
    section_id       VARCHAR(64)  NOT NULL,
    email            VARCHAR(255),
    phone            VARCHAR(32),
    avatar_initials  VARCHAR(4),
    archived         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_students_section_roll UNIQUE (section_id, roll_number),
    CONSTRAINT fk_students_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    CONSTRAINT fk_students_semester   FOREIGN KEY (semester_id)   REFERENCES semesters (id)   ON DELETE CASCADE,
    CONSTRAINT fk_students_section    FOREIGN KEY (section_id)    REFERENCES sections (id)    ON DELETE CASCADE,
    INDEX idx_students_section (section_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- attendance_sessions
-- One class meeting: section + subject + date. Unique so a professor cannot
-- accidentally store two sessions for the same lecture.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_sessions (
    id                 VARCHAR(128) NOT NULL PRIMARY KEY,
    department_id      VARCHAR(64)  NOT NULL,
    department_name    VARCHAR(255) NOT NULL,
    semester_id        VARCHAR(64)  NOT NULL,
    semester_name      VARCHAR(255) NOT NULL,
    section_id         VARCHAR(64)  NOT NULL,
    section_name       VARCHAR(255) NOT NULL,
    subject_id         VARCHAR(64)  NOT NULL,
    subject_name       VARCHAR(255) NOT NULL,
    attendance_date    VARCHAR(10)  NOT NULL,
    created_at_millis  BIGINT       NOT NULL,
    CONSTRAINT fk_sessions_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sessions_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    CONSTRAINT uq_sessions_section_subject_date UNIQUE (section_id, subject_id, attendance_date),
    INDEX idx_sessions_department (department_id),
    INDEX idx_sessions_date (attendance_date)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- attendance_records
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_records (
    id                 VARCHAR(128) NOT NULL PRIMARY KEY,
    session_id         VARCHAR(128) NOT NULL,
    student_id         VARCHAR(64)  NOT NULL,
    student_name       VARCHAR(255) NOT NULL,
    roll_number        VARCHAR(32)  NOT NULL,
    section_id         VARCHAR(64)  NOT NULL,
    semester_id        VARCHAR(64)  NOT NULL,
    department_id      VARCHAR(64)  NOT NULL,
    subject_id         VARCHAR(64),
    attendance_date    VARCHAR(10)  NOT NULL,
    status             ENUM("PRESENT","ABSENT","LATE","LEAVE","NOT_MARKED") NOT NULL,
    marked_by          VARCHAR(255),
    marked_at_millis   BIGINT,
    CONSTRAINT fk_records_session FOREIGN KEY (session_id)
        REFERENCES attendance_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_records_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE RESTRICT,
    INDEX idx_records_session (session_id),
    INDEX idx_records_student (student_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- attendance_adjustments
-- Append-only ledger of teacher-controlled attendance relief. Never edits
-- attendance_records; AnalyticsService folds the net delta into the
-- *computed* rate at read time so the original history stays untouched.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_adjustments (
    id                  VARCHAR(64)  NOT NULL PRIMARY KEY,
    student_id          VARCHAR(64)  NOT NULL,
    department_id       VARCHAR(64)  NOT NULL,
    delta               INT          NOT NULL,
    reason              VARCHAR(500) NOT NULL,
    adjusted_by_user_id VARCHAR(64)  NOT NULL,
    adjusted_by_name    VARCHAR(255) NOT NULL,
    created_at_millis   BIGINT       NOT NULL,
    CONSTRAINT fk_adjustments_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_adjustments_teacher FOREIGN KEY (adjusted_by_user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_adjustments_student (student_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- notifications
-- In-app alert feed, polled by the client. See NotificationService.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id                   VARCHAR(64)  NOT NULL PRIMARY KEY,
    user_id              VARCHAR(64)  NOT NULL,
    type                 VARCHAR(64)  NOT NULL,
    message              VARCHAR(500) NOT NULL,
    related_student_id   VARCHAR(64),
    `read`               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at_millis    BIGINT       NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id, created_at_millis)
) ENGINE=InnoDB;
