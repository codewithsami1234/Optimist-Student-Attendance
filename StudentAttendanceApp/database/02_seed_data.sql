-- Student Attendance System - Phase 2
-- Demo/seed data matching the Phase 1 Android mock data exactly, so first
-- run behaviour against the real API is identical to the Phase 1 in-memory
-- mocks. Run this AFTER 01_schema.sql.

USE student_attendance;

-- ---------------------------------------------------------------------------
-- Demo login: teacher@school.edu / password123
-- The hash below is a genuine bcrypt hash (cost 12) of "password123",
-- verifiable by Spring Security's BCryptPasswordEncoder. Regenerate your own
-- hash (e.g. via PasswordEncoderTest, or any bcrypt tool) before using real
-- credentials in a non-demo deployment.
-- ---------------------------------------------------------------------------
INSERT INTO users (id, name, email, password_hash, role, designation) VALUES
    ('user_1', 'Ayesha Khan', 'teacher@school.edu', '$2b$12$MpxGDKVTdbchbtfYX1WmJO89wXlUTCQm2DWkifiDhZwfhwLQukg26', 'Teacher', 'Assistant Professor')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- Departments
-- Phase 4: every department belongs to a professor (owner_id) - this seed
-- data all belongs to the demo teacher account above.
-- ---------------------------------------------------------------------------
INSERT INTO departments (id, name, code, total_students, head_of_department, owner_id) VALUES
    ('dept_cs',   'Computer Science',        'CS',   20, 'Dr. Ayesha Khan',   'user_1'),
    ('dept_ee',   'Electrical Engineering',  'EE',   20, 'Dr. Bilal Ahmed',   'user_1'),
    ('dept_bba',  'Business Administration', 'BBA',  20, 'Dr. Sana Malik',    'user_1'),
    ('dept_math', 'Mathematics',             'MATH', 20, 'Dr. Imran Sheikh',  'user_1')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- Semesters (4 per department) + Sections (2 per semester: Section A/B)
