-- Upgrade script for existing Phase 2/4 MySQL databases.
-- Safe to re-run: each statement uses IF NOT EXISTS / IGNORE patterns where possible.
-- Fresh installs should use 01_schema.sql instead.

USE student_attendance;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS low_attendance_threshold DOUBLE NOT NULL DEFAULT 75;

ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE semesters
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sections
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

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

-- Default General subject per existing department (needed before sessions.subject_id is NOT NULL).
INSERT INTO subjects (id, department_id, name, code, archived)
SELECT CONCAT('subj_gen_', id), id, 'General', 'GEN', FALSE
FROM departments
WHERE NOT EXISTS (
    SELECT 1 FROM subjects s WHERE s.department_id = departments.id AND s.code = 'GEN'
);

ALTER TABLE attendance_sessions
    ADD COLUMN IF NOT EXISTS subject_id VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS subject_name VARCHAR(255) NULL;

UPDATE attendance_sessions sess
    JOIN subjects subj ON subj.department_id = sess.department_id AND subj.code = 'GEN'
SET sess.subject_id = subj.id,
    sess.subject_name = subj.name
WHERE sess.subject_id IS NULL OR sess.subject_id = '';

ALTER TABLE attendance_records
    ADD COLUMN IF NOT EXISTS subject_id VARCHAR(64) NULL;

UPDATE attendance_records rec
    JOIN attendance_sessions sess ON sess.id = rec.session_id
SET rec.subject_id = sess.subject_id
WHERE rec.subject_id IS NULL;
