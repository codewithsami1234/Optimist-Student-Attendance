package com.example.studentattendance.data.local;

import org.junit.Test;

import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClassSetupCascadeDeleteTest {

    @Test
    public void deletingClassRemovesItsStudentsAndAttendance() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("A", true, true);

        ClassSetupCascadeDelete.delete(graph, "owner", "subj_A");

        assertTrue(graph.subjects.isEmpty());
        assertTrue(graph.students.isEmpty());
        assertTrue(graph.sessions.isEmpty());
        assertTrue(graph.deletedStudentIds.contains("st_A"));
        assertTrue(graph.deletedSessionIds.contains("sess_A"));
        assertFalse(graph.departments.contains("dept_A"));
    }

    @Test
    public void deletingClassALeavesClassBIntact() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("A", true, true);
        addClass(graph, "B", true, true);

        ClassSetupCascadeDelete.delete(graph, "owner", "subj_A");

        assertEquals(1, graph.subjects.size());
        assertEquals("subj_B", graph.subjects.get(0).id);
        assertEquals(1, graph.students.size());
        assertEquals("st_B", graph.students.get(0).id);
        assertEquals(1, graph.sessions.size());
        assertEquals("sess_B", graph.sessions.get(0).id);
        assertTrue(graph.departments.contains("dept_B"));
        assertFalse(graph.departments.contains("dept_A"));
    }

    @Test
    public void deletingEmptyClassDoesNotCrash() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("A", false, false);

        ClassSetupCascadeDelete.delete(graph, "owner", "subj_A");

        assertTrue(graph.subjects.isEmpty());
        assertTrue(graph.students.isEmpty());
        assertTrue(graph.sessions.isEmpty());
        assertFalse(graph.departments.contains("dept_A"));
    }

    @Test
    public void deletingClassWithStudentsAndNoAttendanceSucceeds() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("A", true, false);

        ClassSetupCascadeDelete.delete(graph, "owner", "subj_A");

        assertTrue(graph.subjects.isEmpty());
        assertTrue(graph.students.isEmpty());
        assertTrue(graph.deletedStudentIds.contains("st_A"));
        assertTrue(graph.deletedSessionIds.isEmpty());
    }

    @Test
    public void deletingClassWithStudentsAndAttendanceRemovesDependents() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("A", true, true);

        ClassSetupCascadeDelete.delete(graph, "owner", "subj_A");

        assertTrue(graph.subjects.isEmpty());
        assertTrue(graph.students.isEmpty());
        assertTrue(graph.sessions.isEmpty());
        assertTrue(graph.deletedNotifications.contains("st_A"));
        assertTrue(graph.deletedSessionIds.contains("sess_A"));
    }

    @Test
    public void unknownSubjectDoesNotDeleteOtherClasses() {
        ClassSetupCascadeDelete.MemoryGraph graph = setup("B", true, true);
        try {
            ClassSetupCascadeDelete.delete(graph, "owner", "subj_missing");
            fail();
        } catch (LocalDataException expected) {
            assertEquals("Subject not found.", expected.getMessage());
        }
        assertEquals(1, graph.subjects.size());
        assertEquals(1, graph.students.size());
        assertEquals(1, graph.sessions.size());
    }

    private static ClassSetupCascadeDelete.MemoryGraph setup(String key, boolean withStudent, boolean withAttendance) {
        ClassSetupCascadeDelete.MemoryGraph graph = new ClassSetupCascadeDelete.MemoryGraph();
        addClass(graph, key, withStudent, withAttendance);
        return graph;
    }

    private static void addClass(ClassSetupCascadeDelete.MemoryGraph graph, String key,
                                 boolean withStudent, boolean withAttendance) {
        String departmentId = "dept_" + key;
        String semesterId = "sem_" + key;
        String sectionId = "sec_" + key;
        String subjectId = "subj_" + key;
        graph.departments.add(departmentId);
        graph.semesterToDepartment.put(semesterId, departmentId);
        graph.sectionToSemester.put(sectionId, semesterId);

        SubjectEntity subject = new SubjectEntity();
        subject.id = subjectId;
        subject.ownerUserId = "owner";
        subject.departmentId = departmentId;
        subject.semesterId = semesterId;
        subject.sectionId = sectionId;
        subject.name = "Mathematics";
        subject.code = "MATH" + key;
        graph.subjects.add(subject);

        if (withStudent) {
            StudentEntity student = new StudentEntity();
            student.id = "st_" + key;
            student.ownerUserId = "owner";
            student.name = "Student " + key;
            student.rollNumber = key + "-1";
            student.departmentId = departmentId;
            student.semesterId = semesterId;
            student.sectionId = sectionId;
            graph.students.add(student);
        }
        if (withAttendance) {
            AttendanceSessionEntity session = new AttendanceSessionEntity();
            session.id = "sess_" + key;
            session.ownerUserId = "owner";
            session.departmentId = departmentId;
            session.semesterId = semesterId;
            session.sectionId = sectionId;
            session.subjectId = subjectId;
            session.date = "2026-09-02";
            graph.sessions.add(session);
        }
    }
}
