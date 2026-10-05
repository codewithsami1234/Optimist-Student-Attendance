package com.example.studentattendance.repository.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.SectionStats;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AnalyticsRepository;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ReportAnalytics;

public class RoomAnalyticsRepository implements AnalyticsRepository {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomAnalyticsRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void getDashboardStats(ApiCallback<DashboardStats> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            User user = LocalOwner.requireUser(prefs);
            String today = todayString();
            List<AttendanceRecordEntity> allRecords = db.attendanceRecordDao().listAll(ownerId);
            int presentToday = 0;
            int absentToday = 0;
            int lateToday = 0;
            int leaveToday = 0;
            for (AttendanceRecordEntity record : allRecords) {
                if (!today.equals(record.date)) {
                    continue;
                }
                if (record.status == AttendanceStatus.PRESENT) {
                    presentToday++;
                } else if (record.status == AttendanceStatus.ABSENT) {
                    absentToday++;
                } else if (record.status == AttendanceStatus.LATE) {
                    lateToday++;
                } else if (record.status == AttendanceStatus.LEAVE) {
                    leaveToday++;
                }
            }
            DashboardStats stats = new DashboardStats();
            stats.setTotalStudents(db.studentDao().listAllActive(ownerId).size());
            stats.setPresentToday(presentToday);
            stats.setAbsentToday(absentToday);
            stats.setLateToday(lateToday);
            stats.setLeaveToday(leaveToday);
            ReportAnalytics.TodayOverview overview = ReportAnalytics.todayOverview(
                    presentToday, absentToday, lateToday, leaveToday);
            stats.setAttendanceRateToday(overview.hasData ? overview.attendanceRate : 0);
            stats.setLowAttendanceThreshold(user.getLowAttendanceThreshold());
            stats.setWeeklyTrend(buildTrend(ownerId, allRecords, null, null, null, null, 7));
            stats.setMonthlyTrend(buildTrend(ownerId, allRecords, null, null, null, null, 30));
            stats.setLowAttendanceStudents(buildLowAttendance(ownerId, allRecords, user.getLowAttendanceThreshold(), 5));
            stats.setRecentSessions(recentSessions(ownerId, 5));
            java.util.List<Section> sections = new ArrayList<>();
            for (SectionEntity section : db.sectionDao().listAllActive(ownerId)) {
                sections.add(section.toModel(db.studentDao().countInSection(ownerId, section.id)));
            }
            stats.setChartSections(sections);
            java.util.List<Student> students = new ArrayList<>();
            for (StudentEntity student : db.studentDao().listAllActive(ownerId)) {
                students.add(student.toModel());
            }
            stats.setChartStudents(students);
            return stats;
        }, callback);
    }

    @Override
    public void getSectionStats(String departmentIdOrNull, String semesterIdOrNull, String subjectIdOrNull,
                                 ApiCallback<List<SectionStats>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<AttendanceRecordEntity> records = filterRecords(
                    db.attendanceRecordDao().listAll(ownerId), departmentIdOrNull, semesterIdOrNull, null, subjectIdOrNull);
            Map<String, int[]> totals = new LinkedHashMap<>();
            Map<String, AttendanceRecordEntity> sample = new LinkedHashMap<>();
            for (AttendanceRecordEntity record : records) {
                int[] t = totals.computeIfAbsent(record.sectionId, k -> new int[4]);
                bump(t, record.status);
                sample.putIfAbsent(record.sectionId, record);
            }
            List<SectionStats> result = new ArrayList<>();
            for (Map.Entry<String, int[]> entry : totals.entrySet()) {
                AttendanceRecordEntity row = sample.get(entry.getKey());
                int[] t = entry.getValue();
                SectionStats stats = new SectionStats();
                stats.setDepartmentId(row.departmentId);
                stats.setSemesterId(row.semesterId);
                stats.setSectionId(entry.getKey());
                stats.setTotalStudents(db.studentDao().countInSection(ownerId, entry.getKey()));
                stats.setPresentCount(t[0] + t[2]);
                stats.setTotalMarked(t[0] + t[1] + t[2] + t[3]);
                stats.setAttendanceRate(AttendancePolicy.rate(t[0], t[1], t[2], t[3], 0));
                SectionEntity section = db.sectionDao().findOwned(entry.getKey(), ownerId);
                if (section != null) {
                    stats.setSectionName(section.name);
                }
                SemesterEntity semester = db.semesterDao().findOwned(row.semesterId, ownerId);
                if (semester != null) {
                    stats.setSemesterName(semester.name);
                }
                DepartmentEntity department = db.departmentDao().findOwned(row.departmentId, ownerId);
                if (department != null) {
                    stats.setDepartmentName(department.name);
                }
                result.add(stats);
            }
            result.sort(Comparator.comparingDouble(SectionStats::getAttendanceRate));
            return result;
        }, callback);
    }

    @Override
    public void getTrend(String departmentIdOrNull, String subjectIdOrNull, int days,
                          ApiCallback<List<DailyTrend>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            return buildTrend(ownerId, db.attendanceRecordDao().listAll(ownerId),
                    departmentIdOrNull, subjectIdOrNull, null, null, days);
        }, callback);
    }

    @Override
    public void getScopedTrend(String sectionIdOrNull, String studentIdOrNull, int days,
                                ApiCallback<List<DailyTrend>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            return buildTrend(ownerId, db.attendanceRecordDao().listAll(ownerId),
                    null, null, sectionIdOrNull, studentIdOrNull, days);
        }, callback);
    }

    @Override
    public void getLowAttendance(Double threshold, int limit, ApiCallback<List<StudentAttendanceSummary>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            double cut = threshold != null ? threshold : LocalOwner.requireUser(prefs).getLowAttendanceThreshold();
            return buildLowAttendance(ownerId, db.attendanceRecordDao().listAll(ownerId), cut, limit);
        }, callback);
    }

    @Override
    public void getStudentReport(String studentId, ApiCallback<StudentAttendanceReport> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            return buildStudentReport(db, ownerId, studentId);
        }, callback);
    }

    public static StudentAttendanceReport buildStudentReport(AppDatabase db, String ownerId, String studentId) {
        StudentEntity student = db.studentDao().findOwned(studentId, ownerId);
        if (student == null) {
            throw new LocalDataException("Student not found.");
        }
        List<AttendanceRecordEntity> records = db.attendanceRecordDao().listByStudent(ownerId, studentId);
        int present = 0;
        int absent = 0;
        int late = 0;
        int leave = 0;
        List<AttendanceRecord> mapped = new ArrayList<>();
        for (AttendanceRecordEntity record : records) {
            mapped.add(record.toModel());
            if (record.status == AttendanceStatus.PRESENT) {
                present++;
            } else if (record.status == AttendanceStatus.ABSENT) {
                absent++;
            } else if (record.status == AttendanceStatus.LATE) {
                late++;
            } else if (record.status == AttendanceStatus.LEAVE) {
                leave++;
            }
        }
        int adjustment = db.adjustmentDao().netDelta(ownerId, studentId);
        StudentAttendanceReport report = new StudentAttendanceReport();
        report.setStudentId(student.id);
        report.setStudentName(student.name);
        report.setRollNumber(student.rollNumber);
        report.setDepartmentId(student.departmentId);
        report.setSemesterId(student.semesterId);
        report.setSectionId(student.sectionId);
        DepartmentEntity department = db.departmentDao().findOwned(student.departmentId, ownerId);
        if (department != null) {
            report.setDepartmentName(department.name);
        }
        SemesterEntity semester = db.semesterDao().findOwned(student.semesterId, ownerId);
        if (semester != null) {
            report.setSemesterName(semester.name);
        }
        SectionEntity section = db.sectionDao().findOwned(student.sectionId, ownerId);
        if (section != null) {
            report.setSectionName(section.name);
        }
        report.setPresentCount(present);
        report.setAbsentCount(absent);
        report.setLateCount(late);
        report.setLeaveCount(leave);
        report.setTotalMarked(present + absent + late + leave);
        report.setAdjustmentDelta(adjustment);
        report.setAdjustedPresentCount(Math.max(0, present + late + adjustment));
        report.setAttendanceRate(AttendancePolicy.rate(present, absent, late, leave, adjustment));
        report.setRecords(mapped);
        return report;
    }

    private List<DailyTrend> buildTrend(String ownerId, List<AttendanceRecordEntity> allRecords,
                                        String departmentId, String subjectId,
                                        String sectionId, String studentId, int days) {
        int safeDays = Math.max(1, Math.min(days, 90));
        SimpleDateFormat fmt = dateFormat();
        Map<String, int[]> totals = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -(safeDays - 1));
        for (int i = 0; i < safeDays; i++) {
            totals.put(fmt.format(calendar.getTime()), new int[4]);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        for (AttendanceRecordEntity record : allRecords) {
            if (notBlank(departmentId) && !departmentId.equals(record.departmentId)) {
                continue;
            }
            if (notBlank(sectionId) && !sectionId.equals(record.sectionId)) {
                continue;
            }
            if (notBlank(studentId) && !studentId.equals(record.studentId)) {
                continue;
            }
            int[] t = totals.get(record.date);
            if (t == null) {
                continue;
            }
            bump(t, record.status);
        }
        List<DailyTrend> trend = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totals.entrySet()) {
            int[] t = entry.getValue();
            DailyTrend point = new DailyTrend();
            point.setDate(entry.getKey());
            point.setPresentCount(t[0]);
            point.setAbsentCount(t[1]);
            point.setLateCount(t[2]);
            point.setLeaveCount(t[3]);
            point.setTotalMarked(t[0] + t[1] + t[2] + t[3]);
            point.setAttendanceRate(AttendancePolicy.rate(t[0], t[1], t[2], t[3], 0));
            trend.add(point);
        }
        return ReportAnalytics.realTrendPoints(trend);
    }

    private List<StudentAttendanceSummary> buildLowAttendance(String ownerId, List<AttendanceRecordEntity> allRecords,
                                                              double threshold, int limit) {
        Map<String, int[]> totals = new LinkedHashMap<>();
        Map<String, AttendanceRecordEntity> sample = new LinkedHashMap<>();
        for (AttendanceRecordEntity record : allRecords) {
            int[] t = totals.computeIfAbsent(record.studentId, k -> new int[4]);
            bump(t, record.status);
            sample.putIfAbsent(record.studentId, record);
        }
        List<StudentAttendanceSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totals.entrySet()) {
            int[] t = entry.getValue();
            int countable = t[0] + t[1] + t[2];
            if (countable == 0) {
                continue;
            }
            int adjustment = db.adjustmentDao().netDelta(ownerId, entry.getKey());
            double rate = AttendancePolicy.rate(t[0], t[1], t[2], t[3], adjustment);
            if (rate > threshold) {
                continue;
            }
            AttendanceRecordEntity row = sample.get(entry.getKey());
            StudentAttendanceSummary summary = new StudentAttendanceSummary();
            summary.setStudentId(entry.getKey());
            summary.setStudentName(row.studentName);
            summary.setRollNumber(row.rollNumber);
            summary.setSectionId(row.sectionId);
            SectionEntity section = db.sectionDao().findOwned(row.sectionId, ownerId);
            if (section != null) {
                summary.setSectionName(section.name);
            }
            summary.setPresentCount(Math.max(0, t[0] + t[2] + adjustment));
            summary.setTotalMarked(countable);
            summary.setAttendanceRate(rate);
            summaries.add(summary);
        }
        summaries.sort(Comparator.comparingDouble(StudentAttendanceSummary::getAttendanceRate));
        if (summaries.size() > limit && limit > 0) {
            return new ArrayList<>(summaries.subList(0, limit));
        }
        return summaries;
    }

    private List<AttendanceSession> recentSessions(String ownerId, int limit) {
        List<AttendanceSession> result = new ArrayList<>();
        List<AttendanceSessionEntity> sessions = db.attendanceSessionDao().listAll(ownerId);
        int count = 0;
        for (AttendanceSessionEntity entity : sessions) {
            AttendanceSession session = entity.toModel();
            List<AttendanceRecord> records = new ArrayList<>();
            for (AttendanceRecordEntity record : db.attendanceRecordDao().listBySession(entity.id, ownerId)) {
                records.add(record.toModel());
            }
            session.setRecords(records);
            result.add(session);
            count++;
            if (count >= limit) {
                break;
            }
        }
        return result;
    }

    private static List<AttendanceRecordEntity> filterRecords(List<AttendanceRecordEntity> records,
                                                               String departmentId, String semesterId,
                                                               String sectionId, String subjectId) {
        List<AttendanceRecordEntity> result = new ArrayList<>();
        for (AttendanceRecordEntity record : records) {
            if (notBlank(departmentId) && !departmentId.equals(record.departmentId)) {
                continue;
            }
            if (notBlank(semesterId) && !semesterId.equals(record.semesterId)) {
                continue;
            }
            if (notBlank(sectionId) && !sectionId.equals(record.sectionId)) {
                continue;
            }
            result.add(record);
        }
        return result;
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

    private static boolean notBlank(String value) {
        return value != null && !value.isEmpty();
    }

    static String todayString() {
        return dateFormat().format(new Date());
    }

    static SimpleDateFormat dateFormat() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fmt.setLenient(false);
        return fmt;
    }

    static boolean isFutureDate(String date) {
        try {
            Date parsed = dateFormat().parse(date);
            Date today = dateFormat().parse(todayString());
            return parsed != null && today != null && parsed.after(today);
        } catch (ParseException e) {
            throw new LocalDataException("Please select a valid date.");
        }
    }
}
