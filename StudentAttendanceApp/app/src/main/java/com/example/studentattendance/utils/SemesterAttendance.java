package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;

/**
 * Semester attendance review and spreadsheet rows. Percentages always go
 * through {@link AttendancePolicy}.
 */
public final class SemesterAttendance {

    public static final class Review {
        public String studentId;
        public String studentName;
        public String rollNumber;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int leaveCount;
        public int countableSessions;
        public double attendanceRate;
        public boolean belowThreshold;
    }

    private SemesterAttendance() {
    }

    public static List<Review> reviews(List<AttendanceSession> sessions,
                                       Map<String, Integer> adjustmentDeltas,
                                       double warningThreshold) {
        Map<String, StudentReport> reports = new LinkedHashMap<>();
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (session == null || session.getRecords() == null) {
                    continue;
                }
                for (AttendanceRecord record : session.getRecords()) {
                    if (record == null || record.getStatus() == null
                            || record.getStatus() == AttendanceStatus.NOT_MARKED) {
                        continue;
                    }
                    StudentReport report = reports.computeIfAbsent(record.getStudentId(),
                            id -> new StudentReport(id, record.getStudentName(), record.getRollNumber()));
                    report.increment(record.getStatus());
                }
            }
        }
        double threshold = warningThreshold <= 0 ? AttendancePolicy.DEFAULT_LOW_THRESHOLD : warningThreshold;
        List<Review> result = new ArrayList<>();
        for (StudentReport report : reports.values()) {
            if (adjustmentDeltas != null && report.getStudentId() != null) {
                Integer delta = adjustmentDeltas.get(report.getStudentId());
                if (delta != null) {
                    report.setAdjustmentDelta(delta);
                }
            }
            Review review = new Review();
            review.studentId = report.getStudentId();
            review.studentName = report.getStudentName();
            review.rollNumber = report.getRollNumber();
            review.presentCount = report.getPresentDays();
            review.absentCount = report.getAbsentDays();
            review.lateCount = report.getLateDays();
            review.leaveCount = report.getLeaveDays();
            review.countableSessions = report.getPresentDays() + report.getAbsentDays() + report.getLateDays();
            review.attendanceRate = report.getAttendancePercentage();
            review.belowThreshold = review.countableSessions > 0
                    && Double.compare(review.attendanceRate, threshold) < 0;
            result.add(review);
        }
        result.sort((a, b) -> Double.compare(a.attendanceRate, b.attendanceRate));
        return result;
    }

    public static List<Review> belowThreshold(List<Review> reviews) {
        List<Review> result = new ArrayList<>();
        if (reviews == null) {
            return result;
        }
        for (Review review : reviews) {
            if (review != null && review.belowThreshold) {
                result.add(review);
            }
        }
        return result;
    }

    public static List<String[]> matrixRows(List<AttendanceSession> sessions,
                                            List<Review> reviews) {
        TreeSet<String> dates = new TreeSet<>();
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (session != null && session.getDate() != null && !session.getDate().isEmpty()) {
                    dates.add(session.getDate());
                }
            }
        }
        List<String> dateList = new ArrayList<>(dates);
        List<String[]> rows = new ArrayList<>();
        List<String> header = new ArrayList<>();
        header.add("Student Name");
        header.add("Roll Number");
        header.addAll(dateList);
        header.add("Present");
        header.add("Absent");
        header.add("Late");
        header.add("Leave");
        header.add("Attendance %");
        rows.add(header.toArray(new String[0]));
        if (reviews == null) {
            return rows;
        }
        Map<String, Map<String, String>> statusByStudentDate = new LinkedHashMap<>();
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (session == null || session.getRecords() == null) {
                    continue;
                }
                for (AttendanceRecord record : session.getRecords()) {
                    if (record == null || record.getStudentId() == null) {
                        continue;
                    }
                    Map<String, String> byDate = statusByStudentDate.computeIfAbsent(
                            record.getStudentId(), k -> new LinkedHashMap<>());
                    String status = record.getStatus() == null ? "" : record.getStatus().getLabel();
                    byDate.put(session.getDate(), status);
                }
            }
        }
        for (Review review : reviews) {
            if (review == null) {
                continue;
            }
            List<String> row = new ArrayList<>();
            row.add(review.studentName == null ? "" : review.studentName);
            row.add(review.rollNumber == null ? "" : review.rollNumber);
            Map<String, String> byDate = statusByStudentDate.get(review.studentId);
            for (String date : dateList) {
                String status = byDate == null ? null : byDate.get(date);
                row.add(status == null ? "" : status);
            }
            row.add(String.valueOf(review.presentCount));
            row.add(String.valueOf(review.absentCount));
            row.add(String.valueOf(review.lateCount));
            row.add(String.valueOf(review.leaveCount));
            row.add(String.format(java.util.Locale.US, "%.1f", review.attendanceRate));
            rows.add(row.toArray(new String[0]));
        }
        return rows;
    }

    public static List<Student> studentsFromReviews(List<Review> reviews) {
        List<Student> students = new ArrayList<>();
        if (reviews == null) {
            return students;
        }
        for (Review review : reviews) {
            students.add(new Student(review.studentId, review.rollNumber, review.studentName,
                    "", "", "", "", ""));
        }
        return students;
    }
}