-- ---------------------------------------------------------------------------
INSERT INTO semesters (id, department_id, name, semester_number) VALUES
    ('dept_cs_sem1', 'dept_cs', 'Semester 1', 1),
    ('dept_cs_sem2', 'dept_cs', 'Semester 2', 2),
    ('dept_cs_sem3', 'dept_cs', 'Semester 3', 3),
    ('dept_cs_sem4', 'dept_cs', 'Semester 4', 4),
    ('dept_ee_sem1', 'dept_ee', 'Semester 1', 1),
    ('dept_ee_sem2', 'dept_ee', 'Semester 2', 2),
    ('dept_ee_sem3', 'dept_ee', 'Semester 3', 3),
    ('dept_ee_sem4', 'dept_ee', 'Semester 4', 4),
    ('dept_bba_sem1', 'dept_bba', 'Semester 1', 1),
    ('dept_bba_sem2', 'dept_bba', 'Semester 2', 2),
    ('dept_bba_sem3', 'dept_bba', 'Semester 3', 3),
    ('dept_bba_sem4', 'dept_bba', 'Semester 4', 4),
    ('dept_math_sem1', 'dept_math', 'Semester 1', 1),
    ('dept_math_sem2', 'dept_math', 'Semester 2', 2),
    ('dept_math_sem3', 'dept_math', 'Semester 3', 3),
    ('dept_math_sem4', 'dept_math', 'Semester 4', 4)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sections (id, semester_id, name, student_count) VALUES
    ('dept_cs_sem1_sectiona', 'dept_cs_sem1', 'Section A', 10),
    ('dept_cs_sem1_sectionb', 'dept_cs_sem1', 'Section B', 0),
    ('dept_cs_sem2_sectiona', 'dept_cs_sem2', 'Section A', 0),
    ('dept_cs_sem2_sectionb', 'dept_cs_sem2', 'Section B', 0),
    ('dept_cs_sem3_sectiona', 'dept_cs_sem3', 'Section A', 0),
    ('dept_cs_sem3_sectionb', 'dept_cs_sem3', 'Section B', 0),
    ('dept_cs_sem4_sectiona', 'dept_cs_sem4', 'Section A', 0),
    ('dept_cs_sem4_sectionb', 'dept_cs_sem4', 'Section B', 0),
    ('dept_ee_sem1_sectiona', 'dept_ee_sem1', 'Section A', 10),
    ('dept_ee_sem1_sectionb', 'dept_ee_sem1', 'Section B', 0),
    ('dept_ee_sem2_sectiona', 'dept_ee_sem2', 'Section A', 0),
    ('dept_ee_sem2_sectionb', 'dept_ee_sem2', 'Section B', 0),
    ('dept_ee_sem3_sectiona', 'dept_ee_sem3', 'Section A', 0),
    ('dept_ee_sem3_sectionb', 'dept_ee_sem3', 'Section B', 0),
    ('dept_ee_sem4_sectiona', 'dept_ee_sem4', 'Section A', 0),
    ('dept_ee_sem4_sectionb', 'dept_ee_sem4', 'Section B', 0),
    ('dept_bba_sem1_sectiona', 'dept_bba_sem1', 'Section A', 10),
    ('dept_bba_sem1_sectionb', 'dept_bba_sem1', 'Section B', 0),
    ('dept_bba_sem2_sectiona', 'dept_bba_sem2', 'Section A', 0),
    ('dept_bba_sem2_sectionb', 'dept_bba_sem2', 'Section B', 0),
    ('dept_bba_sem3_sectiona', 'dept_bba_sem3', 'Section A', 0),
    ('dept_bba_sem3_sectionb', 'dept_bba_sem3', 'Section B', 0),
    ('dept_bba_sem4_sectiona', 'dept_bba_sem4', 'Section A', 0),
    ('dept_bba_sem4_sectionb', 'dept_bba_sem4', 'Section B', 0),
    ('dept_math_sem1_sectiona', 'dept_math_sem1', 'Section A', 10),
    ('dept_math_sem1_sectionb', 'dept_math_sem1', 'Section B', 0),
    ('dept_math_sem2_sectiona', 'dept_math_sem2', 'Section A', 0),
    ('dept_math_sem2_sectionb', 'dept_math_sem2', 'Section B', 0),
    ('dept_math_sem3_sectiona', 'dept_math_sem3', 'Section A', 0),
    ('dept_math_sem3_sectionb', 'dept_math_sem3', 'Section B', 0),
    ('dept_math_sem4_sectiona', 'dept_math_sem4', 'Section A', 0),
    ('dept_math_sem4_sectionb', 'dept_math_sem4', 'Section B', 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- Students: full semester-1 / Section A roster per department (matches the
-- Phase 1 mock roster exactly, including roll numbers and generated emails).
-- ---------------------------------------------------------------------------
INSERT INTO students (id, roll_number, name, department_id, semester_id, section_id, email, phone, avatar_initials) VALUES
    ('dept_cs_sem1_sectiona_st0', 'CS-001', 'Ahmed Raza',     'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'ahmed.raza@university.edu',     '0300000001', 'AR'),
    ('dept_cs_sem1_sectiona_st1', 'CS-002', 'Fatima Noor',    'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'fatima.noor@university.edu',    '0300000002', 'FN'),
    ('dept_cs_sem1_sectiona_st2', 'CS-003', 'Hassan Ali',     'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'hassan.ali@university.edu',     '0300000003', 'HA'),
    ('dept_cs_sem1_sectiona_st3', 'CS-004', 'Zainab Malik',   'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'zainab.malik@university.edu',   '0300000004', 'ZM'),
    ('dept_cs_sem1_sectiona_st4', 'CS-005', 'Usman Tariq',    'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'usman.tariq@university.edu',    '0300000005', 'UT'),
    ('dept_cs_sem1_sectiona_st5', 'CS-006', 'Ayesha Siddiqui','dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'ayesha.siddiqui@university.edu','0300000006', 'AS'),
    ('dept_cs_sem1_sectiona_st6', 'CS-007', 'Bilal Hussain',  'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'bilal.hussain@university.edu',  '0300000007', 'BH'),
    ('dept_cs_sem1_sectiona_st7', 'CS-008', 'Mariam Yousaf',  'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'mariam.yousaf@university.edu',  '0300000008', 'MY'),
    ('dept_cs_sem1_sectiona_st8', 'CS-009', 'Omar Farooq',    'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'omar.farooq@university.edu',    '0300000009', 'OF'),
    ('dept_cs_sem1_sectiona_st9', 'CS-010', 'Sana Iqbal',     'dept_cs', 'dept_cs_sem1', 'dept_cs_sem1_sectiona', 'sana.iqbal@university.edu',     '0300000010', 'SI'),

    ('dept_ee_sem1_sectiona_st0', 'EE-011', 'Hamza Sheikh',   'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'hamza.sheikh@university.edu',   '0300000011', 'HS'),
    ('dept_ee_sem1_sectiona_st1', 'EE-012', 'Iqra Batool',    'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'iqra.batool@university.edu',    '0300000012', 'IB'),
    ('dept_ee_sem1_sectiona_st2', 'EE-013', 'Danish Khalid',  'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'danish.khalid@university.edu',  '0300000013', 'DK'),
    ('dept_ee_sem1_sectiona_st3', 'EE-014', 'Rabia Anwar',    'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'rabia.anwar@university.edu',    '0300000014', 'RA'),
    ('dept_ee_sem1_sectiona_st4', 'EE-015', 'Talha Yaseen',   'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'talha.yaseen@university.edu',   '0300000015', 'TY'),
    ('dept_ee_sem1_sectiona_st5', 'EE-016', 'Nida Kazmi',     'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'nida.kazmi@university.edu',     '0300000016', 'NK'),
    ('dept_ee_sem1_sectiona_st6', 'EE-017', 'Faisal Mehmood', 'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'faisal.mehmood@university.edu', '0300000017', 'FM'),
    ('dept_ee_sem1_sectiona_st7', 'EE-018', 'Sadia Rehman',   'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'sadia.rehman@university.edu',   '0300000018', 'SR'),
    ('dept_ee_sem1_sectiona_st8', 'EE-019', 'Adnan Qureshi',  'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'adnan.qureshi@university.edu',  '0300000019', 'AQ'),
    ('dept_ee_sem1_sectiona_st9', 'EE-020', 'Hira Aslam',     'dept_ee', 'dept_ee_sem1', 'dept_ee_sem1_sectiona', 'hira.aslam@university.edu',     '0300000020', 'HA'),

    ('dept_bba_sem1_sectiona_st0', 'BBA-021', 'Waqas Ahmed',   'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'waqas.ahmed@university.edu',   '0300000021', 'WA'),
    ('dept_bba_sem1_sectiona_st1', 'BBA-022', 'Amna Riaz',     'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'amna.riaz@university.edu',     '0300000022', 'AR'),
    ('dept_bba_sem1_sectiona_st2', 'BBA-023', 'Shahzad Iqbal', 'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'shahzad.iqbal@university.edu', '0300000023', 'SI'),
    ('dept_bba_sem1_sectiona_st3', 'BBA-024', 'Komal Nawaz',   'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'komal.nawaz@university.edu',   '0300000024', 'KN'),
    ('dept_bba_sem1_sectiona_st4', 'BBA-025', 'Junaid Akbar',  'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'junaid.akbar@university.edu',  '0300000025', 'JA'),
    ('dept_bba_sem1_sectiona_st5', 'BBA-026', 'Laiba Hassan',  'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'laiba.hassan@university.edu',  '0300000026', 'LH'),
    ('dept_bba_sem1_sectiona_st6', 'BBA-027', 'Kashif Nadeem', 'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'kashif.nadeem@university.edu', '0300000027', 'KN'),
    ('dept_bba_sem1_sectiona_st7', 'BBA-028', 'Mahnoor Zafar', 'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'mahnoor.zafar@university.edu', '0300000028', 'MZ'),
    ('dept_bba_sem1_sectiona_st8', 'BBA-029', 'Salman Raza',   'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'salman.raza@university.edu',   '0300000029', 'SR'),
    ('dept_bba_sem1_sectiona_st9', 'BBA-030', 'Anum Shahid',   'dept_bba', 'dept_bba_sem1', 'dept_bba_sem1_sectiona', 'anum.shahid@university.edu',   '0300000030', 'AS'),

    ('dept_math_sem1_sectiona_st0', 'MATH-031', 'Bilawal Khan',   'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'bilawal.khan@university.edu',   '0300000031', 'BK'),
    ('dept_math_sem1_sectiona_st1', 'MATH-032', 'Rida Fatima',    'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'rida.fatima@university.edu',    '0300000032', 'RF'),
    ('dept_math_sem1_sectiona_st2', 'MATH-033', 'Zeeshan Haider', 'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'zeeshan.haider@university.edu', '0300000033', 'ZH'),
    ('dept_math_sem1_sectiona_st3', 'MATH-034', 'Sidra Yousuf',   'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'sidra.yousuf@university.edu',   '0300000034', 'SY'),
    ('dept_math_sem1_sectiona_st4', 'MATH-035', 'Arslan Butt',    'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'arslan.butt@university.edu',    '0300000035', 'AB'),
    ('dept_math_sem1_sectiona_st5', 'MATH-036', 'Noor ul Ain',    'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'noor.ul.ain@university.edu',    '0300000036', 'NA'),
    ('dept_math_sem1_sectiona_st6', 'MATH-037', 'Kamran Ashraf',  'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'kamran.ashraf@university.edu',  '0300000037', 'KA'),
    ('dept_math_sem1_sectiona_st7', 'MATH-038', 'Aleena Malik',   'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'aleena.malik@university.edu',   '0300000038', 'AM'),
    ('dept_math_sem1_sectiona_st8', 'MATH-039', 'Rehan Zaman',    'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'rehan.zaman@university.edu',    '0300000039', 'RZ'),
    ('dept_math_sem1_sectiona_st9', 'MATH-040', 'Warda Naseem',   'dept_math', 'dept_math_sem1', 'dept_math_sem1_sectiona', 'warda.naseem@university.edu',   '0300000040', 'WN')
ON DUPLICATE KEY UPDATE name = VALUES(name);
