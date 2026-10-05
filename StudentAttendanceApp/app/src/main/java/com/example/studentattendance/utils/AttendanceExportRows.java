package com.example.studentattendance.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.example.studentattendance.model.AttendanceSession;

/**
 * Builds Excel attendance report rows: class metadata once at the top, then a
 * student table without repeating Department/Semester/Section/Course on every row.
 * Percentages use {@link AttendancePolicy} via {@link SemesterAttendance}.
 */
public final class AttendanceExportRows {

    public static final class ReportMeta {
        public String departmentName;
        public String semesterName;
        public String sectionName;
        public String courseName;
        public String courseCode;
        public String shiftName;
        public String fromDate;
        public String toDate;
        public String generatedAt;
    }

    public static final class ConsolidatedReport {
        public final List<String[]> rows;
        /** 0-based index of the student-table header row. */
        public final int tableHeaderRowIndex;
        public final int studentCount;

        public ConsolidatedReport(List<String[]> rows, int tableHeaderRowIndex, int studentCount) {
            this.rows = rows;
            this.tableHeaderRowIndex = tableHeaderRowIndex;
            this.studentCount = studentCount;
        }
    }

    public static final class SessionDayReport {
        public final List<String[]> rows;
        public final int tableHeaderRowIndex;
        public final int studentCount;

        public SessionDayReport(List<String[]> rows, int tableHeaderRowIndex, int studentCount) {
            this.rows = rows;
            this.tableHeaderRowIndex = tableHeaderRowIndex;
            this.studentCount = studentCount;
        }
    }

    private AttendanceExportRows() {
    }

