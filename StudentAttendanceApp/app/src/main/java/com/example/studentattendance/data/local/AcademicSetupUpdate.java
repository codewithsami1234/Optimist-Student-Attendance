package com.example.studentattendance.data.local;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.Department;

/**
 * Updates one saved class/course card in place using the same fields as Add
 * Department. Existing primary keys and foreign-key relationships are kept.
 */
public final class AcademicSetupUpdate {

    private AcademicSetupUpdate() {
    }

    public static Department execute(AppDatabase db, String ownerId, String subjectId,
                                     String departmentName, String shift, int semesterNumber, String sectionName,
                                     String courseName, String courseCode) {
        return db.runInTransaction((Callable<Department>) () -> {
            Result result = update(new RoomGraph(db), ownerId, subjectId, departmentName, shift,
                    semesterNumber, sectionName, courseName, courseCode);
            return result.department.toModel(db.studentDao().countInDepartment(ownerId, result.department.id));
        });
    }

    static Result update(Graph graph, String ownerId, String subjectId, String departmentName,
                         String shift, int semesterNumber, String sectionName, String courseName,
                         String courseCode) {
        if (graph == null) {
            throw new LocalDataException("Class setup could not be updated.");
        }
        if (departmentName == null || departmentName.trim().isEmpty()) {
            throw new LocalDataException("Department name is required.");
        }
        if (semesterNumber < 1 || semesterNumber > 8) {
            throw new LocalDataException("Semester number must be between 1 and 8.");
        }
        if (courseName == null || courseName.trim().isEmpty()
                || courseCode == null || courseCode.trim().isEmpty()) {
            throw new LocalDataException("Course name and course code are required.");
        }

        SubjectEntity subject = graph.requireSubject(ownerId, subjectId);
        DepartmentEntity department = graph.requireDepartment(ownerId, subject.departmentId);
        SemesterEntity semester = graph.requireSemester(ownerId, subject.semesterId);
        SectionEntity section = graph.requireSection(ownerId, subject.sectionId);

        String trimmedDepartmentName = departmentName.trim();
        String resolvedSectionName = sectionName == null ? "" : sectionName.trim();
        String normalizedCode = courseCode.trim().toUpperCase();
        String resolvedShift = com.example.studentattendance.model.DepartmentShift.normalize(shift);

        SemesterEntity semesterClash = graph.findSemesterByNumber(ownerId, department.id, semesterNumber);
        if (semesterClash != null && !semesterClash.id.equals(semester.id)) {
            throw new LocalDataException("A semester with this number already exists in this department.");
        }

        // Mirror createAcademicSetup: reuse a section by name when present, otherwise
        // rename in place when this subject is the only user, or insert a new section
        // when the current (often blank) section is shared with other classes.
        SectionEntity targetSection = resolveSection(graph, ownerId, semester, section,
                resolvedSectionName);

        SubjectEntity codeClash = graph.findSubjectByCodeInSection(ownerId, targetSection.id, normalizedCode);
        if (codeClash != null && !codeClash.id.equals(subject.id)) {
            throw new LocalDataException("A course with this code already exists in this class.");
        }

        department.name = trimmedDepartmentName;
        department.shift = resolvedShift;
        graph.updateDepartment(department);

        semester.semesterNumber = semesterNumber;
        semester.name = "Semester " + semesterNumber;
        graph.updateSemester(semester);

        subject.sectionId = targetSection.id;
        subject.name = courseName.trim();
        subject.code = normalizedCode;
        graph.updateSubject(subject);

        return new Result(department, semester, targetSection, subject);
    }

