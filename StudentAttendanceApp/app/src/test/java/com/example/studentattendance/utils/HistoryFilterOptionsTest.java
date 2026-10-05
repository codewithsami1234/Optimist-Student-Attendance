package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HistoryFilterOptionsTest {

    @Test
    public void childOptionsStayInsideSelectedDepartment() {
        HistoryFilterOptions.Snapshot snapshot = HistoryFilterOptions.from(setups(), "deptA", null, null);
        assertEquals(2, snapshot.departments.size());
        assertEquals(1, snapshot.semesters.size());
        assertEquals("semA", snapshot.semesters.get(0).id);
        for (HistoryFilterOptions.Option option : snapshot.subjects) {
            assertTrue(option.id.startsWith("subA"));
        }
        assertEquals("CS-401", snapshot.courseCodes.get(0).id);
    }

    @Test
    public void unnamedSectionIsLabeledNoSection() {
        HistoryFilterOptions.Snapshot snapshot = HistoryFilterOptions.from(setups(), "deptA", "semA", null);
        assertEquals("No section", snapshot.sections.get(0).label);
        assertEquals("secA", snapshot.sections.get(0).id);
    }

    @Test
    public void sectionsAreScopedToSelectedSemester() {
        List<ClassSetup> list = new ArrayList<>();
        list.add(setup("deptA", "CS", "sem4", "4", "secA", "A", "subA", "Algo", "CS-401"));
        list.add(setup("deptA", "CS", "sem5", "5", "secB", "B", "subB", "Networks", "CS-501"));
        HistoryFilterOptions.Snapshot withoutSemester =
                HistoryFilterOptions.from(list, "deptA", null, null);
        assertEquals(2, withoutSemester.sections.size());

        HistoryFilterOptions.Snapshot withSemester =
                HistoryFilterOptions.from(list, "deptA", "sem4", null);
        assertEquals(1, withSemester.sections.size());
        assertEquals("secA", withSemester.sections.get(0).id);
        assertEquals(1, withSemester.subjects.size());
        assertEquals("subA", withSemester.subjects.get(0).id);
    }

    @Test
    public void changingSectionScopesCourses() {
        List<ClassSetup> list = new ArrayList<>();
        list.add(setup("deptA", "CS", "sem4", "4", "secA", "A", "subA", "Algo", "CS-401"));
        list.add(setup("deptA", "CS", "sem4", "4", "secB", "B", "subB", "DB", "CS-402"));
        HistoryFilterOptions.Snapshot forA =
                HistoryFilterOptions.from(list, "deptA", "sem4", "secA");
        assertEquals(1, forA.subjects.size());
        assertEquals("subA", forA.subjects.get(0).id);
        HistoryFilterOptions.Snapshot forB =
                HistoryFilterOptions.from(list, "deptA", "sem4", "secB");
        assertEquals(1, forB.subjects.size());
        assertEquals("subB", forB.subjects.get(0).id);
    }

    private static List<ClassSetup> setups() {
        List<ClassSetup> list = new ArrayList<>();
        list.add(setup("deptA", "Computer Science", "semA", "4", "secA", "", "subA", "Algorithms", "CS-401"));
        list.add(setup("deptB", "Mathematics", "semB", "3", "secB", "B", "subB", "Calculus", "MATH-201"));
        return list;
    }

    private static ClassSetup setup(String deptId, String deptName, String semId, String semName,
                                    String sectionId, String sectionName, String subjectId,
                                    String subjectName, String code) {
        Department department = new Department(deptId, deptName, "", 0, null);
        Semester semester = new Semester(semId, deptId, semName, 4);
        Section section = new Section(sectionId, semId, sectionName, 0);
        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setDepartmentId(deptId);
        subject.setSemesterId(semId);
        subject.setSectionId(sectionId);
        subject.setName(subjectName);
        subject.setCode(code);
        return new ClassSetup(department, semester, section, subject);
    }
}
