package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.StudentReport;

/**
 * Full-semester attendance report. Sessions are grouped by academic course so
 * a student in two courses is never merged into one percentage. Percentages
 * always go through {@link AttendancePolicy}. Missing dates stay blank.
 */
public final class SemesterReport {

    public enum StatusFilter { ALL, BELOW_THRESHOLD, AT_OR_ABOVE }

    public enum SortMode { NAME, ROLL, PERCENTAGE, PRESENT, ABSENT }

    public static final class DateMark {
        public final String date;
        public final AttendanceStatus status;

        public DateMark(String date, AttendanceStatus status) {
            this.date = date;
            this.status = status;
        }
    }

    public static final class StudentRow {
        public String studentId;
        public String studentName;
        public String rollNumber;
        public String departmentId;
        public String departmentName;
        public String semesterId;
        public String semesterName;
        public String sectionId;
        public String sectionName;
        public String subjectId;
        public String subjectName;
        public String subjectCode;
        public int presentCount;
        public int lateCount;
        public int absentCount;
        public int leaveCount;
        public int recordedSessions;
        public int attendedSessions;
        public int countableSessions;
        public int adjustmentDelta;
        public double attendanceRate;
        public boolean belowThreshold;
        public final List<DateMark> dates = new ArrayList<>();

        public String statusLabel() {
            if (countableSessions <= 0) {
                return "";
            }
            return belowThreshold ? "Below 75%" : "Good";
        }

        public StudentReport toStudentReport() {
            StudentReport report = new StudentReport(studentId, studentName, rollNumber);
            for (int i = 0; i < presentCount; i++) {
                report.increment(AttendanceStatus.PRESENT);
            }
            for (int i = 0; i < absentCount; i++) {
                report.increment(AttendanceStatus.ABSENT);
            }
            for (int i = 0; i < lateCount; i++) {
                report.increment(AttendanceStatus.LATE);
            }
            for (int i = 0; i < leaveCount; i++) {
                report.increment(AttendanceStatus.LEAVE);
            }
            report.setAdjustmentDelta(adjustmentDelta);
            return report;
        }
    }

    public static final class CourseGroup {
        public String departmentId;
        public String departmentName;
        public String semesterId;
        public String semesterName;
        public String sectionId;
        public String sectionName;
        public String subjectId;
        public String subjectName;
        public String subjectCode;
        public int sessionCount;
        public int studentCount;
        public int belowCount;
        public int atOrAboveCount;
        public double averageRate;
        public final List<String> sessionDates = new ArrayList<>();
        public final List<StudentRow> allStudents = new ArrayList<>();
        public final List<StudentRow> students = new ArrayList<>();
    }

    public static final class Snapshot {
        public final List<CourseGroup> courses = new ArrayList<>();
        public final List<StudentRow> visibleRows = new ArrayList<>();
        public final List<StudentRow> exportRows = new ArrayList<>();
        public int totalStudents;
        public int totalSessions;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int leaveCount;
        public int belowCount;
        public int atOrAboveCount;
        public double averageRate;
        public StudentRow highest;
        public StudentRow lowest;
        public String periodFrom;
        public String periodTo;
        public boolean hasAttendance;
        public boolean hasVisibleStudents;
        public boolean hasActiveFilters;
        public boolean statusFilterEmptyBecauseAllAbove;
    }

    private SemesterReport() {
    }

