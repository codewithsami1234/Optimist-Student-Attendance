package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttendanceExportRowsTest {

    @Test
    public void exportContainsSelectedClassOnly() {
        List<AttendanceSession> all = new ArrayList<>();
        all.add(session("d1", "Computer Science", "sem1", "4", "secA", "A",
                "sub1", "Database Systems", "CS-401", "2026-09-01",
                "st1", "Ayesha", "IT-001", AttendanceStatus.PRESENT));
        all.add(session("d2", "Mathematics", "sem2", "2", "secB", "B",
                "sub9", "Calculus", "MATH-201", "2026-09-01",
                "st9", "Sara", "MT-009", AttendanceStatus.ABSENT));
        HistoryFilter filter = new HistoryFilter("d1", null, "secA", null, "CS-401", null, null);
        AttendanceExportRows.ConsolidatedReport report =
                AttendanceExportRows.recordsReport(HistoryFilter.apply(all, filter));
        String joined = String.join("\n", joinRows(report.rows));
        assertTrue(joined.contains("Ayesha"));
        assertTrue(joined.contains("Computer Science"));
        assertFalse(joined.contains("Sara"));
        assertFalse(joined.contains("Mathematics"));
        String[] header = report.rows.get(report.tableHeaderRowIndex);
        assertEquals("#", header[0]);
        assertEquals("Student Name", header[1]);
        assertEquals("Roll No", header[2]);
        // Class fields are metadata, not repeated on each student row.
        for (String col : header) {
            assertFalse("Department".equals(col));
            assertFalse("Semester".equals(col));
        }
    }

    @Test
    public void consolidatedReportUsesPeriodTotalsAndPolicyPercent() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-01",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.PRESENT));
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-02",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.PRESENT));
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-03",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.ABSENT));
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subOs", "Operating Systems", "IT-307", "2026-09-01",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.PRESENT));

        List<AttendanceSession> filtered = new ArrayList<>();
        filtered.add(sessions.get(0));
        filtered.add(sessions.get(1));
        filtered.add(sessions.get(2));

        AttendanceExportRows.ReportMeta meta = new AttendanceExportRows.ReportMeta();
        meta.departmentName = "BS IT";
        meta.semesterName = "6th";
        meta.sectionName = "A";
        meta.courseName = "Database Systems";
        meta.courseCode = "IT-306";
        meta.fromDate = "2026-09-01";
        meta.toDate = "2026-09-10";
        meta.generatedAt = "2026-09-10 12:00";

        AttendanceExportRows.ConsolidatedReport report =
                AttendanceExportRows.consolidatedReport(meta, filtered, null);
        assertEquals(1, report.studentCount);
        assertEquals("Attendance Report", report.rows.get(0)[0]);
        assertTrue(metaLine(report.rows, "Department:").contains("BS IT"));
        String[] header = report.rows.get(report.tableHeaderRowIndex);
        assertEquals("#", header[0]);
        assertEquals("Student Name", header[1]);
        assertEquals("Roll No", header[2]);
        assertEquals("Attendance %", header[8]);
        String[] student = report.rows.get(report.tableHeaderRowIndex + 1);
        assertEquals("1", student[0]);
        assertEquals("Ali Khan", student[1]);
        assertEquals("IT-001", student[2]);
        assertEquals("3", student[3]);
        assertEquals("2", student[4]);
        assertEquals("0", student[5]);
        assertEquals("1", student[6]);
        assertEquals("66.67%", student[8]);
        assertFalse(String.join(" ", student).contains("Operating Systems"));
        assertFalse(String.join(" ", student).contains("6th"));
    }

    @Test
    public void consolidatedReportHandlesZeroCountableAsZeroPercent() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-01",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.LEAVE));
        AttendanceExportRows.ReportMeta meta = baseMeta();
        AttendanceExportRows.ConsolidatedReport report =
                AttendanceExportRows.consolidatedReport(meta, sessions, null);
        assertEquals(1, report.studentCount);
        String[] student = report.rows.get(report.tableHeaderRowIndex + 1);
        assertEquals("0", student[3]);
        assertEquals("0.00%", student[8]);
    }

    @Test
    public void consolidatedReportAppliesAdjustmentsViaPolicy() {
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(session("d1", "BS IT", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-01",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.ABSENT));
        Map<String, Integer> deltas = new HashMap<>();
        deltas.put("st1", 1);
        AttendanceExportRows.ConsolidatedReport report =
                AttendanceExportRows.consolidatedReport(baseMeta(), sessions, deltas);
        String[] student = report.rows.get(report.tableHeaderRowIndex + 1);
        assertEquals("1", student[3]);
        assertTrue(student[8].contains("%"));
    }

    @Test
    public void sessionDayRecordsIncludesOnlyThatSessionAndNoPercentage() {
        AttendanceSession target = session("d1", "BS IT — Morning", "sem6", "6th", "secA", "A",
                "subDb", "Database Systems", "IT-306", "2026-09-10",
                "st1", "Ali Khan", "IT-001", AttendanceStatus.PRESENT);
        AttendanceRecord otherDay = new AttendanceRecord();
        otherDay.setStudentId("st2");
        otherDay.setStudentName("Other Day");
        otherDay.setRollNumber("IT-002");
        otherDay.setDate("2026-09-09");
        otherDay.setStatus(AttendanceStatus.ABSENT);
        target.getRecords().add(otherDay);

        AttendanceExportRows.SessionDayReport report = AttendanceExportRows.sessionDayReport(target);
        assertEquals(1, report.studentCount);
        assertTrue(metaLine(report.rows, "Department:").contains("BS IT"));
        assertTrue(metaLine(report.rows, "Course:").contains("Database"));
        String[] header = report.rows.get(report.tableHeaderRowIndex);
        assertEquals("#", header[0]);
        assertEquals("Student Name", header[1]);
        assertEquals("Roll No", header[2]);
        assertEquals("Attendance Status", header[3]);
        for (String col : header) {
            assertFalse(col != null && col.toLowerCase(java.util.Locale.US).contains("percent"));
            assertFalse("%".equals(col));
            assertFalse("Department".equals(col));
            assertFalse("Date".equals(col));
        }
        String[] student = report.rows.get(report.tableHeaderRowIndex + 1);
        assertEquals("Ali Khan", student[1]);
        assertEquals("IT-001", student[2]);
        assertEquals("Present", student[3]);
        String joined = String.join(" ", student);
        assertFalse(joined.contains("Other Day"));
        assertFalse(joined.contains("%"));
    }

    @Test
    public void excelIdentifiersNormalizeWholeNumberArtifactsOnly() {
        assertEquals("123", ExcelIdentifiers.normalizeRollNumber("123.0"));
        assertEquals("123", ExcelIdentifiers.normalizeRollNumber("123.00"));
        assertEquals("00123", ExcelIdentifiers.normalizeRollNumber("00123"));
        assertEquals("BSIT-001", ExcelIdentifiers.normalizeRollNumber("BSIT-001"));
        assertEquals("123.5", ExcelIdentifiers.normalizeRollNumber("123.5"));
    }

    @Test
    public void invalidDateOrderIsCaughtByStringCompare() {
        assertTrue("2026-09-10".compareTo("2026-09-01") > 0);
        assertFalse("2026-09-01".compareTo("2026-09-10") > 0);
        assertFalse("2026-09-01".compareTo("2026-09-01") > 0);
    }

    private static String metaLine(List<String[]> rows, String label) {
        for (String[] row : rows) {
            if (row.length >= 2 && label.equals(row[0])) {
                return row[1];
            }
        }
        return "";
    }

    private static List<String> joinRows(List<String[]> rows) {
        List<String> out = new ArrayList<>();
        for (String[] row : rows) {
            out.add(String.join(" ", row));
        }
        return out;
    }

    private static AttendanceExportRows.ReportMeta baseMeta() {
        AttendanceExportRows.ReportMeta meta = new AttendanceExportRows.ReportMeta();
        meta.departmentName = "BS IT";
        meta.semesterName = "6th";
        meta.sectionName = "A";
        meta.courseName = "Database Systems";
        meta.courseCode = "IT-306";
        meta.fromDate = "2026-09-01";
        meta.toDate = "2026-09-10";
        meta.generatedAt = "2026-09-10 12:00";
        return meta;
    }

    private static AttendanceSession session(String departmentId, String departmentName,
                                             String semesterId, String semesterName,
                                             String sectionId, String sectionName,
                                             String subjectId, String subjectName, String code,
                                             String date, String studentId, String name,
                                             String roll, AttendanceStatus status) {
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
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(studentId);
        record.setStudentName(name);
        record.setRollNumber(roll);
        record.setStatus(status);
        List<AttendanceRecord> records = new ArrayList<>();
        records.add(record);
        session.setRecords(records);
        return session;
    }
}