    private static SectionEntity resolveSection(Graph graph, String ownerId, SemesterEntity semester,
                                                SectionEntity current, String resolvedSectionName) {
        SectionEntity byName = graph.findSectionByName(ownerId, semester.id, resolvedSectionName);
        if (byName != null) {
            return byName;
        }
        int users = graph.countSubjectsInSection(ownerId, current.id);
        if (users <= 1) {
            current.name = resolvedSectionName;
            graph.updateSection(current);
            return current;
        }
        // Shared section row (common for blank names created by Add). Split this
        // subject onto its own section so sibling classes keep the old name.
        SectionEntity created = new SectionEntity();
        created.id = "sec_" + UUID.randomUUID();
        created.ownerUserId = ownerId;
        created.semesterId = semester.id;
        created.name = resolvedSectionName;
        graph.insertSection(created);
        return created;
    }

    static final class Result {
        final DepartmentEntity department;
        final SemesterEntity semester;
        final SectionEntity section;
        final SubjectEntity subject;

        Result(DepartmentEntity department, SemesterEntity semester, SectionEntity section,
               SubjectEntity subject) {
            this.department = department;
            this.semester = semester;
            this.section = section;
            this.subject = subject;
        }
    }

    interface Graph {
        SubjectEntity requireSubject(String ownerId, String subjectId);

        DepartmentEntity requireDepartment(String ownerId, String departmentId);

        SemesterEntity requireSemester(String ownerId, String semesterId);

        SectionEntity requireSection(String ownerId, String sectionId);

        SemesterEntity findSemesterByNumber(String ownerId, String departmentId, int number);

        SectionEntity findSectionByName(String ownerId, String semesterId, String name);

        SubjectEntity findSubjectByCodeInSection(String ownerId, String sectionId, String code);

        int countSubjectsInSection(String ownerId, String sectionId);

        void insertSection(SectionEntity section);

        void updateDepartment(DepartmentEntity department);

        void updateSemester(SemesterEntity semester);

        void updateSection(SectionEntity section);

        void updateSubject(SubjectEntity subject);
    }

    private static final class RoomGraph implements Graph {
        private final AppDatabase db;

        RoomGraph(AppDatabase db) {
            this.db = db;
        }

        @Override
        public SubjectEntity requireSubject(String ownerId, String subjectId) {
            SubjectEntity entity = db.subjectDao().findOwned(subjectId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Subject not found.");
            }
            return entity;
        }

        @Override
        public DepartmentEntity requireDepartment(String ownerId, String departmentId) {
            DepartmentEntity entity = db.departmentDao().findOwned(departmentId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Department not found.");
            }
            return entity;
        }

        @Override
        public SemesterEntity requireSemester(String ownerId, String semesterId) {
            SemesterEntity entity = db.semesterDao().findOwned(semesterId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Semester not found.");
            }
            return entity;
        }

        @Override
        public SectionEntity requireSection(String ownerId, String sectionId) {
            SectionEntity entity = db.sectionDao().findOwned(sectionId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Section not found.");
            }
            return entity;
        }

        @Override
        public SemesterEntity findSemesterByNumber(String ownerId, String departmentId, int number) {
            return db.semesterDao().findByNumber(ownerId, departmentId, number);
        }

        @Override
        public SectionEntity findSectionByName(String ownerId, String semesterId, String name) {
            return db.sectionDao().findByName(ownerId, semesterId, name);
        }

        @Override
        public SubjectEntity findSubjectByCodeInSection(String ownerId, String sectionId, String code) {
            return db.subjectDao().findByCodeInSection(ownerId, sectionId, code);
        }

        @Override
        public int countSubjectsInSection(String ownerId, String sectionId) {
            return db.subjectDao().countInSection(ownerId, sectionId);
        }

        @Override
        public void insertSection(SectionEntity section) {
            db.sectionDao().insert(section);
        }

        @Override
        public void updateDepartment(DepartmentEntity department) {
            db.departmentDao().update(department);
        }

        @Override
        public void updateSemester(SemesterEntity semester) {
            db.semesterDao().update(semester);
        }

        @Override
        public void updateSection(SectionEntity section) {
            db.sectionDao().update(section);
        }

