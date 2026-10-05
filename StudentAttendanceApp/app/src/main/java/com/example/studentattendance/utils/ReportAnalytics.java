package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.model.Subject;

/**
 * Pure Room-backed report math used by the Reports screen. Filters may be
 * applied independently or combined (AND). Percentages always go through
 * {@link AttendancePolicy}.
 */
public final class ReportAnalytics {

    public enum SortMode { NAME, PERCENTAGE, ROLL }

    public static final class Filter {
        public String departmentId;
        public String semesterId;
        public String sectionId;
        public String subjectId;
        public String courseCode;
        public String fromDate;
        public String toDate;

        public Filter() {
        }

        public Filter(String departmentId, String sectionId, String courseCode) {
            this(departmentId, null, sectionId, null, courseCode, null, null);
        }

        public Filter(String departmentId, String semesterId, String sectionId, String subjectId,
                      String courseCode, String fromDate, String toDate) {
            this.departmentId = emptyToNull(departmentId);
            this.semesterId = emptyToNull(semesterId);
            this.sectionId = emptyToNull(sectionId);
            this.subjectId = emptyToNull(subjectId);
            this.courseCode = normalizeCode(courseCode);
            this.fromDate = emptyToNull(fromDate);
            this.toDate = emptyToNull(toDate);
        }

        public boolean hasAny() {
            return departmentId != null || semesterId != null || sectionId != null
                    || subjectId != null || courseCode != null || fromDate != null || toDate != null;
        }
    }

    public static final class TodayOverview {
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int leaveCount;
        public double attendanceRate;
        public boolean hasData;
    }

    public static TodayOverview todayOverview(int present, int absent, int late, int leave) {
        TodayOverview overview = new TodayOverview();
        overview.presentCount = Math.max(0, present);
        overview.absentCount = Math.max(0, absent);
        overview.lateCount = Math.max(0, late);
        overview.leaveCount = Math.max(0, leave);
        overview.hasData = overview.presentCount + overview.absentCount
                + overview.lateCount + overview.leaveCount > 0;
        overview.attendanceRate = overview.hasData
                ? AttendancePolicy.rate(overview.presentCount, overview.absentCount,
                overview.lateCount, overview.leaveCount, 0)
                : 0;
        return overview;
    }

    public static List<DailyTrend> realTrendPoints(List<DailyTrend> trend) {
        List<DailyTrend> real = new ArrayList<>();
        if (trend == null) {
            return real;
        }
        for (DailyTrend point : trend) {
            if (point != null && point.getTotalMarked() > 0) {
                real.add(point);
            }
        }
        return real;
    }

    public static final class Snapshot {
        public int totalStudents;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int leaveCount;
        public int totalSessions;
        public double attendanceRate;
        public List<StudentReport> studentReports = new ArrayList<>();
        public List<DailyTrend> weeklyTrend = new ArrayList<>();
        public List<DailyTrend> monthlyTrend = new ArrayList<>();
    }

    private ReportAnalytics() {
    }