    public static Snapshot build(List<AttendanceSession> sessions,
                                 Map<String, Integer> adjustmentDeltas,
                                 HistoryFilter filter,
                                 String searchQuery,
                                 SortMode sortMode,
                                 StatusFilter statusFilter) {
        HistoryFilter safeFilter = filter == null ? new HistoryFilter() : filter;
        List<AttendanceSession> matched = HistoryFilter.apply(sessions, safeFilter);
        Map<String, CourseGroup> groups = new LinkedHashMap<>();
        TreeSet<String> allDates = new TreeSet<>();

        for (AttendanceSession session : matched) {
            if (session == null) {
                continue;
            }
            String groupKey = courseKey(session);
            CourseGroup group = groups.get(groupKey);
            if (group == null) {
                group = new CourseGroup();
                group.departmentId = session.getDepartmentId();
                group.departmentName = session.getDepartmentName();
                group.semesterId = session.getSemesterId();
                group.semesterName = session.getSemesterName();
                group.sectionId = session.getSectionId();
                group.sectionName = HistoryFilter.sectionLabel(session.getSectionName());
                group.subjectId = session.getSubjectId();
                group.subjectName = session.getSubjectName();
                group.subjectCode = session.getSubjectCode();
                groups.put(groupKey, group);
            }
            if (session.getDate() != null && !session.getDate().isEmpty()) {
                if (!group.sessionDates.contains(session.getDate())) {
                    group.sessionDates.add(session.getDate());
                }
                allDates.add(session.getDate());
            }
            group.sessionCount++;
            if (session.getRecords() == null) {
                continue;
            }
            Map<String, StudentRow> byStudent = studentIndex(group);
            for (AttendanceRecord record : session.getRecords()) {
                if (record == null || record.getStatus() == null
                        || record.getStatus() == AttendanceStatus.NOT_MARKED) {
                    continue;
                }
                String studentId = record.getStudentId() == null
                        ? record.getRollNumber() + "|" + record.getStudentName()
                        : record.getStudentId();
                StudentRow row = byStudent.get(studentId);
                if (row == null) {
                    row = new StudentRow();
                    row.studentId = record.getStudentId();
                    row.studentName = record.getStudentName();
                    row.rollNumber = record.getRollNumber();
                    copyClass(row, group);
                    group.students.add(row);
                    byStudent.put(studentId, row);
                }
                switch (record.getStatus()) {
                    case PRESENT:
                        row.presentCount++;
                        break;
                    case ABSENT:
                        row.absentCount++;
                        break;
                    case LATE:
                        row.lateCount++;
                        break;
                    case LEAVE:
                        row.leaveCount++;
                        break;
                    default:
                        break;
                }
                row.dates.add(new DateMark(session.getDate(), record.getStatus()));
            }
        }

        double threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;
        List<StudentRow> allRows = new ArrayList<>();
        for (CourseGroup group : groups.values()) {
            Collections.sort(group.sessionDates);
            int groupPresent = 0;
            int groupAbsent = 0;
            int groupLate = 0;
            int groupLeave = 0;
            int groupAdj = 0;
            for (StudentRow row : group.students) {
                row.recordedSessions = row.presentCount + row.absentCount + row.lateCount + row.leaveCount;
                row.attendedSessions = row.presentCount + row.lateCount;
                row.countableSessions = row.presentCount + row.absentCount + row.lateCount;
                if (adjustmentDeltas != null && row.studentId != null
                        && adjustmentDeltas.get(row.studentId) != null) {
                    row.adjustmentDelta = adjustmentDeltas.get(row.studentId);
                    groupAdj += row.adjustmentDelta;
                }
                row.attendanceRate = AttendancePolicy.rate(row.presentCount, row.absentCount,
                        row.lateCount, row.leaveCount, row.adjustmentDelta);
                // Strict: Below 75% means rate < 75. Exact 75.00 belongs in "75% and Above".
                row.belowThreshold = row.countableSessions > 0
                        && Double.compare(row.attendanceRate, threshold) < 0;
                row.dates.sort(Comparator.comparing(mark -> mark.date == null ? "" : mark.date));
                groupPresent += row.presentCount;
                groupAbsent += row.absentCount;
                groupLate += row.lateCount;
                groupLeave += row.leaveCount;
                if (row.belowThreshold) {
                    group.belowCount++;
                } else if (row.countableSessions > 0) {
                    group.atOrAboveCount++;
                }
                allRows.add(row);
            }
            group.allStudents.addAll(group.students);
            group.studentCount = group.allStudents.size();
            group.averageRate = AttendancePolicy.rate(groupPresent, groupAbsent, groupLate, groupLeave, groupAdj);
        }

        List<CourseGroup> ordered = new ArrayList<>(groups.values());
        ordered.sort((a, b) -> {
            int dept = compare(a.departmentName, b.departmentName);
            if (dept != 0) {
                return dept;
            }
            int sem = compare(a.semesterName, b.semesterName);
            if (sem != 0) {
                return sem;
            }
            int sec = compare(a.sectionName, b.sectionName);
            if (sec != 0) {
                return sec;
            }
            return compare(a.subjectName, b.subjectName);
        });

        StatusFilter status = statusFilter == null ? StatusFilter.ALL : statusFilter;
        SortMode sort = sortMode == null ? SortMode.PERCENTAGE : sortMode;
        List<StudentRow> visible = new ArrayList<>();
        for (StudentRow row : allRows) {
            if (!matchesSearch(row, searchQuery)) {
                continue;
            }
            if (status == StatusFilter.BELOW_THRESHOLD && !row.belowThreshold) {
                continue;
            }
            if (status == StatusFilter.AT_OR_ABOVE && (row.belowThreshold || row.countableSessions <= 0)) {
                continue;
            }
            visible.add(row);
        }
        sortRows(visible, sort);
        for (CourseGroup group : ordered) {
            List<StudentRow> filtered = new ArrayList<>();
            for (StudentRow row : group.students) {
                if (visible.contains(row)) {
                    filtered.add(row);
                }
            }
            sortRows(filtered, sort);
            group.students.clear();
            group.students.addAll(filtered);
        }

        Snapshot snapshot = new Snapshot();
        snapshot.courses.addAll(ordered);
        snapshot.visibleRows.addAll(visible);
        snapshot.exportRows.addAll(allRows);
        sortRows(snapshot.exportRows, SortMode.PERCENTAGE);
        snapshot.hasAttendance = !matched.isEmpty();
        snapshot.hasVisibleStudents = !visible.isEmpty();
        snapshot.hasActiveFilters = safeFilter.hasAny();
        // Empty-Below messaging uses the full academic-scope population.
        int scopeBelow = uniqueBelowCount(allRows);
        int scopeStudents = uniqueStudentCount(allRows);
        snapshot.statusFilterEmptyBecauseAllAbove = status == StatusFilter.BELOW_THRESHOLD
                && snapshot.hasAttendance && scopeBelow == 0 && scopeStudents > 0;

        // Summary cards / highest-lowest must match the active status+search list.
        int presentVisible = 0;
        int absentVisible = 0;
        int lateVisible = 0;
        int leaveVisible = 0;
        int adjustmentVisible = 0;
        for (StudentRow row : visible) {
            presentVisible += row.presentCount;
            absentVisible += row.absentCount;
            lateVisible += row.lateCount;
            leaveVisible += row.leaveCount;
            adjustmentVisible += row.adjustmentDelta;
        }
        snapshot.totalStudents = uniqueStudentCount(visible);
        snapshot.presentCount = presentVisible;
        snapshot.absentCount = absentVisible;
        snapshot.lateCount = lateVisible;
        snapshot.leaveCount = leaveVisible;
        snapshot.belowCount = uniqueBelowCount(visible);
        snapshot.atOrAboveCount = Math.max(0, uniqueCountableCount(visible) - snapshot.belowCount);
        snapshot.averageRate = AttendancePolicy.rate(presentVisible, absentVisible, lateVisible,
                leaveVisible, adjustmentVisible);
        snapshot.highest = extreme(visible, true);
        snapshot.lowest = extreme(visible, false);
        boolean listNarrowed = status != StatusFilter.ALL
                || (searchQuery != null && !searchQuery.trim().isEmpty());
        if (listNarrowed) {
            snapshot.totalSessions = countSessionsForStudents(matched, visible);
        } else {
            snapshot.totalSessions = matched.size();
        }
        snapshot.periodFrom = allDates.isEmpty() ? null : allDates.first();
        snapshot.periodTo = allDates.isEmpty() ? null : allDates.last();
        return snapshot;
    }

