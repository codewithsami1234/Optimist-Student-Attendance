package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.studentattendance.data.local.AcademicSetupUpdate;
import com.example.studentattendance.data.local.AcademicStructureImport;
import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.ClassSetupCascadeDelete;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.LectureEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Lecture;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.utils.AcademicExcelParser;
import com.example.studentattendance.utils.PreferenceManager;

public class RoomDepartmentRepository implements DepartmentRepository {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomDepartmentRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void getDepartments(ApiCallback<List<Department>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<Department> result = new ArrayList<>();
            for (DepartmentEntity entity : db.departmentDao().listActive(ownerId)) {
                result.add(entity.toModel(db.studentDao().countInDepartment(ownerId, entity.id)));
            }
            return result;
        }, callback);
    }

    @Override
    public void getClassSetups(ApiCallback<List<ClassSetup>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            return db.runInTransaction((java.util.concurrent.Callable<List<ClassSetup>>) () -> {
                List<ClassSetup> result = new ArrayList<>();
                for (SubjectEntity subject : db.subjectDao().listActive(ownerId)) {
                    DepartmentEntity department = db.departmentDao().findOwned(subject.departmentId, ownerId);
                    if (department == null || department.archived) {
                        continue;
                    }
                    ensureSubjectLinked(ownerId, subject, department);
                    SemesterEntity semester = db.semesterDao().findOwned(subject.semesterId, ownerId);
                    SectionEntity section = db.sectionDao().findOwned(subject.sectionId, ownerId);
                    if (semester == null || section == null) {
                        continue;
                    }
                    int count = db.studentDao().countInSection(ownerId, section.id);
                    result.add(new ClassSetup(
                            department.toModel(count),
                            semester.toModel(),
                            section.toModel(count),
                            subject.toModel()));
                }
                result.sort((left, right) -> left.matchesQuery("").compareToIgnoreCase(right.matchesQuery("")));
                return result;
            });
        }, callback);
    }

    @Override
    public void getSemesters(String departmentId, ApiCallback<List<Semester>> callback) {
        LocalExec.io(() -> {
            if (departmentId == null) {
                throw new LocalDataException("Please select a department first.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireDepartment(ownerId, departmentId);
            List<Semester> result = new ArrayList<>();
            for (SemesterEntity entity : db.semesterDao().listByDepartment(ownerId, departmentId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void getSections(String semesterId, ApiCallback<List<Section>> callback) {
        LocalExec.io(() -> {
            if (semesterId == null) {
                throw new LocalDataException("Please select a semester first.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireSemester(ownerId, semesterId);
            List<Section> result = new ArrayList<>();
            for (SectionEntity entity : db.sectionDao().listBySemester(ownerId, semesterId)) {
                result.add(entity.toModel(db.studentDao().countInSection(ownerId, entity.id)));
            }
            return result;
        }, callback);
    }

    @Override
    public void createDepartment(String name, String code, String headOfDepartment, ApiCallback<Department> callback) {
        LocalExec.io(() -> {
            if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                throw new LocalDataException("Department name and code are required.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            String normalizedCode = code.trim().toUpperCase();
            if (db.departmentDao().findByCode(ownerId, normalizedCode) != null) {
                throw new LocalDataException("A department with this code already exists.");
            }
            DepartmentEntity entity = new DepartmentEntity();
            entity.id = "dept_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.name = name.trim();
            entity.code = normalizedCode;
            entity.headOfDepartment = headOfDepartment;
            entity.shift = com.example.studentattendance.model.DepartmentShift.MORNING;
            db.departmentDao().insert(entity);
            return entity.toModel(0);
        }, callback);
    }

    @Override
    public void createAcademicSetup(String departmentName, String shift, int semesterNumber, String sectionName,
                                    String courseName, String courseCode, ApiCallback<Department> callback) {
        LocalExec.io(() -> {
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
            String ownerId = LocalOwner.requireId(prefs);
            String resolvedSectionName = sectionName == null ? "" : sectionName.trim();
            String resolvedShift = com.example.studentattendance.model.DepartmentShift.normalize(shift);
            return db.runInTransaction((java.util.concurrent.Callable<Department>) () -> {
                DepartmentEntity department = db.departmentDao().findByNameAndShift(
                        ownerId, departmentName.trim(), resolvedShift);
                if (department == null) {
                    department = new DepartmentEntity();
                    department.id = "dept_" + UUID.randomUUID();
                    department.ownerUserId = ownerId;
                    department.name = departmentName.trim();
                    department.shift = resolvedShift;
                    department.code = uniqueDepartmentCode(ownerId, department.name, resolvedShift);
                    db.departmentDao().insert(department);
                } else if (department.archived) {
                    department.archived = false;
                    db.departmentDao().update(department);
                }

                SemesterEntity semester = db.semesterDao().findByNumber(ownerId, department.id, semesterNumber);
                if (semester == null) {
                    semester = new SemesterEntity();
                    semester.id = "sem_" + UUID.randomUUID();
                    semester.ownerUserId = ownerId;
                    semester.departmentId = department.id;
                    semester.semesterNumber = semesterNumber;
                    semester.name = "Semester " + semesterNumber;
                    db.semesterDao().insert(semester);
                }

                SectionEntity section = db.sectionDao().findByName(ownerId, semester.id, resolvedSectionName);
                if (section == null) {
                    section = new SectionEntity();
                    section.id = "sec_" + UUID.randomUUID();
                    section.ownerUserId = ownerId;
                    section.semesterId = semester.id;
                    section.name = resolvedSectionName;
                    db.sectionDao().insert(section);
                }

                String normalizedCode = courseCode.trim().toUpperCase();
                if (db.subjectDao().findByCodeInSection(ownerId, section.id, normalizedCode) != null) {
                    throw new LocalDataException("A course with this code already exists in this class.");
                }

                SubjectEntity subject = new SubjectEntity();
                subject.id = "subj_" + UUID.randomUUID();
                subject.ownerUserId = ownerId;
                subject.departmentId = department.id;
                subject.semesterId = semester.id;
                subject.sectionId = section.id;
                subject.name = courseName.trim();
                subject.code = normalizedCode;
                db.subjectDao().insert(subject);
                return department.toModel(db.studentDao().countInDepartment(ownerId, department.id));
            });
        }, callback);
    }

    @Override
    public void importAcademicStructure(AcademicExcelParser.Result parsed, ApiCallback<ImportResult> callback) {
        LocalExec.io(() -> {
            if (parsed == null) {
                throw new LocalDataException("The Excel file could not be read.");
            }
            if (parsed.fatalError != null && !parsed.fatalError.isEmpty()) {
                throw new LocalDataException(parsed.fatalError);
            }
            String ownerId = LocalOwner.requireId(prefs);
            return AcademicStructureImport.execute(db, ownerId, parsed);
        }, callback);
    }

    @Override
    public void updateAcademicSetup(String subjectId, String departmentName, String shift, int semesterNumber,
                                    String sectionName, String courseName, String courseCode,
                                    ApiCallback<Department> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            return AcademicSetupUpdate.execute(db, ownerId, subjectId, departmentName, shift,
                    semesterNumber, sectionName, courseName, courseCode);
        }, callback);
    }

    private String uniqueDepartmentCode(String ownerId, String name, String shift) {
        StringBuilder letters = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            char ch = Character.toUpperCase(part.charAt(0));
            if (Character.isLetterOrDigit(ch)) {
                letters.append(ch);
            }
        }
        String base = letters.length() >= 2 ? letters.substring(0, Math.min(letters.length(), 5))
                : name.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (base.length() < 2) {
            base = "DEPT";
        }
        if (base.length() > 5) {
            base = base.substring(0, 5);
        }
        String suffix = com.example.studentattendance.model.DepartmentShift.isEvening(shift) ? "E" : "M";
        String candidate = base + suffix;
        if (db.departmentDao().findByCode(ownerId, candidate) == null) {
            return candidate;
        }
        int n = 2;
        while (db.departmentDao().findByCode(ownerId, base + suffix + n) != null) {
            n++;
        }
        return base + suffix + n;
    }

    @Override
    public void createSemester(String departmentId, String name, int semesterNumber, ApiCallback<Semester> callback) {
        LocalExec.io(() -> {
            if (name == null || name.trim().isEmpty()) {
                throw new LocalDataException("Semester name is required.");
            }
            if (semesterNumber < 1 || semesterNumber > 16) {
                throw new LocalDataException("Semester number must be between 1 and 16.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireDepartment(ownerId, departmentId);
            if (db.semesterDao().findByNumber(ownerId, departmentId, semesterNumber) != null) {
                throw new LocalDataException("A semester with this number already exists in this department.");
            }
            SemesterEntity entity = new SemesterEntity();
            entity.id = "sem_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.departmentId = departmentId;
            entity.name = name.trim();
            entity.semesterNumber = semesterNumber;
            db.semesterDao().insert(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void createSection(String semesterId, String name, ApiCallback<Section> callback) {
        LocalExec.io(() -> {
            if (name == null || name.trim().isEmpty()) {
                throw new LocalDataException("Section name is required.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireSemester(ownerId, semesterId);
            if (db.sectionDao().findByName(ownerId, semesterId, name.trim()) != null) {
                throw new LocalDataException("A section with this name already exists in this semester.");
            }
            SectionEntity entity = new SectionEntity();
            entity.id = "sec_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.semesterId = semesterId;
            entity.name = name.trim();
            db.sectionDao().insert(entity);
            return entity.toModel(0);
        }, callback);
    }

    @Override
    public void updateDepartment(String departmentId, String name, String code, String headOfDepartment,
                                  Boolean archived, ApiCallback<Department> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            DepartmentEntity entity = requireDepartment(ownerId, departmentId);
            if (name != null && !name.trim().isEmpty()) {
                entity.name = name.trim();
            }
            if (code != null && !code.trim().isEmpty()) {
                String normalizedCode = code.trim().toUpperCase();
                DepartmentEntity clash = db.departmentDao().findByCode(ownerId, normalizedCode);
                if (clash != null && !clash.id.equals(entity.id)) {
                    throw new LocalDataException("A department with this code already exists.");
                }
                entity.code = normalizedCode;
            }
            if (headOfDepartment != null) {
                entity.headOfDepartment = headOfDepartment;
            }
            if (archived != null) {
                entity.archived = archived;
            }
            db.departmentDao().update(entity);
            return entity.toModel(db.studentDao().countInDepartment(ownerId, entity.id));
        }, callback);
    }

    @Override
    public void deleteDepartment(String departmentId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            requireDepartment(ownerId, departmentId);
            if (db.studentDao().countInDepartment(ownerId, departmentId) > 0) {
                throw new LocalDataException("Remove students from this department before deleting it.");
            }
            db.subjectDao().deleteByDepartment(departmentId, ownerId);
            for (SemesterEntity semester : db.semesterDao().listByDepartment(ownerId, departmentId)) {
                db.sectionDao().deleteBySemester(semester.id, ownerId);
            }
            db.semesterDao().deleteByDepartment(departmentId, ownerId);
            db.departmentDao().delete(departmentId, ownerId);
        }, callback);
    }

    @Override
    public void updateSemester(String semesterId, String name, Integer semesterNumber, Boolean archived,
                                ApiCallback<Semester> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            SemesterEntity entity = requireSemester(ownerId, semesterId);
            if (name != null && !name.trim().isEmpty()) {
                entity.name = name.trim();
            }
            if (semesterNumber != null) {
                if (semesterNumber < 1 || semesterNumber > 16) {
                    throw new LocalDataException("Semester number must be between 1 and 16.");
                }
                SemesterEntity clash = db.semesterDao().findByNumber(ownerId, entity.departmentId, semesterNumber);
                if (clash != null && !clash.id.equals(entity.id)) {
                    throw new LocalDataException("A semester with this number already exists in this department.");
                }
                entity.semesterNumber = semesterNumber;
            }
            if (archived != null) {
                entity.archived = archived;
            }
            db.semesterDao().update(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void deleteSemester(String semesterId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            requireSemester(ownerId, semesterId);
            for (SectionEntity section : db.sectionDao().listBySemester(ownerId, semesterId)) {
                if (db.studentDao().countInSection(ownerId, section.id) > 0) {
                    throw new LocalDataException("Remove students from this semester's sections before deleting it.");
                }
            }
            db.sectionDao().deleteBySemester(semesterId, ownerId);
            db.semesterDao().delete(semesterId, ownerId);
        }, callback);
    }

    @Override
    public void updateSection(String sectionId, String name, Boolean archived, ApiCallback<Section> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            SectionEntity entity = requireSection(ownerId, sectionId);
            if (name != null && !name.trim().isEmpty()) {
                SectionEntity clash = db.sectionDao().findByName(ownerId, entity.semesterId, name.trim());
                if (clash != null && !clash.id.equals(entity.id)) {
                    throw new LocalDataException("A section with this name already exists in this semester.");
                }
                entity.name = name.trim();
            }
            if (archived != null) {
                entity.archived = archived;
            }
            db.sectionDao().update(entity);
            return entity.toModel(db.studentDao().countInSection(ownerId, entity.id));
        }, callback);
    }

    @Override
    public void deleteSection(String sectionId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            requireSection(ownerId, sectionId);
            if (db.studentDao().countInSection(ownerId, sectionId) > 0) {
                throw new LocalDataException("Remove students from this section before deleting it.");
            }
            db.sectionDao().delete(sectionId, ownerId);
        }, callback);
    }

    @Override
    public void getSubjects(String departmentId, ApiCallback<List<Subject>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            requireDepartment(ownerId, departmentId);
            List<Subject> result = new ArrayList<>();
            for (SubjectEntity entity : db.subjectDao().listByDepartment(ownerId, departmentId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void createSubject(String departmentId, String name, String code, ApiCallback<Subject> callback) {
        LocalExec.io(() -> {
            if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                throw new LocalDataException("Subject name and code are required.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            DepartmentEntity department = requireDepartment(ownerId, departmentId);
            String normalizedCode = code.trim().toUpperCase();
            SemesterEntity semester = firstOrCreateSemester(ownerId, department);
            SectionEntity section = firstOrCreateSection(ownerId, semester);
            if (db.subjectDao().findByCodeInSection(ownerId, section.id, normalizedCode) != null) {
                throw new LocalDataException("A subject with this code already exists in this department.");
            }
            SubjectEntity entity = new SubjectEntity();
            entity.id = "subj_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.departmentId = departmentId;
            entity.semesterId = semester.id;
            entity.sectionId = section.id;
            entity.name = name.trim();
            entity.code = normalizedCode;
            db.subjectDao().insert(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void updateSubject(String subjectId, String name, String code, Boolean archived,
                               ApiCallback<Subject> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            SubjectEntity entity = requireSubject(ownerId, subjectId);
            if (name != null && !name.trim().isEmpty()) {
                entity.name = name.trim();
            }
            if (code != null && !code.trim().isEmpty()) {
                String normalizedCode = code.trim().toUpperCase();
                SubjectEntity clash = db.subjectDao().findByCodeInSection(ownerId, entity.sectionId, normalizedCode);
                if (clash != null && !clash.id.equals(entity.id)) {
                    throw new LocalDataException("A subject with this code already exists in this department.");
                }
                entity.code = normalizedCode;
            }
            if (archived != null) {
                entity.archived = archived;
            }
            db.subjectDao().update(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void deleteSubject(String subjectId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            ClassSetupCascadeDelete.execute(db, ownerId, subjectId);
        }, callback);
    }

    @Override
    public void getLectures(String sectionId, String subjectId, ApiCallback<List<Lecture>> callback) {
        LocalExec.io(() -> {
            if (sectionId == null || subjectId == null) {
                throw new LocalDataException("Please add a section and a course first.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireSection(ownerId, sectionId);
            requireSubject(ownerId, subjectId);
            List<Lecture> result = new ArrayList<>();
            for (LectureEntity entity : db.lectureDao().listBySectionAndSubject(ownerId, sectionId, subjectId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void createLecture(String sectionId, String subjectId, int lectureNumber, String title,
                              ApiCallback<Lecture> callback) {
        LocalExec.io(() -> {
            if (lectureNumber < 1 || lectureNumber > 99) {
                throw new LocalDataException("Lecture number must be between 1 and 99.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            requireSection(ownerId, sectionId);
            requireSubject(ownerId, subjectId);
            if (db.lectureDao().findByNumber(ownerId, sectionId, subjectId, lectureNumber) != null) {
                throw new LocalDataException("This lecture already exists for the selected course.");
            }
            LectureEntity entity = new LectureEntity();
            entity.id = "lec_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.sectionId = sectionId;
            entity.subjectId = subjectId;
            entity.lectureNumber = lectureNumber;
            entity.title = title == null ? "" : title.trim();
            db.lectureDao().insert(entity);
            return entity.toModel();
        }, callback);
    }

    @Override
    public void deleteLecture(String lectureId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            LectureEntity entity = db.lectureDao().findOwned(lectureId, ownerId);
            if (entity == null) {
                throw new LocalDataException("Lecture not found.");
            }
            db.lectureDao().delete(lectureId, ownerId);
        }, callback);
    }

    private DepartmentEntity requireDepartment(String ownerId, String departmentId) {
        DepartmentEntity entity = db.departmentDao().findOwned(departmentId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Department not found.");
        }
        return entity;
    }

    private SemesterEntity requireSemester(String ownerId, String semesterId) {
        SemesterEntity entity = db.semesterDao().findOwned(semesterId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Semester not found.");
        }
        return entity;
    }

    private SectionEntity requireSection(String ownerId, String sectionId) {
        SectionEntity entity = db.sectionDao().findOwned(sectionId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Section not found.");
        }
        return entity;
    }

    private SubjectEntity requireSubject(String ownerId, String subjectId) {
        SubjectEntity entity = db.subjectDao().findOwned(subjectId, ownerId);
        if (entity == null) {
            throw new LocalDataException("Subject not found.");
        }
        return entity;
    }

    private void ensureSubjectLinked(String ownerId, SubjectEntity subject, DepartmentEntity department) {
        boolean semesterOk = subject.semesterId != null && !subject.semesterId.isEmpty()
                && db.semesterDao().findOwned(subject.semesterId, ownerId) != null;
        boolean sectionOk = subject.sectionId != null && !subject.sectionId.isEmpty()
                && db.sectionDao().findOwned(subject.sectionId, ownerId) != null;
        if (semesterOk && sectionOk) {
            return;
        }
        SemesterEntity semester = firstOrCreateSemester(ownerId, department);
        SectionEntity section = firstOrCreateSection(ownerId, semester);
        subject.semesterId = semester.id;
        subject.sectionId = section.id;
        db.subjectDao().update(subject);
    }

    private SemesterEntity firstOrCreateSemester(String ownerId, DepartmentEntity department) {
        List<SemesterEntity> existing = db.semesterDao().listByDepartment(ownerId, department.id);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        SemesterEntity semester = new SemesterEntity();
        semester.id = "sem_" + UUID.randomUUID();
        semester.ownerUserId = ownerId;
        semester.departmentId = department.id;
        semester.semesterNumber = 1;
        semester.name = "Semester 1";
        db.semesterDao().insert(semester);
        return semester;
    }

    private SectionEntity firstOrCreateSection(String ownerId, SemesterEntity semester) {
        List<SectionEntity> existing = db.sectionDao().listBySemester(ownerId, semester.id);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        SectionEntity section = new SectionEntity();
        section.id = "sec_" + UUID.randomUUID();
        section.ownerUserId = ownerId;
        section.semesterId = semester.id;
        section.name = "";
        db.sectionDao().insert(section);
        return section;
    }
}
