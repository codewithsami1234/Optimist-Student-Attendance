package com.example.studentattendance.data.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.DepartmentShift;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.utils.AcademicExcelParser;

/**
 * Writes a validated academic workbook into the existing Room tables.
 * One transaction covers the whole file. Attendance rows are never deleted.
 */
public final class AcademicStructureImport {

    private AcademicStructureImport() {
    }

    public static ImportResult execute(AppDatabase db, String ownerId, AcademicExcelParser.Result parsed) {
        ImportResult result = baseResult(parsed);
        if (parsed == null || parsed.rows.isEmpty()) {
            return result;
        }
        try {
            return db.runInTransaction((java.util.concurrent.Callable<ImportResult>) () ->
                    apply(db, ownerId, parsed, result));
        } catch (LocalDataException e) {
            throw e;
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof LocalDataException) {
                throw (LocalDataException) cause;
            }
            String message = cause.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "The Excel file could not be saved.";
            }
            throw new LocalDataException(message);
        }
    }

    private static ImportResult apply(AppDatabase db, String ownerId, AcademicExcelParser.Result parsed,
                                      ImportResult result) {
        Set<String> seenStudents = new HashSet<>();
        Set<String> createdDepartments = new HashSet<>();
        Set<String> reusedDepartments = new HashSet<>();
        Set<String> createdSemesters = new HashSet<>();
        Set<String> reusedSemesters = new HashSet<>();
        Set<String> createdSections = new HashSet<>();
        Set<String> reusedSections = new HashSet<>();
        Set<String> createdCourses = new HashSet<>();
        Set<String> reusedCourses = new HashSet<>();
        int imported = 0;
        int duplicates = 0;
        for (AcademicExcelParser.Row row : parsed.rows) {
            DepartmentEntity department = db.departmentDao().findByNameAndShift(
                    ownerId, row.departmentName, row.shift);
            if (department == null) {
                department = new DepartmentEntity();
                department.id = "dept_" + UUID.randomUUID();
                department.ownerUserId = ownerId;
                department.name = row.departmentName;
                department.shift = row.shift;
                department.code = uniqueDepartmentCode(db, ownerId, row.departmentName, row.shift);
                db.departmentDao().insert(department);
                noteStructure(createdDepartments, reusedDepartments, department.id, true);
            } else {
                noteStructure(createdDepartments, reusedDepartments, department.id, false);
                if (department.archived) {
                    department.archived = false;
                    db.departmentDao().update(department);
                }
            }

            SemesterEntity semester = db.semesterDao().findByNumber(
                    ownerId, department.id, row.semesterNumber);
            if (semester == null) {
                semester = new SemesterEntity();
                semester.id = "sem_" + UUID.randomUUID();
                semester.ownerUserId = ownerId;
                semester.departmentId = department.id;
                semester.semesterNumber = row.semesterNumber;
                semester.name = "Semester " + row.semesterNumber;
                db.semesterDao().insert(semester);
                noteStructure(createdSemesters, reusedSemesters, semester.id, true);
            } else {
                noteStructure(createdSemesters, reusedSemesters, semester.id, false);
                if (semester.archived) {
                    semester.archived = false;
                    db.semesterDao().update(semester);
                }
            }

            SectionEntity section = db.sectionDao().findByNameIgnoreCase(
                    ownerId, semester.id, row.sectionName);
            if (section == null) {
                section = new SectionEntity();
                section.id = "sec_" + UUID.randomUUID();
                section.ownerUserId = ownerId;
                section.semesterId = semester.id;
                section.name = row.sectionName;
                db.sectionDao().insert(section);
                noteStructure(createdSections, reusedSections, section.id, true);
            } else {
                noteStructure(createdSections, reusedSections, section.id, false);
                if (section.archived) {
                    section.archived = false;
                    db.sectionDao().update(section);
                }
            }

            SubjectEntity subject = db.subjectDao().findByCodeInSection(
                    ownerId, section.id, row.courseCode);
            if (subject == null) {
                subject = new SubjectEntity();
                subject.id = "subj_" + UUID.randomUUID();
                subject.ownerUserId = ownerId;
                subject.departmentId = department.id;
                subject.semesterId = semester.id;
                subject.sectionId = section.id;
                subject.name = row.courseName;
                subject.code = row.courseCode;
                db.subjectDao().insert(subject);
                noteStructure(createdCourses, reusedCourses, subject.id, true);
            } else {
                noteStructure(createdCourses, reusedCourses, subject.id, false);
                if (subject.archived) {
                    subject.archived = false;
                    db.subjectDao().update(subject);
                }
            }

            String studentKey = section.id + "\n" + row.rollNumber;
            StudentEntity existing = db.studentDao().findByRollInSection(
                    ownerId, section.id, row.rollNumber);
            if (existing != null || seenStudents.contains(studentKey)) {
                if (existing != null && existing.archived) {
                    existing.archived = false;
                    db.studentDao().update(existing);
                }
                seenStudents.add(studentKey);
                duplicates++;
                continue;
            }
            seenStudents.add(studentKey);
            StudentEntity student = new StudentEntity();
            student.id = "st_" + UUID.randomUUID();
            student.ownerUserId = ownerId;
            student.rollNumber = row.rollNumber;
            student.name = row.studentName;
            student.departmentId = department.id;
            student.semesterId = semester.id;
            student.sectionId = section.id;
            student.email = "";
            student.phone = "";
            db.studentDao().insert(student);
            imported++;
        }
        result.setImported(imported);
        result.setDuplicates(duplicates);
        result.setReused(reusedDepartments.size() + reusedSemesters.size()
                + reusedSections.size() + reusedCourses.size());
        result.setDepartmentsCreated(createdDepartments.size());
        result.setDepartmentsReused(reusedDepartments.size());
        result.setSemestersCreated(createdSemesters.size());
        result.setSemestersReused(reusedSemesters.size());
        result.setSectionsCreated(createdSections.size());
        result.setSectionsReused(reusedSections.size());
        result.setCoursesCreated(createdCourses.size());
        result.setCoursesReused(reusedCourses.size());
        return result;
    }

    private static void noteStructure(Set<String> created, Set<String> reused, String id, boolean inserted) {
        if (inserted) {
            created.add(id);
            return;
        }
        if (!created.contains(id)) {
            reused.add(id);
        }
    }

    private static ImportResult baseResult(AcademicExcelParser.Result parsed) {
        ImportResult result = new ImportResult();
        if (parsed == null) {
            return result;
        }
        result.setTotalRows(parsed.totalRows);
        result.setValidRows(parsed.validRows);
        result.setInvalid(parsed.invalidRows);
        result.setErrors(new ArrayList<>(parsed.errors));
        return result;
    }

    private static String uniqueDepartmentCode(AppDatabase db, String ownerId, String name, String shift) {
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
        String suffix = DepartmentShift.isEvening(shift) ? "E" : "M";
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
}