    /** Unique attendance sessions that include at least one student in {@code students}. */
    static int countSessionsForStudents(List<AttendanceSession> sessions, List<StudentRow> students) {
        if (sessions == null || sessions.isEmpty() || students == null || students.isEmpty()) {
            return 0;
        }
        java.util.HashSet<String> ids = new java.util.HashSet<>();
        for (StudentRow row : students) {
            if (row == null) {
                continue;
            }
            if (row.studentId != null && !row.studentId.isEmpty()) {
                ids.add(row.studentId);
            } else {
                ids.add(studentKey(row));
            }
        }
        int count = 0;
        for (AttendanceSession session : sessions) {
            if (session == null || session.getRecords() == null) {
                continue;
            }
            for (AttendanceRecord record : session.getRecords()) {
                if (record == null) {
                    continue;
                }
                String id = record.getStudentId();
                if (id == null || id.isEmpty()) {
                    id = record.getRollNumber() + "|" + record.getStudentName();
                }
                if (ids.contains(id)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public static final class SheetBuild {
        public final List<String[]> rows;
        public final int tableHeaderRowIndex;

        public SheetBuild(List<String[]> rows, int tableHeaderRowIndex) {
            this.rows = rows;
            this.tableHeaderRowIndex = tableHeaderRowIndex;
        }
    }

    public static List<String[]> summarySheet(Snapshot snapshot, HistoryFilter filter) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Attendance Report"});
        rows.add(new String[]{""});
        CourseGroup first = snapshot == null || snapshot.courses.isEmpty() ? null : snapshot.courses.get(0);
        boolean single = snapshot != null && snapshot.courses.size() == 1;
        AttendanceExportRows.appendMeta(rows, "Department:", label(filter == null ? null : filter.departmentId,
                single && first != null ? first.departmentName : "All"));
        AttendanceExportRows.appendMeta(rows, "Shift:",
                filter == null || filter.shift == null
                        ? "All"
                        : com.example.studentattendance.model.DepartmentShift.label(filter.shift));
        AttendanceExportRows.appendMeta(rows, "Semester:", label(filter == null ? null : filter.semesterId,
                single && first != null ? first.semesterName : "All"));
        AttendanceExportRows.appendMeta(rows, "Section:", label(filter == null ? null : filter.sectionId,
                single && first != null ? first.sectionName : "All"));
        AttendanceExportRows.appendMeta(rows, "Course:", label(filter == null ? null : filter.subjectId,
                single && first != null ? first.subjectName : "All"));
        AttendanceExportRows.appendMeta(rows, "Course Code:", label(filter == null ? null : filter.courseCode,
                single && first != null ? first.subjectCode : "All"));
        if (snapshot != null) {
            AttendanceExportRows.appendMeta(rows, "Reporting Period:",
                    DateUtils.reportingPeriod(snapshot.periodFrom, snapshot.periodTo));
            AttendanceExportRows.appendMeta(rows, "Total Students (unique):", String.valueOf(snapshot.totalStudents));
            AttendanceExportRows.appendMeta(rows, "Total Sessions:", String.valueOf(snapshot.totalSessions));
            AttendanceExportRows.appendMeta(rows, "Present Records:", String.valueOf(snapshot.presentCount));
            AttendanceExportRows.appendMeta(rows, "Absent Records:", String.valueOf(snapshot.absentCount));
            AttendanceExportRows.appendMeta(rows, "Late Records:", String.valueOf(snapshot.lateCount));
            AttendanceExportRows.appendMeta(rows, "Leave Records:", String.valueOf(snapshot.leaveCount));
            AttendanceExportRows.appendMeta(rows, "Average Attendance:",
                    String.format(Locale.US, "%.1f%%", snapshot.averageRate));
            AttendanceExportRows.appendMeta(rows, "Students >= 75%:", String.valueOf(snapshot.atOrAboveCount));
            AttendanceExportRows.appendMeta(rows, "Students < 75%:", String.valueOf(snapshot.belowCount));
        }
        return rows;
    }

    public static SheetBuild studentSummaryReport(Snapshot snapshot) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Student Attendance Summary"});
        rows.add(new String[]{""});
        CourseGroup first = snapshot == null || snapshot.courses.isEmpty() ? null : snapshot.courses.get(0);
        boolean singleCourse = snapshot != null && snapshot.courses.size() == 1;
        if (first != null) {
            AttendanceExportRows.appendMeta(rows, "Department:",
                    singleCourse ? safe(first.departmentName) : "Multiple");
            AttendanceExportRows.appendMeta(rows, "Semester:",
                    singleCourse ? safe(first.semesterName) : "Multiple");
            AttendanceExportRows.appendMeta(rows, "Section:",
                    singleCourse ? HistoryFilter.sectionLabel(first.sectionName) : "Multiple");
            if (singleCourse) {
                AttendanceExportRows.appendMeta(rows, "Course:", safe(first.subjectName));
                AttendanceExportRows.appendMeta(rows, "Course Code:", safe(first.subjectCode));
            }
        }
        if (snapshot != null) {
            AttendanceExportRows.appendMeta(rows, "Reporting Period:",
                    DateUtils.reportingPeriod(snapshot.periodFrom, snapshot.periodTo));
        }
        rows.add(new String[]{""});

        int tableHeaderRowIndex = rows.size();
        if (singleCourse) {
            rows.add(new String[]{
                    "#", "Student Name", "Roll No", "Present", "Late", "Absent", "Leave",
                    "Total Sessions", "Attendance %", "Status"
            });
        } else {
            rows.add(new String[]{
                    "#", "Student Name", "Roll No", "Course", "Course Code",
                    "Present", "Late", "Absent", "Leave",
                    "Total Sessions", "Attendance %", "Status"
            });
        }

        if (snapshot == null) {
            return new SheetBuild(rows, tableHeaderRowIndex);
        }
        List<StudentRow> source = snapshot.visibleRows;
        int serial = 1;
        for (StudentRow row : source) {
            if (singleCourse) {
                rows.add(new String[]{
                        String.valueOf(serial++),
                        safe(row.studentName),
                        ExcelIdentifiers.normalizeRollNumber(row.rollNumber),
                        String.valueOf(row.presentCount),
                        String.valueOf(row.lateCount),
                        String.valueOf(row.absentCount),
                        String.valueOf(row.leaveCount),
                        String.valueOf(row.recordedSessions),
                        String.format(Locale.US, "%.1f%%", row.attendanceRate),
                        row.statusLabel()
                });
            } else {
                rows.add(new String[]{
                        String.valueOf(serial++),
                        safe(row.studentName),
                        ExcelIdentifiers.normalizeRollNumber(row.rollNumber),
                        safe(row.subjectName),
                        safe(row.subjectCode),
                        String.valueOf(row.presentCount),
                        String.valueOf(row.lateCount),
                        String.valueOf(row.absentCount),
                        String.valueOf(row.leaveCount),
                        String.valueOf(row.recordedSessions),
                        String.format(Locale.US, "%.1f%%", row.attendanceRate),
                        row.statusLabel()
                });
            }
        }
        return new SheetBuild(rows, tableHeaderRowIndex);
    }

    public static List<String[]> studentSummarySheet(Snapshot snapshot) {
        return studentSummaryReport(snapshot).rows;
    }

    public static SheetBuild dateMatrixReport(CourseGroup group) {
        List<String[]> rows = new ArrayList<>();
        if (group == null) {
            return new SheetBuild(rows, 0);
        }
        rows.add(new String[]{"Course Attendance Matrix"});
        rows.add(new String[]{""});
        AttendanceExportRows.appendMeta(rows, "Department:", safe(group.departmentName));
        AttendanceExportRows.appendMeta(rows, "Semester:", safe(group.semesterName));
        AttendanceExportRows.appendMeta(rows, "Section:", HistoryFilter.sectionLabel(group.sectionName));
        AttendanceExportRows.appendMeta(rows, "Course:", safe(group.subjectName));
        AttendanceExportRows.appendMeta(rows, "Course Code:", safe(group.subjectCode));
        rows.add(new String[]{""});

        int tableHeaderRowIndex = rows.size();
        List<String> header = new ArrayList<>();
        header.add("#");
        header.add("Student Name");
        header.add("Roll No");
        header.addAll(group.sessionDates);
        header.add("Attendance %");
        rows.add(header.toArray(new String[0]));

        List<StudentRow> source = group.students;
        int serial = 1;
        for (StudentRow student : source) {
            Map<String, String> byDate = new LinkedHashMap<>();
            for (DateMark mark : student.dates) {
                if (mark.date != null && mark.status != null) {
                    byDate.put(mark.date, mark.status.getLabel());
                }
            }
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(serial++));
            row.add(safe(student.studentName));
            row.add(ExcelIdentifiers.normalizeRollNumber(student.rollNumber));
            for (String date : group.sessionDates) {
                String status = byDate.get(date);
                row.add(status == null ? "" : status);
            }
            row.add(String.format(Locale.US, "%.1f%%", student.attendanceRate));
            rows.add(row.toArray(new String[0]));
        }
        return new SheetBuild(rows, tableHeaderRowIndex);
    }

    public static List<String[]> dateMatrixSheet(CourseGroup group) {
        return dateMatrixReport(group).rows;
    }

    public static String sheetNameFor(CourseGroup group, int index) {
        String name = group == null ? "Course" : safe(group.subjectCode);
        if (name.isEmpty() && group != null) {
            name = safe(group.subjectName);
        }
        if (name.isEmpty()) {
            name = "Course " + (index + 1);
        }
        name = name.replaceAll("[\\\\/:?*\\[\\]]", " ").trim();
        if (name.length() > 28) {
            name = name.substring(0, 28);
        }
        return name;
    }

    private static Map<String, StudentRow> studentIndex(CourseGroup group) {
        Map<String, StudentRow> map = new LinkedHashMap<>();
        for (StudentRow row : group.students) {
            String key = row.studentId == null ? row.rollNumber + "|" + row.studentName : row.studentId;
            map.put(key, row);
        }
        return map;
    }

    private static void copyClass(StudentRow row, CourseGroup group) {
        row.departmentId = group.departmentId;
        row.departmentName = group.departmentName;
        row.semesterId = group.semesterId;
        row.semesterName = group.semesterName;
        row.sectionId = group.sectionId;
        row.sectionName = group.sectionName;
        row.subjectId = group.subjectId;
        row.subjectName = group.subjectName;
        row.subjectCode = group.subjectCode;
    }

    private static String courseKey(AttendanceSession session) {
        if (session.getSubjectId() != null && !session.getSubjectId().isEmpty()) {
            return session.getSubjectId();
        }
        return safe(session.getDepartmentId()) + "|" + safe(session.getSemesterId()) + "|"
                + safe(session.getSectionId()) + "|" + safe(session.getSubjectCode());
    }

    private static boolean matchesSearch(StudentRow row, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.getDefault());
        String name = row.studentName == null ? "" : row.studentName.toLowerCase(Locale.getDefault());
        String roll = row.rollNumber == null ? "" : row.rollNumber.toLowerCase(Locale.getDefault());
        return name.contains(needle) || roll.contains(needle);
    }