        @Override
        public void updateSubject(SubjectEntity subject) {
            db.subjectDao().update(subject);
        }
    }

    /** In-memory graph used by unit tests. Not a second database. */
    static final class MemoryGraph implements Graph {
        final List<DepartmentEntity> departments = new ArrayList<>();
        final List<SemesterEntity> semesters = new ArrayList<>();
        final List<SectionEntity> sections = new ArrayList<>();
        final List<SubjectEntity> subjects = new ArrayList<>();
        final List<StudentEntity> students = new ArrayList<>();
        final List<AttendanceSessionEntity> sessions = new ArrayList<>();

        @Override
        public SubjectEntity requireSubject(String ownerId, String subjectId) {
            for (SubjectEntity subject : subjects) {
                if (subjectId.equals(subject.id) && ownerId.equals(subject.ownerUserId)) {
                    return subject;
                }
            }
            throw new LocalDataException("Subject not found.");
        }

        @Override
        public DepartmentEntity requireDepartment(String ownerId, String departmentId) {
            for (DepartmentEntity department : departments) {
                if (departmentId.equals(department.id) && ownerId.equals(department.ownerUserId)) {
                    return department;
                }
            }
            throw new LocalDataException("Department not found.");
        }

        @Override
        public SemesterEntity requireSemester(String ownerId, String semesterId) {
            for (SemesterEntity semester : semesters) {
                if (semesterId.equals(semester.id) && ownerId.equals(semester.ownerUserId)) {
                    return semester;
                }
            }
            throw new LocalDataException("Semester not found.");
        }

        @Override
        public SectionEntity requireSection(String ownerId, String sectionId) {
            for (SectionEntity section : sections) {
                if (sectionId.equals(section.id) && ownerId.equals(section.ownerUserId)) {
                    return section;
                }
            }
            throw new LocalDataException("Section not found.");
        }

        @Override
        public SemesterEntity findSemesterByNumber(String ownerId, String departmentId, int number) {
            for (SemesterEntity semester : semesters) {
                if (ownerId.equals(semester.ownerUserId)
                        && departmentId.equals(semester.departmentId)
                        && semester.semesterNumber == number) {
                    return semester;
                }
            }
            return null;
        }

        @Override
        public SectionEntity findSectionByName(String ownerId, String semesterId, String name) {
            String resolved = name == null ? "" : name;
            for (SectionEntity section : sections) {
                if (ownerId.equals(section.ownerUserId)
                        && semesterId.equals(section.semesterId)
                        && resolved.equals(section.name == null ? "" : section.name)) {
                    return section;
                }
            }
            return null;
        }

        @Override
        public SubjectEntity findSubjectByCodeInSection(String ownerId, String sectionId, String code) {
            for (SubjectEntity subject : subjects) {
                if (ownerId.equals(subject.ownerUserId)
                        && sectionId.equals(subject.sectionId)
                        && code.equals(subject.code)) {
                    return subject;
                }
            }
            return null;
        }

        @Override
        public int countSubjectsInSection(String ownerId, String sectionId) {
            int count = 0;
            for (SubjectEntity subject : subjects) {
                if (ownerId.equals(subject.ownerUserId) && sectionId.equals(subject.sectionId)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public void insertSection(SectionEntity section) {
            sections.add(section);
        }

        @Override
        public void updateDepartment(DepartmentEntity department) {
            replace(departments, department);
        }

        @Override
        public void updateSemester(SemesterEntity semester) {
            replace(semesters, semester);
        }

        @Override
        public void updateSection(SectionEntity section) {
            replace(sections, section);
        }

        @Override
        public void updateSubject(SubjectEntity subject) {
            replace(subjects, subject);
        }

        private static <T> void replace(List<T> items, T updated) {
            int index = items.indexOf(updated);
            if (index >= 0) {
                items.set(index, updated);
            } else {
                items.add(updated);
            }
        }
    }
}
