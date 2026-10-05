package com.example.studentattendance.data.local;

import org.junit.Test;

import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;
import com.example.studentattendance.model.DepartmentShift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class AcademicSetupUpdateTest {

    @Test
    public void editingUpdatesEveryAddFieldInPlace() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Software Engineering", DepartmentShift.EVENING, 3, "Section B",
                "Algorithms", "algo101");

        assertEquals("Software Engineering", result.department.name);
        assertEquals(DepartmentShift.EVENING, result.department.shift);
        assertEquals(3, result.semester.semesterNumber);
        assertEquals("Semester 3", result.semester.name);
        assertEquals("Section B", result.section.name);
        assertEquals("Algorithms", result.subject.name);
        assertEquals("ALGO101", result.subject.code);
    }

    @Test
    public void editingPreservesOriginalIds() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        String departmentId = graph.departments.get(0).id;
        String semesterId = graph.semesters.get(0).id;
        String sectionId = graph.sections.get(0).id;
        String subjectId = graph.subjects.get(0).id;

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Renamed Dept", DepartmentShift.MORNING, 2, "Z", "Renamed Course", "REN101");

        assertEquals(departmentId, result.department.id);
        assertEquals(semesterId, result.semester.id);
        assertEquals(sectionId, result.section.id);
        assertEquals(subjectId, result.subject.id);
        assertSame(graph.departments.get(0), result.department);
        assertSame(graph.subjects.get(0), result.subject);
    }

    @Test
    public void editingDoesNotCreateDuplicateDepartmentOrCourse() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");

        AcademicSetupUpdate.update(graph, "owner", "subj_A",
                "Computer Science", DepartmentShift.MORNING, 1, "A", "Data Structures", "DS101");

        assertEquals(1, graph.departments.size());
        assertEquals(1, graph.semesters.size());
        assertEquals(1, graph.sections.size());
        assertEquals(1, graph.subjects.size());
        assertEquals("dept_A", graph.departments.get(0).id);
        assertEquals("subj_A", graph.subjects.get(0).id);
    }

    @Test
    public void relatedAcademicDataStaysAssociatedAfterEdit() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        StudentEntity student = graph.students.get(0);
        AttendanceSessionEntity session = graph.sessions.get(0);

        AcademicSetupUpdate.update(graph, "owner", "subj_A",
                "Updated CS", DepartmentShift.MORNING, 4, "Lab", "Operating Systems", "OS200");

        assertEquals("dept_A", student.departmentId);
        assertEquals("sem_A", student.semesterId);
        assertEquals("sec_A", student.sectionId);
        assertEquals("dept_A", session.departmentId);
        assertEquals("sem_A", session.semesterId);
        assertEquals("sec_A", session.sectionId);
        assertEquals("subj_A", session.subjectId);
        assertEquals("CS", graph.departments.get(0).code);
        assertEquals("Dr Smith", graph.departments.get(0).headOfDepartment);
    }

    @Test
    public void optionalSectionNameCanBeCleared() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Computer Science", DepartmentShift.MORNING, 1, "  ", "Data Structures", "DS101");

        assertEquals("", result.section.name);
    }

    @Test
    public void duplicateCourseCodeIsRejected() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        SubjectEntity other = new SubjectEntity();
        other.id = "subj_other";
        other.ownerUserId = "owner";
        other.departmentId = "dept_A";
        other.semesterId = "sem_A";
        other.sectionId = "sec_A";
        other.name = "Other";
        other.code = "OTH1";
        graph.subjects.add(other);

        try {
            AcademicSetupUpdate.update(graph, "owner", "subj_A",
                    "Computer Science", DepartmentShift.MORNING, 1, "A", "Data Structures", "OTH1");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("A course with this code already exists in this class.", expected.getMessage());
        }
        assertEquals("DS101", graph.subjects.get(0).code);
        assertEquals(2, graph.subjects.size());
    }

    @Test
    public void duplicateSemesterNumberIsRejected() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        SemesterEntity other = new SemesterEntity();
        other.id = "sem_other";
        other.ownerUserId = "owner";
        other.departmentId = "dept_A";
        other.semesterNumber = 2;
        other.name = "Semester 2";
        graph.semesters.add(other);

        try {
            AcademicSetupUpdate.update(graph, "owner", "subj_A",
                    "Computer Science", DepartmentShift.MORNING, 2, "A", "Data Structures", "DS101");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("A semester with this number already exists in this department.", expected.getMessage());
        }
        assertEquals(1, graph.semesters.get(0).semesterNumber);
    }

    @Test
    public void requiredFieldValidationMatchesAdd() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        try {
            AcademicSetupUpdate.update(graph, "owner", "subj_A", " ", DepartmentShift.MORNING,
                    1, "A", "Course", "C1");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("Department name is required.", expected.getMessage());
        }
        try {
            AcademicSetupUpdate.update(graph, "owner", "subj_A", "CS", DepartmentShift.MORNING,
                    9, "A", "Course", "C1");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("Semester number must be between 1 and 8.", expected.getMessage());
        }
        try {
            AcademicSetupUpdate.update(graph, "owner", "subj_A", "CS", DepartmentShift.MORNING,
                    1, "A", "", "C1");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("Course name and course code are required.", expected.getMessage());
        }
        assertEquals("Computer Science", graph.departments.get(0).name);
    }

    @Test
    public void editingOneClassLeavesAnotherIntact() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        addClass(graph, "B");

        AcademicSetupUpdate.update(graph, "owner", "subj_A",
                "Renamed A", DepartmentShift.EVENING, 3, "XA", "Course A", "CA1");

        assertEquals("Renamed A", graph.departments.get(0).name);
        assertEquals(DepartmentShift.EVENING, graph.departments.get(0).shift);
        assertEquals("Department B", graph.departments.get(1).name);
        assertEquals("MATHB", graph.subjects.get(1).code);
        assertEquals("st_B", graph.students.get(1).id);
        assertEquals("dept_B", graph.students.get(1).departmentId);
        assertEquals("sess_B", graph.sessions.get(1).id);
        assertEquals("subj_B", graph.sessions.get(1).subjectId);
        assertEquals(2, graph.departments.size());
        assertEquals(2, graph.subjects.size());
    }

    @Test
    public void emptySectionCanBeFilledOnEdit() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        graph.sections.get(0).name = "";

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Computer Science", DepartmentShift.MORNING, 1, "A", "Data Structures", "DS101");

        assertEquals("A", result.section.name);
        assertEquals("sec_A", result.section.id);
        assertEquals("sec_A", result.subject.sectionId);
    }

    @Test
    public void emptySectionEditReusesExistingNamedSection() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        graph.sections.get(0).name = "";
        SectionEntity existing = new SectionEntity();
        existing.id = "sec_named";
        existing.ownerUserId = "owner";
        existing.semesterId = "sem_A";
        existing.name = "A";
        graph.sections.add(existing);

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Computer Science", DepartmentShift.MORNING, 1, "A", "Data Structures", "DS101");

        assertEquals("sec_named", result.section.id);
        assertEquals("A", result.section.name);
        assertEquals("sec_named", result.subject.sectionId);
        assertEquals("", graph.sections.get(0).name);
    }

    @Test
    public void emptySharedSectionSplitsWhenRenaming() {
        AcademicSetupUpdate.MemoryGraph graph = seed("A");
        graph.sections.get(0).name = "";
        SubjectEntity sibling = new SubjectEntity();
        sibling.id = "subj_sibling";
        sibling.ownerUserId = "owner";
        sibling.departmentId = "dept_A";
        sibling.semesterId = "sem_A";
        sibling.sectionId = "sec_A";
        sibling.name = "Other";
        sibling.code = "OTH1";
        graph.subjects.add(sibling);

        AcademicSetupUpdate.Result result = AcademicSetupUpdate.update(
                graph, "owner", "subj_A",
                "Computer Science", DepartmentShift.MORNING, 1, "A", "Data Structures", "DS101");

        assertEquals("A", result.section.name);
        assertFalse("sec_A".equals(result.section.id));
        assertEquals("sec_A", sibling.sectionId);
        assertEquals("", graph.sections.get(0).name);
        assertEquals(result.section.id, result.subject.sectionId);
    }

    private static AcademicSetupUpdate.MemoryGraph seed(String key) {
        AcademicSetupUpdate.MemoryGraph graph = new AcademicSetupUpdate.MemoryGraph();
        addClass(graph, key);
        return graph;
    }

    private static void addClass(AcademicSetupUpdate.MemoryGraph graph, String key) {
        DepartmentEntity department = new DepartmentEntity();
        department.id = "dept_" + key;
        department.ownerUserId = "owner";
        department.name = key.equals("A") ? "Computer Science" : "Department " + key;
        department.code = key.equals("A") ? "CS" : "D" + key;
        department.headOfDepartment = key.equals("A") ? "Dr Smith" : null;
        department.shift = DepartmentShift.MORNING;
        graph.departments.add(department);

        SemesterEntity semester = new SemesterEntity();
        semester.id = "sem_" + key;
        semester.ownerUserId = "owner";
        semester.departmentId = department.id;
        semester.semesterNumber = 1;
        semester.name = "Semester 1";
        graph.semesters.add(semester);

        SectionEntity section = new SectionEntity();
        section.id = "sec_" + key;
        section.ownerUserId = "owner";
        section.semesterId = semester.id;
        section.name = "A";
        graph.sections.add(section);

        SubjectEntity subject = new SubjectEntity();
        subject.id = "subj_" + key;
        subject.ownerUserId = "owner";
        subject.departmentId = department.id;
        subject.semesterId = semester.id;
        subject.sectionId = section.id;
        subject.name = key.equals("A") ? "Data Structures" : "Mathematics";
        subject.code = key.equals("A") ? "DS101" : "MATH" + key;
        graph.subjects.add(subject);

        StudentEntity student = new StudentEntity();
        student.id = "st_" + key;
        student.ownerUserId = "owner";
        student.name = "Student " + key;
        student.rollNumber = key + "-1";
        student.departmentId = department.id;
        student.semesterId = semester.id;
        student.sectionId = section.id;
        graph.students.add(student);

        AttendanceSessionEntity session = new AttendanceSessionEntity();
        session.id = "sess_" + key;
        session.ownerUserId = "owner";
        session.departmentId = department.id;
        session.semesterId = semester.id;
        session.sectionId = section.id;
        session.subjectId = subject.id;
        session.date = "2026-09-02";
        graph.sessions.add(session);
    }
}