    private static void sortRows(List<StudentRow> rows, SortMode sortMode) {
        Comparator<StudentRow> comparator;
        switch (sortMode) {
            case ROLL:
                comparator = Comparator.comparing(r -> r.rollNumber == null ? "" : r.rollNumber,
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case PRESENT:
                comparator = Comparator.comparingInt((StudentRow r) -> r.presentCount).reversed();
                break;
            case ABSENT:
                comparator = Comparator.comparingInt((StudentRow r) -> r.absentCount).reversed();
                break;
            case NAME:
                comparator = Comparator.comparing(r -> r.studentName == null ? "" : r.studentName,
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case PERCENTAGE:
            default:
                comparator = Comparator.comparingDouble((StudentRow r) -> r.attendanceRate)
                        .thenComparing(r -> r.studentName == null ? "" : r.studentName,
                                String.CASE_INSENSITIVE_ORDER);
                break;
        }
        Collections.sort(rows, comparator);
    }

    private static int uniqueBelowCount(List<StudentRow> rows) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (StudentRow row : rows) {
            if (!row.belowThreshold) {
                continue;
            }
            seen.put(studentKey(row), Boolean.TRUE);
        }
        return seen.size();
    }

    private static int uniqueCountableCount(List<StudentRow> rows) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (StudentRow row : rows) {
            if (row.countableSessions <= 0) {
                continue;
            }
            seen.put(studentKey(row), Boolean.TRUE);
        }
        return seen.size();
    }

    private static String studentKey(StudentRow row) {
        return row.studentId == null ? row.rollNumber + "|" + row.studentName : row.studentId;
    }

    private static int uniqueStudentCount(List<StudentRow> rows) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (StudentRow row : rows) {
            seen.put(studentKey(row), Boolean.TRUE);
        }
        return seen.size();
    }

    private static StudentRow extreme(List<StudentRow> rows, boolean highest) {
        StudentRow best = null;
        for (StudentRow row : rows) {
            if (row.countableSessions <= 0) {
                continue;
            }
            if (best == null) {
                best = row;
                continue;
            }
            if (highest && row.attendanceRate > best.attendanceRate) {
                best = row;
            } else if (!highest && row.attendanceRate < best.attendanceRate) {
                best = row;
            }
        }
        return best;
    }

    private static String label(String filterId, String fallback) {
        return filterId == null ? "All" : safe(fallback);
    }

    private static int compare(String left, String right) {
        return safe(left).compareToIgnoreCase(safe(right));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
