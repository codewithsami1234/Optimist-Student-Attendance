package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.DepartmentShift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HistoryFilterTest {

    @Test
    public void historyCardClassLineKeepsDepartmentSemesterSectionCourseAndCode() {
        AttendanceSession session = session("d1", "sem4", "secA", "sub401", "CS-401", "2026-01-10");
        session.setDepartmentName("BS IT");
        session.setSemesterName("6th");
        session.setSectionName("A");
        session.setSubjectName("Database Systems");
        String line = HistoryFilter.formatClassLine(session);
        assertTrue(line.contains("BS IT"));
        assertTrue(line.contains("6th"));
        assertTrue(line.contains("A"));
        assertTrue(line.contains("Database Systems"));
        assertTrue(line.contains("CS-401"));
    }

    @Test
    public void allFiltersMeanNoRestriction() {
        HistoryFilter filter = new HistoryFilter();
        AttendanceSession session = session("d1", "s1", "sec1", "sub1", "CS-401", "2026-01-10");
        assertTrue(filter.matches(session));
        assertEquals(1, HistoryFilter.apply(Arrays.asList(session), filter).size());
    }

    @Test
    public void departmentFilterUsesIds() {
        HistoryFilter filter = new HistoryFilter("d1", null, null, null, null, null, null);
        assertEquals(1, HistoryFilter.apply(twoDepartments(), filter).size());
        assertEquals("d1", HistoryFilter.apply(twoDepartments(), filter).get(0).getDepartmentId());
    }

    @Test
    public void semesterSectionSubjectAndCourseCodeFilters() {
        List<AttendanceSession> sessions = twoDepartments();
        HistoryFilter semester = new HistoryFilter(null, "sem4", null, null, null, null, null);
        assertEquals(1, HistoryFilter.apply(sessions, semester).size());

        HistoryFilter section = new HistoryFilter(null, null, "secA", null, null, null, null);
        assertEquals(1, HistoryFilter.apply(sessions, section).size());

        HistoryFilter subject = new HistoryFilter(null, null, null, "sub401", null, null, null);
        assertEquals(1, HistoryFilter.apply(sessions, subject).size());

        HistoryFilter code = new HistoryFilter(null, null, null, null, "cs-401", null, null);
        assertEquals(1, HistoryFilter.apply(sessions, code).size());
        assertEquals("CS-401", HistoryFilter.apply(sessions, code).get(0).getSubjectCode());
    }

    @Test
    public void dateRangeIsInclusiveOnBothEnds() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-08-31"));
        sessions.add(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-09-01"));
        sessions.add(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-09-10"));
        sessions.add(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-09-11"));
        HistoryFilter range = new HistoryFilter("d1", "sem4", "secA", "sub401", null,
                "2026-09-01", "2026-09-10");
        List<AttendanceSession> matched = HistoryFilter.apply(sessions, range);
        assertEquals(2, matched.size());
        java.util.Set<String> dates = new java.util.HashSet<>();
        for (AttendanceSession session : matched) {
            dates.add(session.getDate());
        }
        assertTrue(dates.contains("2026-09-01"));
        assertTrue(dates.contains("2026-09-10"));
        assertFalse(dates.contains("2026-08-31"));
        assertFalse(dates.contains("2026-09-11"));
    }

    @Test
    public void andFilterExcludesOtherDepartmentSemesterSectionAndCourse() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-09-05"));
        sessions.add(session("d2", "sem4", "secA", "sub401", "CS-401", "2026-09-05"));
        sessions.add(session("d1", "sem5", "secA", "sub401", "CS-401", "2026-09-05"));
        sessions.add(session("d1", "sem4", "secB", "sub401", "CS-401", "2026-09-05"));
        sessions.add(session("d1", "sem4", "secA", "sub999", "CS-999", "2026-09-05"));
        HistoryFilter filter = new HistoryFilter("d1", "sem4", "secA", "sub401", null,
                "2026-09-01", "2026-09-10");
        List<AttendanceSession> matched = HistoryFilter.apply(sessions, filter);
        assertEquals(1, matched.size());
        assertEquals("d1", matched.get(0).getDepartmentId());
        assertEquals("sem4", matched.get(0).getSemesterId());
        assertEquals("secA", matched.get(0).getSectionId());
        assertEquals("sub401", matched.get(0).getSubjectId());
    }

    @Test
    public void dateRangeFilter() {
        List<AttendanceSession> sessions = twoDepartments();
        HistoryFilter range = new HistoryFilter(null, null, null, null, null, "2026-01-10", "2026-01-10");
        assertEquals(1, HistoryFilter.apply(sessions, range).size());
        HistoryFilter none = new HistoryFilter(null, null, null, null, null, "2026-02-01", "2026-02-28");
        assertTrue(HistoryFilter.apply(sessions, none).isEmpty());
    }

    @Test
    public void multipleFiltersUseAndLogic() {
        HistoryFilter filter = new HistoryFilter("d1", "sem4", "secA", "sub401", "CS-401", "2026-01-10", "2026-01-10");
        List<AttendanceSession> matched = HistoryFilter.apply(twoDepartments(), filter);
        assertEquals(1, matched.size());
        assertEquals("d1", matched.get(0).getDepartmentId());

        HistoryFilter mixed = new HistoryFilter("d1", "sem4", "secA", "sub401", "MATH-201", null, null);
        assertTrue(HistoryFilter.apply(twoDepartments(), mixed).isEmpty());
    }

    @Test
    public void departmentANeverMatchesDepartmentB() {
        HistoryFilter filter = new HistoryFilter("d1", "sem4", "secA", "sub401", null, null, null);
        for (AttendanceSession session : HistoryFilter.apply(twoDepartments(), filter)) {
            assertFalse("d2".equals(session.getDepartmentId()));
            assertFalse("secB".equals(session.getSectionId()));
        }
    }

    @Test
    public void unnamedSectionStillMatchesById() {
        AttendanceSession unnamed = session("d1", "sem4", "secBlank", "sub401", "CS-401", "2026-01-10");
        unnamed.setSectionName("");
        HistoryFilter filter = new HistoryFilter(null, null, "secBlank", null, null, null, null);
        assertTrue(filter.matches(unnamed));
        assertEquals("No section", HistoryFilter.sectionLabel(unnamed.getSectionName()));
    }

    @Test
    public void shiftFilterUsesDepartmentIdsFromClassSetups() {
        List<AttendanceSession> sessions = twoDepartments();
        List<ClassSetup> setups = new ArrayList<>();
        setups.add(setup("d1", DepartmentShift.MORNING));
        setups.add(setup("d2", DepartmentShift.EVENING));

        HistoryFilter morning = new HistoryFilter(null, null, null, null, null, null, null,
                DepartmentShift.MORNING);
        morning.bindShiftDepartments(setups);
        List<AttendanceSession> morningMatched = HistoryFilter.apply(sessions, morning);
        assertEquals(1, morningMatched.size());
        assertEquals("d1", morningMatched.get(0).getDepartmentId());

        HistoryFilter evening = new HistoryFilter(null, null, null, null, null, null, null,
                DepartmentShift.EVENING);
        evening.bindShiftDepartments(setups);
        List<AttendanceSession> eveningMatched = HistoryFilter.apply(sessions, evening);
        assertEquals(1, eveningMatched.size());
        assertEquals("d2", eveningMatched.get(0).getDepartmentId());

        HistoryFilter all = new HistoryFilter();
        all.bindShiftDepartments(setups);
        assertEquals(2, HistoryFilter.apply(sessions, all).size());
    }

    @Test
    public void blankOptionalFiltersDoNotRestrict() {
        HistoryFilter filter = new HistoryFilter("d1", null, null, null, null, "2026-01-10", "2026-01-10");
        assertTrue(filter.matches(session("d1", "sem4", "secA", "sub401", "CS-401", "2026-01-10")));
        assertTrue(filter.matches(session("d1", "sem4", "secBlank", "sub999", "OTH-1", "2026-01-10")));
    }

    private static ClassSetup setup(String departmentId, String shift) {
        Department department = new Department(departmentId, "Dept", "D", 0, null, shift);
        return new ClassSetup(department, null, null, null);
    }

    @Test
    public void statusFiltersUseStoredStatus() {
        List<AttendanceRecord> records = Arrays.asList(
                record("st1", AttendanceStatus.PRESENT),
                record("st2", AttendanceStatus.ABSENT),
                record("st3", AttendanceStatus.LATE),
                record("st4", AttendanceStatus.LEAVE));
        assertEquals(4, HistoryFilter.byStatus(records, null).size());
        assertEquals(1, HistoryFilter.byStatus(records, AttendanceStatus.PRESENT).size());
        assertEquals("st1", HistoryFilter.byStatus(records, AttendanceStatus.PRESENT).get(0).getStudentId());
        assertEquals(1, HistoryFilter.byStatus(records, AttendanceStatus.ABSENT).size());
        assertEquals(1, HistoryFilter.byStatus(records, AttendanceStatus.LATE).size());
        assertEquals(1, HistoryFilter.byStatus(records, AttendanceStatus.LEAVE).size());
    }

    private static List<AttendanceSession> twoDepartments() {
        List<AttendanceSession> sessions = new ArrayList<>();
        AttendanceSession cs = session("d1", "sem4", "secA", "sub401", "CS-401", "2026-01-10");
        cs.setDepartmentName("Computer Science");
        cs.getRecords().add(record("st1", AttendanceStatus.PRESENT));
        AttendanceSession math = session("d2", "sem3", "secB", "sub201", "MATH-201", "2026-01-11");
        math.setDepartmentName("Mathematics");
        math.getRecords().add(record("st9", AttendanceStatus.ABSENT));
        sessions.add(cs);
        sessions.add(math);
        return sessions;
    }

    private static AttendanceSession session(String departmentId, String semesterId, String sectionId,
                                             String subjectId, String code, String date) {
        AttendanceSession session = new AttendanceSession();
        session.setDepartmentId(departmentId);
        session.setSemesterId(semesterId);
        session.setSectionId(sectionId);
        session.setSectionName("A");
        session.setSubjectId(subjectId);
        session.setSubjectName("Course");
        session.setSubjectCode(code);
        session.setDate(date);
        session.setRecords(new ArrayList<>());
        return session;
    }

    private static AttendanceRecord record(String studentId, AttendanceStatus status) {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(studentId);
        record.setStudentName(studentId);
        record.setRollNumber(studentId);
        record.setStatus(status);
        return record;
    }
}
