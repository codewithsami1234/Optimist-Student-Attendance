package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemesterReportTest {

    @Test
    public void aggregatesMultipleMonthsSessionsAndStatuses() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-12",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.LATE),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.LEAVE)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-06-03",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));

        SemesterReport.Snapshot snapshot = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.ALL);

        assertTrue(snapshot.hasAttendance);
        assertEquals(3, snapshot.totalSessions);
        assertEquals(2, snapshot.totalStudents);
        assertEquals(2, snapshot.presentCount);
        assertEquals(2, snapshot.absentCount);
        assertEquals(1, snapshot.lateCount);
        assertEquals(1, snapshot.leaveCount);
        assertEquals("2026-01-05", snapshot.periodFrom);
        assertEquals("2026-06-03", snapshot.periodTo);

        SemesterReport.StudentRow ali = rowNamed(snapshot, "Ali Ahmed");
        assertEquals(2, ali.presentCount);
        assertEquals(1, ali.lateCount);
        assertEquals(0, ali.absentCount);
        assertEquals(0, ali.leaveCount);
        assertEquals(3, ali.recordedSessions);
        assertEquals(3, ali.attendedSessions);
        assertEquals(AttendancePolicy.rate(2, 0, 1, 0, 0), ali.attendanceRate, 0.001);
        assertFalse(ali.belowThreshold);
        assertEquals("Good", ali.statusLabel());
        assertEquals(3, ali.dates.size());

        SemesterReport.StudentRow sara = rowNamed(snapshot, "Sara Khan");
        assertEquals(0, sara.presentCount);
        assertEquals(2, sara.absentCount);
        assertEquals(1, sara.leaveCount);
        assertEquals(AttendancePolicy.rate(0, 2, 0, 1, 0), sara.attendanceRate, 0.001);
        assertTrue(sara.belowThreshold);
        assertEquals("Below 75%", sara.statusLabel());
    }

    @Test
    public void percentageUsesAttendancePolicyThresholds() {
        double threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;

        SemesterReport.StudentRow below = onlyStudent(sessionsFor(
                AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.ABSENT));
        assertTrue(below.attendanceRate < threshold);
        assertTrue(below.belowThreshold);

        SemesterReport.StudentRow exact = onlyStudent(sessionsFor(
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                AttendanceStatus.PRESENT, AttendanceStatus.ABSENT));
        assertEquals(75.0, exact.attendanceRate, 0.001);
        assertEquals(threshold, exact.attendanceRate, 0.001);
        assertFalse(exact.belowThreshold);

        SemesterReport.StudentRow above = onlyStudent(sessionsFor(
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, AttendanceStatus.LATE));
        assertTrue(above.attendanceRate > threshold);
        assertFalse(above.belowThreshold);
    }

    @Test
    public void departmentSemesterSectionCourseAndCodeFiltersUseAndLogic() {
        List<AttendanceSession> sessions = mixedAcademicSessions();

        SemesterReport.Snapshot dept = build(sessions, new HistoryFilter("d1", null, null, null, null, null, null));
        assertEquals(2, dept.totalSessions);
        assertEquals("d1", dept.courses.get(0).departmentId);
        assertFalse(containsDepartment(dept, "d2"));

        SemesterReport.Snapshot semester = build(sessions, new HistoryFilter(null, "sem4", null, null, null, null, null));
        assertEquals(2, semester.totalSessions);

        SemesterReport.Snapshot section = build(sessions, new HistoryFilter(null, null, "secA", null, null, null, null));
        assertEquals(2, section.totalSessions);
        assertEquals("A", section.courses.get(0).sectionName);

        SemesterReport.Snapshot course = build(sessions, new HistoryFilter(null, null, null, "sub401", null, null, null));
        assertEquals(1, course.totalSessions);
        assertEquals("Database Systems", course.courses.get(0).subjectName);

        SemesterReport.Snapshot code = build(sessions, new HistoryFilter(null, null, null, null, "cs-401", null, null));
        assertEquals(1, code.totalSessions);
        assertEquals("CS-401", code.courses.get(0).subjectCode);

        SemesterReport.Snapshot and = build(sessions,
                new HistoryFilter("d1", "sem4", "secA", "sub401", "CS-401", null, null));
        assertEquals(1, and.totalSessions);
        assertEquals(1, and.totalStudents);
        assertEquals("Ali Ahmed", and.visibleRows.get(0).studentName);

        SemesterReport.Snapshot none = build(sessions,
                new HistoryFilter("d1", "sem4", "secA", "sub401", "MATH-201", null, null));
        assertEquals(0, none.totalSessions);
        assertFalse(none.hasAttendance);
        assertTrue(none.hasActiveFilters);
    }

    @Test
    public void allFiltersLeaveEverySession() {
        List<AttendanceSession> sessions = mixedAcademicSessions();
        SemesterReport.Snapshot snapshot = build(sessions, new HistoryFilter());
        assertEquals(sessions.size(), snapshot.totalSessions);
        assertEquals(3, snapshot.courses.size());
    }

    @Test
    public void departmentANeverIncludesDepartmentB() {
        SemesterReport.Snapshot snapshot = build(mixedAcademicSessions(),
                new HistoryFilter("d1", null, null, null, null, null, null));
        for (SemesterReport.StudentRow row : snapshot.exportRows) {
            assertEquals("d1", row.departmentId);
            assertFalse("Mathematics".equals(row.departmentName));
        }
        for (SemesterReport.CourseGroup group : snapshot.courses) {
            assertEquals("d1", group.departmentId);
        }
    }

    @Test
    public void courseANeverIncludesCourseBAndDoesNotMergeStudentRates() {
        List<AttendanceSession> sessions = mixedAcademicSessions();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub402", "Operating Systems", "CS-402", "2026-02-01",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.ABSENT)));

        SemesterReport.Snapshot db = build(sessions, new HistoryFilter(null, null, null, "sub401", null, null, null));
        assertEquals(1, db.courses.size());
        assertEquals("Database Systems", db.courses.get(0).subjectName);
        SemesterReport.StudentRow aliDb = rowNamed(db, "Ali Ahmed");
        assertEquals(1, aliDb.presentCount);
        assertEquals(0, aliDb.absentCount);
        assertEquals(AttendancePolicy.rate(1, 0, 0, 0, 0), aliDb.attendanceRate, 0.001);

        SemesterReport.Snapshot os = build(sessions, new HistoryFilter(null, null, null, "sub402", null, null, null));
        SemesterReport.StudentRow aliOs = rowNamed(os, "Ali Ahmed");
        assertEquals(0, aliOs.presentCount);
        assertEquals(1, aliOs.absentCount);
        assertTrue(aliOs.belowThreshold);

        SemesterReport.Snapshot both = build(sessions, new HistoryFilter("d1", "sem4", "secA", null, null, null, null));
        assertEquals(2, both.courses.size());
        assertEquals(2, countNamed(both, "Ali Ahmed"));
        assertEquals(2, both.totalStudents);
        double dbRate = 0;
        double osRate = 0;
        for (SemesterReport.StudentRow row : both.exportRows) {
            if (!"Ali Ahmed".equals(row.studentName)) {
                continue;
            }
            if ("sub401".equals(row.subjectId)) {
                dbRate = row.attendanceRate;
            } else if ("sub402".equals(row.subjectId)) {
                osRate = row.attendanceRate;
            }
        }
        assertEquals(100.0, dbRate, 0.001);
        assertEquals(0.0, osRate, 0.001);
        assertTrue(dbRate != osRate);
    }

    @Test
    public void missingDateStaysBlankAndIsNotAbsent() {
        AttendanceSession day1 = session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT));
        AttendanceSession day2 = session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-12",
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT));
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(day1);
        sessions.add(day2);

        SemesterReport.Snapshot snapshot = build(sessions, new HistoryFilter());
        SemesterReport.StudentRow ali = rowNamed(snapshot, "Ali Ahmed");
        assertEquals(1, ali.dates.size());
        assertEquals("2026-01-05", ali.dates.get(0).date);
        assertEquals(AttendanceStatus.PRESENT, ali.dates.get(0).status);
        assertEquals(0, ali.absentCount);

        SemesterReport.SheetBuild matrixBuild = SemesterReport.dateMatrixReport(snapshot.courses.get(0));
        List<String[]> matrix = matrixBuild.rows;
        String[] header = matrix.get(matrixBuild.tableHeaderRowIndex);
        assertEquals("Student Name", header[1]);
        assertEquals("2026-01-05", header[3]);
        assertEquals("2026-01-12", header[4]);
        String[] aliRow = findMatrixRow(matrix, "Ali Ahmed");
        assertEquals("Present", aliRow[3]);
        assertEquals("", aliRow[4]);
        String[] saraRow = findMatrixRow(matrix, "Sara Khan");
        assertEquals("", saraRow[3]);
        assertEquals("Absent", saraRow[4]);
    }

    @Test
    public void unnamedSectionIsLabeledNoSection() {
        AttendanceSession session = session("d1", "Computer Science", "sem4", "4", "secBlank", "",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT));
        SemesterReport.Snapshot snapshot = build(Collections.singletonList(session),
                new HistoryFilter(null, null, "secBlank", null, null, null, null));
        assertEquals("No section", snapshot.courses.get(0).sectionName);
    }

    @Test
    public void belowSeventyFiveQuickFilter() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-12",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-19",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-26",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.PRESENT)));

        SemesterReport.Snapshot below = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.BELOW_THRESHOLD);
        assertEquals(1, below.visibleRows.size());
        assertEquals("Sara Khan", below.visibleRows.get(0).studentName);
        assertTrue(below.visibleRows.get(0).attendanceRate < AttendancePolicy.DEFAULT_LOW_THRESHOLD);
        // Summary cards follow the active Below-75% list.
        assertEquals(1, below.totalStudents);
        assertEquals(below.visibleRows.get(0).presentCount, below.presentCount);
        assertEquals(below.visibleRows.get(0).absentCount, below.absentCount);

        SemesterReport.Snapshot above = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.AT_OR_ABOVE);
        assertEquals(1, above.visibleRows.size());
        assertEquals("Ali Ahmed", above.visibleRows.get(0).studentName);

        SemesterReport.Snapshot allGood = SemesterReport.build(
                Collections.singletonList(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                        "sub401", "Database Systems", "CS-401", "2026-01-05",
                        mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT))),
                Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.BELOW_THRESHOLD);
        assertTrue(allGood.statusFilterEmptyBecauseAllAbove);
        assertFalse(allGood.hasVisibleStudents);
    }

    @Test
    public void exactlySeventyFiveGoesToAboveNotBelow() {
        // Present=5, Late=1, Absent=2 → (5+1)/8 = 75.00%
        List<AttendanceSession> sessions = new ArrayList<>();
        AttendanceStatus[] marks = {
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, AttendanceStatus.LATE,
                AttendanceStatus.ABSENT, AttendanceStatus.ABSENT
        };
        for (int i = 0; i < marks.length; i++) {
            sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                    "subDb", "Database", "IT-306",
                    String.format(java.util.Locale.US, "2026-09-%02d", i + 1),
                    mark("st1", "Exact Student", "IT-001", marks[i])));
        }
        SemesterReport.Snapshot below = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.BELOW_THRESHOLD);
        assertTrue(below.visibleRows.isEmpty());

        SemesterReport.Snapshot above = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.AT_OR_ABOVE);
        assertEquals(1, above.visibleRows.size());
        assertEquals(75.0, above.visibleRows.get(0).attendanceRate, 0.001);
        assertFalse(above.visibleRows.get(0).belowThreshold);
    }

    @Test
    public void belowFilterRespectsAcademicScope() {
        List<AttendanceSession> sessions = new ArrayList<>();
        // In-scope student below 75%
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database", "IT-306", "2026-09-01",
                mark("st1", "Low In Scope", "IT-001", AttendanceStatus.ABSENT)));
        // Out-of-scope student also below 75%
        sessions.add(session("d2", "Math", "sem6", "6th", "secA", "A",
                "subDb", "Database", "IT-306", "2026-09-01",
                mark("st9", "Low Other Dept", "MT-009", AttendanceStatus.ABSENT)));
        HistoryFilter scope = new HistoryFilter("d1", "sem6", "secA", "subDb", null,
                "2026-09-01", "2026-09-10");
        SemesterReport.Snapshot below = SemesterReport.build(
                sessions, Collections.emptyMap(), scope,
                "", SemesterReport.SortMode.PERCENTAGE, SemesterReport.StatusFilter.BELOW_THRESHOLD);
        assertEquals(1, below.visibleRows.size());
        assertEquals("Low In Scope", below.visibleRows.get(0).studentName);
    }

    @Test
    public void searchMatchesNameAndRoll() {
        List<AttendanceSession> sessions = Collections.singletonList(
                session("d1", "Computer Science", "sem4", "4", "secA", "A",
                        "sub401", "Database Systems", "CS-401", "2026-01-05",
                        mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                        mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        assertEquals(1, SemesterReport.build(sessions, Collections.emptyMap(), new HistoryFilter(),
                "ali", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.ALL).visibleRows.size());
        assertEquals(1, SemesterReport.build(sessions, Collections.emptyMap(), new HistoryFilter(),
                "CS-022", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.ALL).visibleRows.size());
        assertTrue(SemesterReport.build(sessions, Collections.emptyMap(), new HistoryFilter(),
                "zz", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.ALL).visibleRows.isEmpty());
    }

    @Test
    public void defaultSortIsPercentageAscending() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        SemesterReport.Snapshot snapshot = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "", null, SemesterReport.StatusFilter.ALL);
        assertEquals("Sara Khan", snapshot.visibleRows.get(0).studentName);
        assertEquals("Ali Ahmed", snapshot.visibleRows.get(1).studentName);
    }

    @Test
    public void emptySemesterHasNoFakeZerosThatLookLikeAttendance() {
        SemesterReport.Snapshot snapshot = build(new ArrayList<>(), new HistoryFilter());
        assertFalse(snapshot.hasAttendance);
        assertEquals(0, snapshot.totalSessions);
        assertEquals(0, snapshot.totalStudents);
        assertEquals(0, snapshot.averageRate, 0.001);
        assertTrue(snapshot.courses.isEmpty());
        assertTrue(snapshot.visibleRows.isEmpty());
        List<String[]> summary = SemesterReport.summarySheet(snapshot, new HistoryFilter());
        assertEquals("0", valueOf(summary, "Total Students (unique)"));
        assertEquals("0", valueOf(summary, "Total Sessions"));
    }

    @Test
    public void recordCountsCanExceedUniqueStudentCount() {
        // 2 unique students × 3 sessions: Present/Late are occurrence totals, not student counts.
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-01",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.LATE)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-08",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.LATE),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.PRESENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-15",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.LATE)));

        SemesterReport.Snapshot snapshot = build(sessions, new HistoryFilter());
        assertEquals(2, snapshot.totalStudents);
        assertEquals(3, snapshot.totalSessions);
        assertEquals(3, snapshot.presentCount);
        assertEquals(0, snapshot.absentCount);
        assertEquals(3, snapshot.lateCount);
        assertEquals(0, snapshot.leaveCount);
        assertTrue("Present records can exceed unique students",
                snapshot.presentCount > snapshot.totalStudents);
        assertTrue("Late records can exceed unique students",
                snapshot.lateCount > snapshot.totalStudents);
        assertEquals(2, snapshot.atOrAboveCount);
        assertEquals(0, snapshot.belowCount);
        assertEquals(AttendancePolicy.rate(3, 0, 3, 0, 0), snapshot.averageRate, 0.001);

        List<String[]> summary = SemesterReport.summarySheet(snapshot, new HistoryFilter());
        assertEquals("2", valueOf(summary, "Total Students (unique)"));
        assertEquals("3", valueOf(summary, "Total Sessions"));
        assertEquals("3", valueOf(summary, "Present Records"));
        assertEquals("3", valueOf(summary, "Late Records"));
        assertEquals("2", valueOf(summary, "Students >= 75%"));
        assertEquals("0", valueOf(summary, "Students < 75%"));
    }

    @Test
    public void sectionBCardsDoNotKeepSectionACounts() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secB", "B",
                "sub401b", "Database Systems", "CS-401", "2026-01-05",
                mark("st3", "Omar Ali", "CS-031", AttendanceStatus.LATE),
                mark("st4", "Hina Noor", "CS-032", AttendanceStatus.LEAVE),
                mark("st5", "Zara", "CS-033", AttendanceStatus.PRESENT)));

        SemesterReport.Snapshot sectionA = build(sessions, new HistoryFilter(null, null, "secA", null, null, null, null));
        assertEquals(1, sectionA.totalSessions);
        assertEquals(2, sectionA.totalStudents);
        assertEquals(1, sectionA.presentCount);
        assertEquals(1, sectionA.absentCount);
        assertEquals(0, sectionA.lateCount);
        assertEquals(0, sectionA.leaveCount);

        SemesterReport.Snapshot sectionB = build(sessions, new HistoryFilter(null, null, "secB", null, null, null, null));
        assertEquals(1, sectionB.totalSessions);
        assertEquals(3, sectionB.totalStudents);
        assertEquals(1, sectionB.presentCount);
        assertEquals(0, sectionB.absentCount);
        assertEquals(1, sectionB.lateCount);
        assertEquals(1, sectionB.leaveCount);
        assertEquals(AttendancePolicy.rate(1, 0, 1, 1, 0), sectionB.averageRate, 0.001);

        SemesterReport.Snapshot none = build(sessions,
                new HistoryFilter("d1", "sem4", "secMissing", null, null, null, null));
        assertFalse(none.hasAttendance);
        assertEquals(0, none.presentCount);
        assertEquals(0, none.absentCount);
        assertEquals(0, none.lateCount);
        assertEquals(0, none.leaveCount);
        assertEquals(0, none.totalStudents);
        assertEquals(0.0, none.averageRate, 0.001);
    }

    @Test
    public void dateRangeCardsUseInclusiveStartAndEndDates() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-08-31",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-01",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-10",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.LATE)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-09-11",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.LEAVE)));

        SemesterReport.Snapshot snapshot = build(sessions,
                new HistoryFilter(null, null, null, null, null, "2026-09-01", "2026-09-10"));
        assertEquals(2, snapshot.totalSessions);
        assertEquals(1, snapshot.presentCount);
        assertEquals(0, snapshot.absentCount);
        assertEquals(1, snapshot.lateCount);
        assertEquals(0, snapshot.leaveCount);
        assertEquals("2026-09-01", snapshot.periodFrom);
        assertEquals("2026-09-10", snapshot.periodTo);
        assertEquals(AttendancePolicy.rate(1, 0, 1, 0, 0), snapshot.averageRate, 0.001);
    }

    @Test
    public void exportSheetsUseRealTotalsDatesStatusesAndPercentages() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-06-03",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.LATE)));

        HistoryFilter filter = new HistoryFilter("d1", "sem4", "secA", "sub401", "CS-401", null, null);
        SemesterReport.Snapshot snapshot = build(sessions, filter);

        List<String[]> summary = SemesterReport.summarySheet(snapshot, filter);
        assertEquals("Computer Science", valueOf(summary, "Department"));
        assertEquals("4", valueOf(summary, "Semester"));
        assertEquals("A", valueOf(summary, "Section"));
        assertEquals("Database Systems", valueOf(summary, "Course"));
        assertEquals("CS-401", valueOf(summary, "Course Code"));
        assertEquals("1", valueOf(summary, "Total Students (unique)"));
        assertEquals("2", valueOf(summary, "Total Sessions"));
        assertEquals("100.0%", valueOf(summary, "Average Attendance"));
        assertEquals("1", valueOf(summary, "Students >= 75%"));
        assertEquals("0", valueOf(summary, "Students < 75%"));

        SemesterReport.SheetBuild students = SemesterReport.studentSummaryReport(snapshot);
        String[] student = students.rows.get(students.tableHeaderRowIndex + 1);
        assertEquals("Ali Ahmed", student[1]);
        assertEquals("CS-021", student[2]);
        assertEquals("1", student[3]);
        assertEquals("1", student[4]);
        assertEquals("0", student[5]);
        assertEquals("0", student[6]);
        assertEquals("2", student[7]);
        assertEquals("100.0%", student[8]);
        assertEquals("Good", student[9]);

        SemesterReport.SheetBuild matrix = SemesterReport.dateMatrixReport(snapshot.courses.get(0));
        String[] matrixHeader = matrix.rows.get(matrix.tableHeaderRowIndex);
        assertEquals("2026-01-05", matrixHeader[3]);
        assertEquals("2026-06-03", matrixHeader[4]);
        String[] matrixStudent = matrix.rows.get(matrix.tableHeaderRowIndex + 1);
        assertEquals("Present", matrixStudent[3]);
        assertEquals("Late", matrixStudent[4]);
        assertEquals("100.0%", matrixStudent[5]);
        assertEquals("CS-401", SemesterReport.sheetNameFor(snapshot.courses.get(0), 0));
    }

    @Test
    public void statusAndSearchFiltersLimitExportSheetsAndSummary() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "001", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "00027", AttendanceStatus.ABSENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-12",
                mark("st1", "Ali Ahmed", "001", AttendanceStatus.PRESENT),
                mark("st2", "Sara Khan", "00027", AttendanceStatus.ABSENT)));

        SemesterReport.Snapshot snapshot = SemesterReport.build(
                sessions, Collections.emptyMap(), new HistoryFilter(),
                "Sara", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.BELOW_THRESHOLD);

        assertEquals(1, snapshot.visibleRows.size());
        assertEquals(2, snapshot.exportRows.size());
        assertEquals("Sara Khan", snapshot.visibleRows.get(0).studentName);
        assertEquals(0, snapshot.presentCount);
        assertEquals(2, snapshot.absentCount);
        assertEquals("00027", snapshot.visibleRows.get(0).rollNumber);

        List<String[]> summary = SemesterReport.summarySheet(snapshot, new HistoryFilter());
        assertEquals("Attendance Report", summary.get(0)[0]);
        assertEquals("All", valueOf(summary, "Shift"));
        assertEquals("1", valueOf(summary, "Total Students (unique)"));
        assertEquals("0", valueOf(summary, "Present Records"));
        assertEquals("2", valueOf(summary, "Absent Records"));

        SemesterReport.SheetBuild students = SemesterReport.studentSummaryReport(snapshot);
        assertEquals(1, students.rows.size() - students.tableHeaderRowIndex - 1);
        String[] student = students.rows.get(students.tableHeaderRowIndex + 1);
        assertEquals("Sara Khan", student[1]);
        assertEquals("00027", student[2]);

        SemesterReport.SheetBuild matrix = SemesterReport.dateMatrixReport(snapshot.courses.get(0));
        assertEquals(1, matrix.rows.size() - matrix.tableHeaderRowIndex - 1);
        String[] matrixStudent = matrix.rows.get(matrix.tableHeaderRowIndex + 1);
        assertEquals("Sara Khan", matrixStudent[1]);
        assertEquals("00027", matrixStudent[2]);
    }

    @Test
    public void exportFileNamesStayFilesystemSafeAndKeepContext() {
        assertEquals("Attendance_BS_Information_Technology_Morning_Sem6_SectionA_IT301.xlsx",
                AttendanceExportRows.buildProfileExportFileName(
                        "BS Information Technology", "Morning", "Semester 6", "A", "IT-301"));
        assertEquals(
                "Attendance_Report_BS_Information_Technology_Morning_Sem6_SectionA_IT301_2026-09-01_to_2026-09-23.xlsx",
                AttendanceExportRows.buildReportsExportFileName(
                        "BS Information Technology", "Morning", "Semester 6", "A", "IT-301",
                        "2026-09-01", "2026-09-23"));
        String unfiltered = AttendanceExportRows.buildReportsExportFileName(
                "All", null, "All", "All", "All", null, null);
        assertTrue(unfiltered.startsWith("Attendance_Report_"));
        assertTrue(unfiltered.endsWith(".xlsx"));
        assertFalse(unfiltered.contains("All"));
        assertFalse(unfiltered.contains(":") || unfiltered.contains("*") || unfiltered.contains("?"));
    }

    @Test
    public void courseGroupsStayOrderedByAcademicSetup() {
        SemesterReport.Snapshot snapshot = build(mixedAcademicSessions(), new HistoryFilter());
        assertEquals("Computer Science", snapshot.courses.get(0).departmentName);
        assertEquals("Computer Science", snapshot.courses.get(1).departmentName);
        assertEquals("Mathematics", snapshot.courses.get(2).departmentName);
    }

    @Test
    public void notMarkedRecordsAreIgnored() {
        AttendanceSession session = session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-05",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.NOT_MARKED),
                mark("st2", "Sara Khan", "CS-022", AttendanceStatus.PRESENT));
        SemesterReport.Snapshot snapshot = build(Collections.singletonList(session), new HistoryFilter());
        assertEquals(1, snapshot.totalStudents);
        assertEquals("Sara Khan", snapshot.visibleRows.get(0).studentName);
        assertEquals(0, snapshot.visibleRows.get(0).absentCount);
    }

    private static SemesterReport.Snapshot build(List<AttendanceSession> sessions, HistoryFilter filter) {
        return SemesterReport.build(sessions, Collections.emptyMap(), filter,
                "", SemesterReport.SortMode.NAME, SemesterReport.StatusFilter.ALL);
    }

    private static SemesterReport.StudentRow onlyStudent(List<AttendanceSession> sessions) {
        return build(sessions, new HistoryFilter()).visibleRows.get(0);
    }

    private static List<AttendanceSession> sessionsFor(AttendanceStatus... statuses) {
        List<AttendanceSession> list = new ArrayList<>();
        int day = 10;
        for (AttendanceStatus status : statuses) {
            list.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                    "sub401", "Database Systems", "CS-401",
                    String.format("2026-01-%02d", day++),
                    mark("st1", "Ali Ahmed", "CS-021", status)));
        }
        return list;
    }

    private static List<AttendanceSession> mixedAcademicSessions() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub401", "Database Systems", "CS-401", "2026-01-10",
                mark("st1", "Ali Ahmed", "CS-021", AttendanceStatus.PRESENT)));
        sessions.add(session("d1", "Computer Science", "sem4", "4", "secA", "A",
                "sub402", "Operating Systems", "CS-402", "2026-01-11",
                mark("st3", "Noor Fatima", "CS-030", AttendanceStatus.LATE)));
        sessions.add(session("d2", "Mathematics", "sem3", "3", "secB", "B",
                "sub201", "Calculus", "MATH-201", "2026-01-12",
                mark("st9", "Bilal", "MATH-009", AttendanceStatus.ABSENT)));
        return sessions;
    }

    private static AttendanceSession session(String departmentId, String departmentName,
                                            String semesterId, String semesterName,
                                            String sectionId, String sectionName,
                                            String subjectId, String subjectName, String code,
                                            String date, AttendanceRecord... records) {
        AttendanceSession session = new AttendanceSession();
        session.setDepartmentId(departmentId);
        session.setDepartmentName(departmentName);
        session.setSemesterId(semesterId);
        session.setSemesterName(semesterName);
        session.setSectionId(sectionId);
        session.setSectionName(sectionName);
        session.setSubjectId(subjectId);
        session.setSubjectName(subjectName);
        session.setSubjectCode(code);
        session.setDate(date);
        session.setRecords(new ArrayList<>());
        Collections.addAll(session.getRecords(), records);
        return session;
    }

    private static AttendanceRecord mark(String studentId, String name, String roll, AttendanceStatus status) {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(studentId);
        record.setStudentName(name);
        record.setRollNumber(roll);
        record.setStatus(status);
        return record;
    }

    private static SemesterReport.StudentRow rowNamed(SemesterReport.Snapshot snapshot, String name) {
        for (SemesterReport.StudentRow row : snapshot.visibleRows) {
            if (name.equals(row.studentName)) {
                return row;
            }
        }
        throw new AssertionError("Missing student " + name);
    }

    private static int countNamed(SemesterReport.Snapshot snapshot, String name) {
        int count = 0;
        for (SemesterReport.StudentRow row : snapshot.exportRows) {
            if (name.equals(row.studentName)) {
                count++;
            }
        }
        return count;
    }

    private static boolean containsDepartment(SemesterReport.Snapshot snapshot, String departmentId) {
        for (SemesterReport.CourseGroup group : snapshot.courses) {
            if (departmentId.equals(group.departmentId)) {
                return true;
            }
        }
        return false;
    }

    private static String[] findMatrixRow(List<String[]> matrix, String name) {
        for (int i = 0; i < matrix.size(); i++) {
            String[] row = matrix.get(i);
            if (row.length > 1 && name.equals(row[1])) {
                return row;
            }
            if (row.length > 0 && name.equals(row[0])) {
                return row;
            }
        }
        throw new AssertionError("Missing matrix row " + name);
    }

    private static String valueOf(List<String[]> rows, String field) {
        for (String[] row : rows) {
            if (row.length >= 2 && (field.equals(row[0]) || (field + ":").equals(row[0]))) {
                return row[1];
            }
        }
        throw new AssertionError("Missing field " + field);
    }
}
