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

public class SemesterAttendanceTest {

    @Test
    public void percentageUsesAttendancePolicy() {
        List<AttendanceSession> sessions = sessions(
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                AttendanceStatus.LATE, AttendanceStatus.ABSENT, AttendanceStatus.LEAVE);
        SemesterAttendance.Review review = SemesterAttendance.reviews(sessions, null,
                AttendancePolicy.DEFAULT_LOW_THRESHOLD).get(0);
        assertEquals(75.0, review.attendanceRate, 0.001);
        assertEquals(2, review.presentCount);
        assertEquals(1, review.absentCount);
        assertEquals(1, review.lateCount);
        assertEquals(1, review.leaveCount);
        assertFalse(review.belowThreshold);
    }

    @Test
    public void exactlySeventyFiveIsNotBelow() {
        List<AttendanceSession> sessions = sessions(
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                AttendanceStatus.PRESENT, AttendanceStatus.ABSENT);
        SemesterAttendance.Review review = SemesterAttendance.reviews(sessions, null, 75.0).get(0);
        assertEquals(75.0, review.attendanceRate, 0.001);
        assertFalse(review.belowThreshold);
        assertTrue(SemesterAttendance.belowThreshold(Collections.singletonList(review)).isEmpty());
    }

    @Test
    public void belowSeventyFiveIsFlagged() {
        List<AttendanceSession> sessions = sessions(
                AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.ABSENT);
        SemesterAttendance.Review review = SemesterAttendance.reviews(sessions, null, 75.0).get(0);
        assertTrue(review.attendanceRate < 75.0);
        assertTrue(review.belowThreshold);
        assertEquals(1, SemesterAttendance.belowThreshold(Collections.singletonList(review)).size());
    }

    @Test
    public void aboveSeventyFiveIsClear() {
        List<AttendanceSession> sessions = sessions(
                AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, AttendanceStatus.PRESENT);
        SemesterAttendance.Review review = SemesterAttendance.reviews(sessions, null, 75.0).get(0);
        assertEquals(100.0, review.attendanceRate, 0.001);
        assertFalse(review.belowThreshold);
    }

    @Test
    public void matrixKeepsDatesWithoutInventingMarks() {
        AttendanceSession day1 = session("2026-01-10", AttendanceStatus.PRESENT);
        AttendanceSession day2 = session("2026-01-12", AttendanceStatus.ABSENT);
        List<String[]> rows = SemesterAttendance.matrixRows(java.util.Arrays.asList(day1, day2),
                SemesterAttendance.reviews(java.util.Arrays.asList(day1, day2), null, 75.0));
        assertEquals("Student Name", rows.get(0)[0]);
        assertEquals("2026-01-10", rows.get(0)[2]);
        assertEquals("2026-01-12", rows.get(0)[3]);
        assertEquals("Present", rows.get(1)[2]);
        assertEquals("Absent", rows.get(1)[3]);
    }

    @Test
    public void belowSeventyFiveDialogShowsOnlyBelowStudents() {
        // Mirrors View History → Below 75%: reviews(all) then belowThreshold(reviews).
        List<AttendanceSession> sessions = new ArrayList<>();
        sessions.add(multi("2026-09-01",
                mark("st1", "Low", "IT-001", AttendanceStatus.ABSENT),
                mark("st2", "Exact", "IT-002", AttendanceStatus.PRESENT),
                mark("st3", "High", "IT-003", AttendanceStatus.PRESENT)));
        sessions.add(multi("2026-09-02",
                mark("st1", "Low", "IT-001", AttendanceStatus.ABSENT),
                mark("st2", "Exact", "IT-002", AttendanceStatus.PRESENT),
                mark("st3", "High", "IT-003", AttendanceStatus.PRESENT)));
        sessions.add(multi("2026-09-03",
                mark("st1", "Low", "IT-001", AttendanceStatus.PRESENT),
                mark("st2", "Exact", "IT-002", AttendanceStatus.ABSENT),
                mark("st3", "High", "IT-003", AttendanceStatus.PRESENT)));
        sessions.add(multi("2026-09-04",
                mark("st1", "Low", "IT-001", AttendanceStatus.ABSENT),
                mark("st2", "Exact", "IT-002", AttendanceStatus.PRESENT),
                mark("st3", "High", "IT-003", AttendanceStatus.PRESENT)));
        // Low: 1/4 = 25%, Exact: 3/4 = 75%, High: 4/4 = 100%
        List<SemesterAttendance.Review> reviews = SemesterAttendance.reviews(
                sessions, null, AttendancePolicy.DEFAULT_LOW_THRESHOLD);
        assertEquals(3, reviews.size());
        List<SemesterAttendance.Review> below = SemesterAttendance.belowThreshold(reviews);
        assertEquals(1, below.size());
        assertEquals("Low", below.get(0).studentName);
        assertTrue(Double.compare(below.get(0).attendanceRate, 75.0) < 0);
    }

    @Test
    public void notMarkedDoesNotCountTowardBelowFilter() {
        List<AttendanceSession> sessions = sessions(AttendanceStatus.NOT_MARKED);
        assertTrue(SemesterAttendance.reviews(sessions, null, 75.0).isEmpty());
    }

    private static AttendanceSession multi(String date, AttendanceRecord... marks) {
        AttendanceSession session = new AttendanceSession();
        session.setDate(date);
        session.setDepartmentId("d1");
        session.setSemesterId("s1");
        session.setSectionId("sec1");
        session.setSubjectId("sub1");
        List<AttendanceRecord> records = new ArrayList<>();
        Collections.addAll(records, marks);
        session.setRecords(records);
        return session;
    }

    private static AttendanceRecord mark(String id, String name, String roll, AttendanceStatus status) {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(id);
        record.setStudentName(name);
        record.setRollNumber(roll);
        record.setStatus(status);
        return record;
    }

    private static List<AttendanceSession> sessions(AttendanceStatus... statuses) {
        List<AttendanceSession> list = new ArrayList<>();
        int day = 10;
        for (AttendanceStatus status : statuses) {
            list.add(session("2026-01-" + day, status));
            day++;
        }
        return list;
    }

    private static AttendanceSession session(String date, AttendanceStatus status) {
        AttendanceSession session = new AttendanceSession();
        session.setDate(date);
        session.setDepartmentId("d1");
        session.setSemesterId("s1");
        session.setSectionId("sec1");
        session.setSubjectId("sub1");
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId("st1");
        record.setStudentName("Ayesha");
        record.setRollNumber("CS-001");
        record.setStatus(status);
        List<AttendanceRecord> records = new ArrayList<>();
        records.add(record);
        session.setRecords(records);
        return session;
    }
}