    public static Snapshot build(List<AttendanceSession> sessions,
                                 List<Student> students,
                                 List<Subject> subjects,
                                 Map<String, Integer> adjustmentDeltas,
                                 Filter filter,
                                 String weekStart,
                                 String weekEnd,
                                 String monthStart,
                                 String monthEnd,
                                 String searchQuery,
                                 SortMode sortMode) {
        Filter safeFilter = filter == null ? new Filter() : filter;
        Map<String, String> subjectCodes = subjectCodeById(subjects);
        Set<String> sectionIdsForCourse = sectionIdsForCourse(subjects, safeFilter.courseCode);

        List<AttendanceSession> matchedSessions = new ArrayList<>();
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                if (sessionMatches(session, safeFilter, subjectCodes)) {
                    matchedSessions.add(session);
                }
            }
        }

        Map<String, StudentReport> reports = new LinkedHashMap<>();
        if (students != null) {
            for (Student student : students) {
                if (student == null || student.isArchived()) {
                    continue;
                }
                if (studentMatches(student, safeFilter, sectionIdsForCourse)) {
                    reports.put(student.getId(),
                            new StudentReport(student.getId(), student.getName(), student.getRollNumber()));
                }
            }
        }

        int present = 0;
        int absent = 0;
        int late = 0;
        int leave = 0;
        for (AttendanceSession session : matchedSessions) {
            if (session.getRecords() == null) {
                continue;
            }
            for (AttendanceRecord record : session.getRecords()) {
                if (record == null || record.getStatus() == null
                        || record.getStatus() == AttendanceStatus.NOT_MARKED) {
                    continue;
                }
                StudentReport report = reports.computeIfAbsent(record.getStudentId(),
                        id -> new StudentReport(id, record.getStudentName(), record.getRollNumber()));
                report.increment(record.getStatus(), session.getDate(), weekStart, monthStart);
                switch (record.getStatus()) {
                    case PRESENT:
                        present++;
                        break;
                    case ABSENT:
                        absent++;
                        break;
                    case LATE:
                        late++;
                        break;
                    case LEAVE:
                        leave++;
                        break;
                    default:
                        break;
                }
            }
        }

        int adjustments = 0;
        List<StudentReport> visible = new ArrayList<>();
        for (StudentReport report : reports.values()) {
            if (adjustmentDeltas != null && report.getStudentId() != null) {
                Integer delta = adjustmentDeltas.get(report.getStudentId());
                if (delta != null) {
                    report.setAdjustmentDelta(delta);
                    adjustments += delta;
                }
            }
            if (matchesSearch(report, searchQuery)) {
                visible.add(report);
            }
        }
        sortReports(visible, sortMode);

        Snapshot snapshot = new Snapshot();
        snapshot.totalStudents = reports.size();
        snapshot.presentCount = present;
        snapshot.absentCount = absent;
        snapshot.lateCount = late;
        snapshot.leaveCount = leave;
        snapshot.totalSessions = matchedSessions.size();
        snapshot.attendanceRate = AttendancePolicy.rate(present, absent, late, leave, adjustments);
        snapshot.studentReports = visible;
        snapshot.weeklyTrend = trendForRange(matchedSessions, weekStart, weekEnd);
        snapshot.monthlyTrend = trendForRange(matchedSessions, monthStart, monthEnd);
        return snapshot;
    }

    static boolean sessionMatches(AttendanceSession session, Filter filter, Map<String, String> subjectCodes) {
        if (session == null) {
            return false;
        }
        if (filter.departmentId != null && !filter.departmentId.equals(session.getDepartmentId())) {
            return false;
        }
        if (filter.semesterId != null && !filter.semesterId.equals(session.getSemesterId())) {
            return false;
        }
        if (filter.sectionId != null && !filter.sectionId.equals(session.getSectionId())) {
            return false;
        }
        if (filter.subjectId != null && !filter.subjectId.equals(session.getSubjectId())) {
            return false;
        }
        if (filter.fromDate != null && (session.getDate() == null
                || session.getDate().compareTo(filter.fromDate) < 0)) {
            return false;
        }
        if (filter.toDate != null && (session.getDate() == null
                || session.getDate().compareTo(filter.toDate) > 0)) {
            return false;
        }
        if (filter.courseCode != null) {
            String code = resolveSessionCode(session, subjectCodes);
            if (!filter.courseCode.equals(code)) {
                return false;
            }
        }
        return true;
    }

    static boolean studentMatches(Student student, Filter filter, Set<String> sectionIdsForCourse) {
        if (student == null) {
            return false;
        }
        if (filter.departmentId != null && !filter.departmentId.equals(student.getDepartmentId())) {
            return false;
        }
        if (filter.semesterId != null && !filter.semesterId.equals(student.getSemesterId())) {
            return false;
        }
        if (filter.sectionId != null && !filter.sectionId.equals(student.getSectionId())) {
            return false;
        }
        if (filter.courseCode != null && (sectionIdsForCourse == null
                || !sectionIdsForCourse.contains(student.getSectionId()))) {
            return false;
        }
        return true;
    }

    /**
     * Keeps sessions that include at least one of the given student IDs so charts
     * match an active status/search filter.
     */
    public static List<AttendanceSession> sessionsForStudentIds(
            List<AttendanceSession> sessions, Set<String> studentIds) {
        List<AttendanceSession> result = new ArrayList<>();
        if (sessions == null || studentIds == null || studentIds.isEmpty()) {
            return result;
        }
        for (AttendanceSession session : sessions) {
            if (session == null || session.getRecords() == null) {
                continue;
            }
            for (AttendanceRecord record : session.getRecords()) {
                if (record == null || record.getStudentId() == null) {
                    continue;
                }
                if (studentIds.contains(record.getStudentId())) {
                    result.add(session);
                    break;
                }
            }
        }
        return result;
    }

    public static List<DailyTrend> trendForRange(List<AttendanceSession> sessions, String from, String to) {
        List<DailyTrend> trend = new ArrayList<>();
        if (from == null || to == null) {
            return trend;
        }
        Map<String, int[]> totals = new LinkedHashMap<>();
        for (String date : DateUtils.datesInclusive(from, to)) {
            totals.put(date, new int[4]);
        }
        if (sessions != null) {
            for (AttendanceSession session : sessions) {
                int[] bucket = totals.get(session.getDate());
                if (bucket == null || session.getRecords() == null) {
                    continue;
                }
                for (AttendanceRecord record : session.getRecords()) {
                    bump(bucket, record == null ? null : record.getStatus());
                }
            }
        }
        for (Map.Entry<String, int[]> entry : totals.entrySet()) {
            int[] t = entry.getValue();
            int marked = t[0] + t[1] + t[2] + t[3];
            if (marked == 0) {
                continue;
            }
            DailyTrend point = new DailyTrend();
            point.setDate(entry.getKey());
            point.setPresentCount(t[0]);
            point.setAbsentCount(t[1]);
            point.setLateCount(t[2]);
            point.setLeaveCount(t[3]);
            point.setTotalMarked(marked);
            point.setAttendanceRate(AttendancePolicy.rate(t[0], t[1], t[2], t[3], 0));
            trend.add(point);
        }
        return trend;
    }

    private static void bump(int[] totals, AttendanceStatus status) {
        if (status == AttendanceStatus.PRESENT) {
            totals[0]++;
        } else if (status == AttendanceStatus.ABSENT) {
            totals[1]++;
        } else if (status == AttendanceStatus.LATE) {
            totals[2]++;
        } else if (status == AttendanceStatus.LEAVE) {
            totals[3]++;
        }
    }

    private static String resolveSessionCode(AttendanceSession session, Map<String, String> subjectCodes) {
        String direct = normalizeCode(session.getSubjectCode());
        if (direct != null) {
            return direct;
        }
        if (subjectCodes == null || session.getSubjectId() == null) {
            return null;
        }
        return normalizeCode(subjectCodes.get(session.getSubjectId()));
    }

    private static Map<String, String> subjectCodeById(List<Subject> subjects) {
        Map<String, String> map = new LinkedHashMap<>();
        if (subjects == null) {
            return map;
        }
        for (Subject subject : subjects) {
            if (subject != null && subject.getId() != null) {
                map.put(subject.getId(), normalizeCode(subject.getCode()));
            }
        }
        return map;
    }

    private static Set<String> sectionIdsForCourse(List<Subject> subjects, String courseCode) {
        Set<String> ids = new HashSet<>();
        if (courseCode == null || subjects == null) {
            return ids;
        }
        for (Subject subject : subjects) {
            if (subject != null && courseCode.equals(normalizeCode(subject.getCode()))
                    && subject.getSectionId() != null) {
                ids.add(subject.getSectionId());
            }
        }
        return ids;
    }

    private static boolean matchesSearch(StudentReport report, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.getDefault());
        String name = report.getStudentName() == null ? "" : report.getStudentName().toLowerCase(Locale.getDefault());
        String roll = report.getRollNumber() == null ? "" : report.getRollNumber().toLowerCase(Locale.getDefault());
        return name.contains(needle) || roll.contains(needle);
    }

    private static void sortReports(List<StudentReport> reports, SortMode sortMode) {
        Comparator<StudentReport> comparator;
        SortMode mode = sortMode == null ? SortMode.NAME : sortMode;
        switch (mode) {
            case PERCENTAGE:
                comparator = Comparator.comparingDouble(StudentReport::getAttendancePercentage).reversed();
                break;
            case ROLL:
                comparator = Comparator.comparing(r -> r.getRollNumber() == null ? "" : r.getRollNumber(),
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case NAME:
            default:
                comparator = Comparator.comparing(r -> r.getStudentName() == null ? "" : r.getStudentName(),
                        String.CASE_INSENSITIVE_ORDER);
                break;
        }
        Collections.sort(reports, comparator);
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String normalizeCode(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().toUpperCase(Locale.US);
    }
}
