package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.model.Subject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportAnalyticsTest {

    @Test
    public void departmentFilterNeverMixesStudents() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter("deptA", null, null),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);

        assertEquals(1, snapshot.totalStudents);
        assertEquals("Ali", snapshot.studentReports.get(0).getStudentName());
        assertEquals(1, snapshot.presentCount);
        assertEquals(0, snapshot.absentCount);
        assertFalse(containsStudent(snapshot, "stB"));
    }

    @Test
    public void sessionsForStudentIdsKeepsOnlySessionsTouchingThoseStudents() {
        List<AttendanceSession> all = sessions();
        java.util.Set<String> onlyAli = new java.util.HashSet<>();
        onlyAli.add("stA");
        List<AttendanceSession> filtered = ReportAnalytics.sessionsForStudentIds(all, onlyAli);
        assertFalse(filtered.isEmpty());
        for (AttendanceSession session : filtered) {
            boolean hasAli = false;
            for (AttendanceRecord record : session.getRecords()) {
                if ("stA".equals(record.getStudentId())) {
                    hasAli = true;
                    break;
                }
            }
            assertTrue(hasAli);
        }
        List<DailyTrend> trend = ReportAnalytics.trendForRange(filtered, "2026-09-01", "2026-09-30");
        assertFalse(trend.isEmpty());
    }

    @Test
    public void sectionFilterMatchesOnlyThatSection() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter(null, "secBlank", null),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);

        assertEquals(1, snapshot.totalStudents);
        assertEquals("Noor", snapshot.studentReports.get(0).getStudentName());
        assertEquals(1, snapshot.leaveCount);
    }

    @Test
    public void courseCodeFilterUsesSubjectRelationship() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter(null, null, "cs101"),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);

        assertEquals(1, snapshot.totalStudents);
        assertEquals("Ali", snapshot.studentReports.get(0).getStudentName());
        assertEquals(1, snapshot.presentCount);
        assertFalse(containsStudent(snapshot, "stB"));
    }

    @Test
    public void combinedFiltersUseAndLogic() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter("deptA", "secA", "CS101"),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);

        assertEquals(1, snapshot.totalStudents);
        assertEquals("Ali", snapshot.studentReports.get(0).getStudentName());
        assertEquals(1, snapshot.totalSessions);

        ReportAnalytics.Snapshot none = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter("deptA", "secA", "EE200"),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);
        assertEquals(0, none.totalSessions);
        assertEquals(0, none.totalStudents);
    }

    @Test
    public void totalsUseAttendancePolicy() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter(),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.PERCENTAGE);

        assertEquals(3, snapshot.totalStudents);
        assertEquals(1, snapshot.presentCount);
        assertEquals(1, snapshot.absentCount);
        assertEquals(1, snapshot.lateCount);
        assertEquals(1, snapshot.leaveCount);
        assertEquals(AttendancePolicy.rate(1, 1, 1, 1, 0), snapshot.attendanceRate, 0.001);
        assertEquals(3, snapshot.studentReports.size());
        assertTrue(snapshot.studentReports.get(0).getAttendancePercentage()
                >= snapshot.studentReports.get(2).getAttendancePercentage());
    }

    @Test
    public void weeklyTrendUsesOnlyMatchingDaysWithRealData() {
        List<DailyTrend> weekly = ReportAnalytics.trendForRange(sessions(), "2026-09-01", "2026-09-07");
        assertEquals(2, weekly.size());
        assertEquals("2026-09-02", weekly.get(0).getDate());
        assertEquals(100.0, weekly.get(0).getAttendanceRate(), 0.001);
        assertEquals("2026-09-03", weekly.get(1).getDate());
        assertEquals(AttendancePolicy.rate(0, 1, 1, 0, 0), weekly.get(1).getAttendanceRate(), 0.001);
    }

    @Test
    public void monthlyTrendIncludesLaterMonthDay() {
        List<DailyTrend> monthly = ReportAnalytics.trendForRange(sessions(), "2026-09-01", "2026-09-30");
        assertEquals(3, monthly.size());
        assertEquals("2026-09-10", monthly.get(2).getDate());
        assertEquals(0.0, monthly.get(2).getAttendanceRate(), 0.001);
    }

    @Test
    public void emptyRangeProducesEmptyTrendNotFakeZeros() {
        List<DailyTrend> empty = ReportAnalytics.trendForRange(sessions(), "2026-08-01", "2026-08-07");
        assertTrue(empty.isEmpty());
    }

    @Test
    public void optionalSectionStillReportsWhenUnnamed() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                sessions(),
                students(),
                subjects(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter("deptC", "secBlank", null),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);

        assertEquals(1, snapshot.totalStudents);
        assertEquals("Noor", snapshot.studentReports.get(0).getStudentName());
        assertEquals(1, snapshot.leaveCount);
        assertEquals(0.0, snapshot.attendanceRate, 0.001);
    }

    @Test
    public void todayOverviewUsesAttendancePolicyCounts() {
        ReportAnalytics.TodayOverview overview = ReportAnalytics.todayOverview(8, 1, 1, 2);
        assertTrue(overview.hasData);
        assertEquals(8, overview.presentCount);
        assertEquals(1, overview.absentCount);
        assertEquals(1, overview.lateCount);
        assertEquals(2, overview.leaveCount);
        assertEquals(AttendancePolicy.rate(8, 1, 1, 2, 0), overview.attendanceRate, 0.001);
    }

    @Test
    public void todayOverviewTreatsPresentAndLateAsAttended() {
        ReportAnalytics.TodayOverview overview = ReportAnalytics.todayOverview(3, 1, 1, 0);
        assertEquals(80.0, overview.attendanceRate, 0.001);
    }

    @Test
    public void todayOverviewExcusesLeaveFromPercentage() {
        ReportAnalytics.TodayOverview leaveOnly = ReportAnalytics.todayOverview(0, 0, 0, 4);
        assertTrue(leaveOnly.hasData);
        assertEquals(AttendancePolicy.rate(0, 0, 0, 4, 0), leaveOnly.attendanceRate, 0.001);
        assertEquals(0.0, leaveOnly.attendanceRate, 0.001);
    }

    @Test
    public void emptyTodayOverviewHasNoDataAndNoPercentage() {
        ReportAnalytics.TodayOverview empty = ReportAnalytics.todayOverview(0, 0, 0, 0);
        assertFalse(empty.hasData);
        assertEquals(0.0, empty.attendanceRate, 0.001);
        assertEquals(0, empty.presentCount);
        assertEquals(0, empty.absentCount);
        assertEquals(0, empty.lateCount);
        assertEquals(0, empty.leaveCount);
    }

    @Test
    public void realTrendPointsKeepOnlyDaysWithMarkedRecords() {
        List<DailyTrend> mixed = new ArrayList<>();
        mixed.add(trendPoint("2026-09-01", 0, 0.0));
        mixed.add(trendPoint("2026-09-02", 5, 80.0));
        mixed.add(trendPoint("2026-09-03", 0, 0.0));
        List<DailyTrend> real = ReportAnalytics.realTrendPoints(mixed);
        assertEquals(1, real.size());
        assertEquals("2026-09-02", real.get(0).getDate());
        assertEquals(80.0, real.get(0).getAttendanceRate(), 0.001);
    }

    @Test
    public void realTrendPointsKeepASingleRealDay() {
        List<DailyTrend> one = ReportAnalytics.realTrendPoints(
                Collections.singletonList(trendPoint("2026-09-02", 4, 75.0)));
        assertEquals(1, one.size());
        assertEquals("2026-09-02", one.get(0).getDate());
        assertEquals(4, one.get(0).getTotalMarked());
        assertEquals(75.0, one.get(0).getAttendanceRate(), 0.001);
    }

    @Test
    public void realTrendPointsDoNotInventValuesForNullOrEmpty() {
        assertTrue(ReportAnalytics.realTrendPoints(null).isEmpty());
        assertTrue(ReportAnalytics.realTrendPoints(new ArrayList<>()).isEmpty());
    }

    @Test
    public void emptyInputsProduceZeroTotals() {
        ReportAnalytics.Snapshot snapshot = ReportAnalytics.build(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                new ReportAnalytics.Filter("deptA", "secA", "CS101"),
                "2026-09-01", "2026-09-07",
                "2026-09-01", "2026-09-30",
                "", ReportAnalytics.SortMode.NAME);
        assertEquals(0, snapshot.totalStudents);
        assertEquals(0, snapshot.presentCount);
        assertEquals(0.0, snapshot.attendanceRate, 0.001);
        assertTrue(snapshot.weeklyTrend.isEmpty());
        assertTrue(snapshot.monthlyTrend.isEmpty());
    }

    private static DailyTrend trendPoint(String date, int totalMarked, double rate) {
        DailyTrend point = new DailyTrend();
        point.setDate(date);
        point.setTotalMarked(totalMarked);
        point.setAttendanceRate(rate);
        return point;
    }

    private static boolean containsStudent(ReportAnalytics.Snapshot snapshot, String studentId) {
        for (StudentReport report : snapshot.studentReports) {
            if (studentId.equals(report.getStudentId())) {
                return true;
            }
        }
        return false;
    }

    private static List<AttendanceSession> sessions() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("s1", "deptA", "secA", "CS101", "2026-09-02",
                record("stA", "Ali", "A-1", "deptA", "secA", AttendanceStatus.PRESENT)));
        sessions.add(session("s2", "deptB", "secB", "EE200", "2026-09-03",
                record("stB", "Bilal", "B-1", "deptB", "secB", AttendanceStatus.ABSENT),
                record("stB", "Bilal", "B-1", "deptB", "secB", AttendanceStatus.LATE)));
        sessions.add(session("s3", "deptC", "secBlank", "MATH10", "2026-09-10",
                record("stC", "Noor", "C-1", "deptC", "secBlank", AttendanceStatus.LEAVE)));
        return sessions;
    }

    private static List<Student> students() {
        List<Student> students = new ArrayList<>();
        students.add(new Student("stA", "A-1", "Ali", "deptA", "sem1", "secA", "", ""));
        students.add(new Student("stB", "B-1", "Bilal", "deptB", "sem1", "secB", "", ""));
        students.add(new Student("stC", "C-1", "Noor", "deptC", "sem1", "secBlank", "", ""));
        return students;
    }

    private static List<Subject> subjects() {
        List<Subject> subjects = new ArrayList<>();
        subjects.add(subject("sub1", "deptA", "secA", "CS101"));
        subjects.add(subject("sub2", "deptB", "secB", "EE200"));
        subjects.add(subject("sub3", "deptC", "secBlank", "MATH10"));
        return subjects;
    }

    private static Subject subject(String id, String departmentId, String sectionId, String code) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setDepartmentId(departmentId);
        subject.setSectionId(sectionId);
        subject.setCode(code);
        subject.setName(code);
        return subject;
    }

    private static AttendanceSession session(String id, String departmentId, String sectionId,
                                             String courseCode, String date,
                                             AttendanceRecord... records) {
        AttendanceSession session = new AttendanceSession();
        session.setId(id);
        session.setDepartmentId(departmentId);
        session.setSectionId(sectionId);
        session.setSubjectCode(courseCode);
        session.setDate(date);
        List<AttendanceRecord> list = new ArrayList<>();
        Collections.addAll(list, records);
        session.setRecords(list);
        return session;
    }

    private static AttendanceRecord record(String studentId, String name, String roll,
                                           String departmentId, String sectionId,
                                           AttendanceStatus status) {
        return new AttendanceRecord(studentId, name, roll, departmentId, "sem1", sectionId, "2026-09-01", status);
    }
}