    /**
     * Multi-session detail export: common class fields once at top; per-row
     * columns are student-specific (and Date when dates vary).
     */
    public static ConsolidatedReport recordsReport(List<AttendanceSession> sessions) {
        List<SemesterAttendance.Review> reviews = SemesterAttendance.reviews(sessions, null,
                AttendancePolicy.DEFAULT_LOW_THRESHOLD);
        java.util.Map<String, SemesterAttendance.Review> byStudent = new java.util.LinkedHashMap<>();
        for (SemesterAttendance.Review review : reviews) {
            if (review != null && review.studentId != null) {
                byStudent.put(review.studentId, review);
            }
        }

        ReportMeta meta = metaFromSessions(sessions);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Attendance Records"});
        rows.add(new String[]{""});
        appendMeta(rows, "Department:", meta.departmentName);
        appendMeta(rows, "Shift:", meta.shiftName);
        appendMeta(rows, "Semester:", meta.semesterName);
        appendMeta(rows, "Section:", HistoryFilter.sectionLabel(meta.sectionName));
        appendMeta(rows, "Course:", meta.courseName);
        appendMeta(rows, "Course Code:", meta.courseCode);
        if (meta.fromDate != null || meta.toDate != null) {
            appendMeta(rows, "Date Range:", formatRange(meta.fromDate, meta.toDate));
        }
        rows.add(new String[]{""});

        boolean multiDate = hasMultipleDates(sessions);
        boolean multiCourse = hasMultipleCourses(sessions);
        int tableHeaderRowIndex = rows.size();
        if (multiDate && multiCourse) {
            rows.add(new String[]{"#", "Student Name", "Roll No", "Course", "Course Code",
                    "Date", "Attendance Status", "Attendance %"});
        } else if (multiDate) {
            rows.add(new String[]{"#", "Student Name", "Roll No", "Date",
                    "Attendance Status", "Attendance %"});
        } else if (multiCourse) {
            rows.add(new String[]{"#", "Student Name", "Roll No", "Course", "Course Code",
                    "Attendance Status", "Attendance %"});
        } else {
            rows.add(new String[]{"#", "Student Name", "Roll No", "Attendance Status", "Attendance %"});
        }

        int serial = 1;
        int studentRows = 0;
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (session == null || session.getRecords() == null) {
                    continue;
                }
                for (com.example.studentattendance.model.AttendanceRecord record : session.getRecords()) {
                    if (record == null) {
                        continue;
                    }
                    SemesterAttendance.Review review = byStudent.get(record.getStudentId());
                    String percent = review == null
                            ? ""
                            : String.format(Locale.US, "%.1f%%", review.attendanceRate);
                    String roll = ExcelIdentifiers.normalizeRollNumber(record.getRollNumber());
                    if (multiDate && multiCourse) {
                        rows.add(new String[]{
                                String.valueOf(serial++),
                                safe(record.getStudentName()),
                                roll,
                                safe(session.getSubjectName()),
                                safe(session.getSubjectCode()),
                                safe(session.getDate()),
                                record.getStatus() == null ? "" : record.getStatus().getLabel(),
                                percent
                        });
                    } else if (multiDate) {
                        rows.add(new String[]{
                                String.valueOf(serial++),
                                safe(record.getStudentName()),
                                roll,
                                safe(session.getDate()),
                                record.getStatus() == null ? "" : record.getStatus().getLabel(),
                                percent
                        });
                    } else if (multiCourse) {
                        rows.add(new String[]{
                                String.valueOf(serial++),
                                safe(record.getStudentName()),
                                roll,
                                safe(session.getSubjectName()),
                                safe(session.getSubjectCode()),
                                record.getStatus() == null ? "" : record.getStatus().getLabel(),
                                percent
                        });
                    } else {
                        rows.add(new String[]{
                                String.valueOf(serial++),
                                safe(record.getStudentName()),
                                roll,
                                record.getStatus() == null ? "" : record.getStatus().getLabel(),
                                percent
                        });
                    }
                    studentRows++;
                }
            }
        }
        return new ConsolidatedReport(rows, tableHeaderRowIndex, studentRows);
    }

    public static List<String[]> records(List<AttendanceSession> sessions) {
        return recordsReport(sessions).rows;
    }

    /**
     * Day/session export: class metadata once, then # / Name / Roll / Status.
     * No percentage column and no cross-date aggregation.
     */
    public static SessionDayReport sessionDayReport(AttendanceSession session) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Daily Attendance"});
        rows.add(new String[]{""});
        if (session != null) {
            appendMeta(rows, "Department:", session.getDepartmentName());
            appendMeta(rows, "Semester:", session.getSemesterName());
            appendMeta(rows, "Section:", HistoryFilter.sectionLabel(session.getSectionName()));
            appendMeta(rows, "Course:", session.getSubjectName());
            appendMeta(rows, "Course Code:", session.getSubjectCode());
            appendMeta(rows, "Date:", displayDate(session.getDate()));
        }
        rows.add(new String[]{""});

        int tableHeaderRowIndex = rows.size();
        rows.add(new String[]{"#", "Student Name", "Roll No", "Attendance Status"});

        int serial = 1;
        int count = 0;
        if (session != null && session.getRecords() != null) {
            String sessionDate = session.getDate() == null ? "" : session.getDate();
            for (com.example.studentattendance.model.AttendanceRecord record : session.getRecords()) {
                if (record == null) {
                    continue;
                }
                if (record.getDate() != null && !record.getDate().isEmpty()
                        && !sessionDate.isEmpty() && !sessionDate.equals(record.getDate())) {
                    continue;
                }
                rows.add(new String[]{
                        String.valueOf(serial++),
                        safe(record.getStudentName()),
                        ExcelIdentifiers.normalizeRollNumber(record.getRollNumber()),
                        record.getStatus() == null ? "" : record.getStatus().getLabel()
                });
                count++;
            }
        }
        return new SessionDayReport(rows, tableHeaderRowIndex, count);
    }

    public static List<String[]> sessionDayRecords(AttendanceSession session) {
        return sessionDayReport(session).rows;
    }

    public static ConsolidatedReport summaryReport(List<AttendanceSession> sessions) {
        ReportMeta meta = metaFromSessions(sessions);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Attendance Summary"});
        rows.add(new String[]{""});
        appendMeta(rows, "Department:", meta.departmentName);
        appendMeta(rows, "Shift:", meta.shiftName);
        appendMeta(rows, "Semester:", meta.semesterName);
        appendMeta(rows, "Section:", HistoryFilter.sectionLabel(meta.sectionName));
        appendMeta(rows, "Course:", meta.courseName);
        appendMeta(rows, "Course Code:", meta.courseCode);
        if (meta.fromDate != null || meta.toDate != null) {
            appendMeta(rows, "Date Range:", formatRange(meta.fromDate, meta.toDate));
        }
        rows.add(new String[]{""});

        int tableHeaderRowIndex = rows.size();
        rows.add(new String[]{
                "#", "Student Name", "Roll No", "Present", "Absent", "Late", "Leave",
                "Eligible Sessions", "Attendance %", "Status"
        });

        List<SemesterAttendance.Review> reviews = SemesterAttendance.reviews(sessions, null,
                AttendancePolicy.DEFAULT_LOW_THRESHOLD);
        reviews.sort((a, b) -> {
            String ra = a == null || a.rollNumber == null ? "" : a.rollNumber;
            String rb = b == null || b.rollNumber == null ? "" : b.rollNumber;
            int cmp = ra.compareToIgnoreCase(rb);
            if (cmp != 0) {
                return cmp;
            }
            String na = a == null || a.studentName == null ? "" : a.studentName;
            String nb = b == null || b.studentName == null ? "" : b.studentName;
            return na.compareToIgnoreCase(nb);
        });

        int serial = 1;
        for (SemesterAttendance.Review review : reviews) {
            if (review == null) {
                continue;
            }
            rows.add(new String[]{
                    String.valueOf(serial++),
                    safe(review.studentName),
                    ExcelIdentifiers.normalizeRollNumber(review.rollNumber),
                    String.valueOf(review.presentCount),
                    String.valueOf(review.absentCount),
                    String.valueOf(review.lateCount),
                    String.valueOf(review.leaveCount),
                    String.valueOf(review.countableSessions),
                    String.format(Locale.US, "%.1f%%", review.attendanceRate),
                    review.belowThreshold ? "Below 75%" : "75% or above"
            });
        }
        return new ConsolidatedReport(rows, tableHeaderRowIndex, Math.max(0, serial - 1));
    }

    public static List<String[]> summary(List<AttendanceSession> sessions) {
        return summaryReport(sessions).rows;
    }

    /**
     * One consolidated report for the selected class and date range. Does not
     * emit day-wise files; each student appears once with period totals.
     */
    public static ConsolidatedReport consolidatedReport(ReportMeta meta,
                                                        List<AttendanceSession> sessions,
                                                        Map<String, Integer> adjustmentDeltas) {
        ReportMeta m = meta == null ? new ReportMeta() : meta;
        String generated = m.generatedAt != null && !m.generatedAt.isEmpty()
                ? m.generatedAt
                : new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Attendance Report"});
        rows.add(new String[]{""});
        appendMeta(rows, "Department:", safe(m.departmentName));
        appendMeta(rows, "Shift:", safe(m.shiftName));
        appendMeta(rows, "Semester:", safe(m.semesterName));
        appendMeta(rows, "Section:", HistoryFilter.sectionLabel(m.sectionName));
        appendMeta(rows, "Course:", safe(m.courseName));
        appendMeta(rows, "Course Code:", safe(m.courseCode));
        appendMeta(rows, "Date Range:", formatRange(m.fromDate, m.toDate));
        appendMeta(rows, "Generated At:", generated);
        rows.add(new String[]{""});

        int tableHeaderRowIndex = rows.size();
        rows.add(new String[]{
                "#",
                "Student Name",
                "Roll No",
                "Total Classes",
                "Present",
                "Late",
                "Absent",
                "Leave",
                "Attendance %"
        });

        List<SemesterAttendance.Review> reviews = SemesterAttendance.reviews(
                sessions, adjustmentDeltas, AttendancePolicy.DEFAULT_LOW_THRESHOLD);
        reviews.sort((a, b) -> {
            String ra = a == null || a.rollNumber == null ? "" : a.rollNumber;
            String rb = b == null || b.rollNumber == null ? "" : b.rollNumber;
            int cmp = ra.compareToIgnoreCase(rb);
            if (cmp != 0) {
                return cmp;
            }
            String na = a == null || a.studentName == null ? "" : a.studentName;
            String nb = b == null || b.studentName == null ? "" : b.studentName;
            return na.compareToIgnoreCase(nb);
        });

        int serial = 1;
        for (SemesterAttendance.Review review : reviews) {
            if (review == null) {
                continue;
            }
            int totalClasses = review.countableSessions;
            String percent = totalClasses <= 0
                    ? "0.00%"
                    : String.format(Locale.US, "%.2f%%", review.attendanceRate);
            rows.add(new String[]{
                    String.valueOf(serial++),
                    safe(review.studentName),
                    ExcelIdentifiers.normalizeRollNumber(review.rollNumber),
                    String.valueOf(totalClasses),
                    String.valueOf(review.presentCount),
                    String.valueOf(review.lateCount),
                    String.valueOf(review.absentCount),
                    String.valueOf(review.leaveCount),
                    percent
            });
        }
        return new ConsolidatedReport(rows, tableHeaderRowIndex, Math.max(0, serial - 1));
    }

    public static String formatRange(String fromDate, String toDate) {
        String from = fromDate == null || fromDate.isEmpty() ? "…" : displayDate(fromDate);
        String to = toDate == null || toDate.isEmpty() ? "…" : displayDate(toDate);
        return from + " to " + to;
    }

    public static String sanitizeFilePart(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "NA";
        }
        String cleaned = value.trim()
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_");
        if (cleaned.length() > 40) {
            cleaned = cleaned.substring(0, 40);
        }
        return cleaned.isEmpty() ? "NA" : cleaned;
    }

    public static String buildProfileExportFileName(String department, String shift, String semester,
                                                    String section, String courseCode) {
        String semesterPart = compactSemester(semester);
        String sectionPart = compactSection(section);
        String codePart = courseCode == null ? "" : courseCode.replaceAll("[^A-Za-z0-9]", "");
        return "Attendance_"
                + sanitizeFilePart(department)
                + "_" + sanitizeFilePart(shift)
                + "_" + sanitizeFilePart(semesterPart)
                + "_" + sanitizeFilePart(sectionPart)
                + "_" + sanitizeFilePart(codePart)
                + ".xlsx";
    }

    public static String buildReportsExportFileName(String department, String shift, String semester,
                                                    String section, String courseCode,
                                                    String fromDate, String toDate) {
        StringBuilder name = new StringBuilder("Attendance_Report");
        appendNamePart(name, department);
        appendNamePart(name, shift);
        appendNamePart(name, compactSemester(semester));
        appendNamePart(name, compactSection(section));
        String code = courseCode == null ? "" : courseCode.replaceAll("[^A-Za-z0-9]", "");
        appendNamePart(name, code);
        if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
            name.append('_').append(sanitizeFilePart(fromDate))
                    .append("_to_").append(sanitizeFilePart(toDate));
        } else {
            name.append('_').append(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        }
        name.append(".xlsx");
        return name.toString();
    }

    private static void appendNamePart(StringBuilder name, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "all".equalsIgnoreCase(trimmed)) {
            return;
        }
        name.append('_').append(sanitizeFilePart(trimmed));
    }

    private static String compactSemester(String semester) {
        if (semester == null || "all".equalsIgnoreCase(semester.trim())) {
            return "";
        }
        String trimmed = semester.trim();
        if (trimmed.toLowerCase(Locale.US).startsWith("semester")) {
            String rest = trimmed.substring("semester".length()).trim();
            if (!rest.isEmpty()) {
                return "Sem" + rest.replaceAll("\\s+", "");
            }
        }
        return trimmed;
    }

    private static String compactSection(String section) {
        if (section == null || "all".equalsIgnoreCase(section.trim())) {
            return "";
        }
        String trimmed = section.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.toLowerCase(Locale.US).startsWith("section")) {
            return trimmed.replaceAll("\\s+", "");
        }
        return "Section" + trimmed.replaceAll("\\s+", "");
    }

    public static String buildReportFileName(String department, String semester, String section,
                                             String courseCode, String fromDate, String toDate) {
        return "Attendance_Report_"
                + sanitizeFilePart(department)
                + "_" + sanitizeFilePart(semester)
                + "_" + sanitizeFilePart(section)
                + "_" + sanitizeFilePart(courseCode)
                + "_" + sanitizeFilePart(fromDate)
                + "_to_" + sanitizeFilePart(toDate)
                + ".xlsx";
    }

    static void appendMeta(List<String[]> rows, String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        rows.add(new String[]{label, value.trim()});
    }

    private static ReportMeta metaFromSessions(List<AttendanceSession> sessions) {
        ReportMeta meta = new ReportMeta();
        if (sessions == null || sessions.isEmpty()) {
            return meta;
        }
        Set<String> depts = new LinkedHashSet<>();
        Set<String> sems = new LinkedHashSet<>();
        Set<String> secs = new LinkedHashSet<>();
        Set<String> courses = new LinkedHashSet<>();
        Set<String> codes = new LinkedHashSet<>();
        String minDate = null;
        String maxDate = null;
        for (AttendanceSession session : sessions) {
            if (session == null) {
                continue;
            }
            if (notBlank(session.getDepartmentName())) {
                depts.add(session.getDepartmentName().trim());
            }
            if (notBlank(session.getSemesterName())) {
                sems.add(session.getSemesterName().trim());
            }
            if (session.getSectionName() != null) {
                secs.add(HistoryFilter.sectionLabel(session.getSectionName()));
            }
            if (notBlank(session.getSubjectName())) {
                courses.add(session.getSubjectName().trim());
            }
            if (notBlank(session.getSubjectCode())) {
                codes.add(session.getSubjectCode().trim());
            }
            String date = session.getDate();
            if (notBlank(date)) {
                if (minDate == null || date.compareTo(minDate) < 0) {
                    minDate = date;
                }
                if (maxDate == null || date.compareTo(maxDate) > 0) {
                    maxDate = date;
                }
            }
        }
        meta.departmentName = joinOrAll(depts);
        meta.semesterName = joinOrAll(sems);
        meta.sectionName = joinOrAll(secs);
        meta.courseName = joinOrAll(courses);
        meta.courseCode = joinOrAll(codes);
        meta.fromDate = minDate;
        meta.toDate = maxDate;
        meta.shiftName = extractShift(meta.departmentName);
        return meta;
    }

    private static String extractShift(String departmentLabel) {
        if (departmentLabel == null) {
            return "";
        }
        String lower = departmentLabel.toLowerCase(Locale.US);
        if (lower.contains("evening")) {
            return "Evening";
        }
        if (lower.contains("morning")) {
            return "Morning";
        }
        return "";
    }

    private static String joinOrAll(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        if (values.size() == 1) {
            return values.iterator().next();
        }
        return "Multiple";
    }

    private static boolean hasMultipleDates(List<AttendanceSession> sessions) {
        Set<String> dates = new LinkedHashSet<>();
        if (sessions == null) {
            return false;
        }
        for (AttendanceSession session : sessions) {
            if (session != null && notBlank(session.getDate())) {
                dates.add(session.getDate());
            }
        }
        return dates.size() > 1;
    }

    private static boolean hasMultipleCourses(List<AttendanceSession> sessions) {
        Set<String> courses = new LinkedHashSet<>();
        if (sessions == null) {
            return false;
        }
        for (AttendanceSession session : sessions) {
            if (session == null) {
                continue;
            }
            String key = safe(session.getSubjectId()) + "|" + safe(session.getSubjectCode());
            if (!key.equals("|")) {
                courses.add(key);
            }
        }
        return courses.size() > 1;
    }

    private static String displayDate(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "";
        }
        try {
            return DateUtils.toDisplayFormat(iso);
        } catch (Exception ignored) {
            return iso;
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
