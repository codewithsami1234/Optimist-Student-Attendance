package com.example.studentattendance.sync;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.User;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.DateUtils;

/**
 * Creates a low-attendance notification only when a student becomes short
 * for the current monthly period, using real Room records and
 * {@link AttendancePolicy}. Not used for daily polling.
 */
public final class LowAttendanceEvaluator {

    private LowAttendanceEvaluator() {
    }

    public static int notifyIfBelow(AppDatabase db, String ownerId, User teacher,
                                    Collection<String> studentIds) {
        if (db == null || ownerId == null || teacher == null || studentIds == null || studentIds.isEmpty()) {
            return 0;
        }
        double threshold = teacher.getLowAttendanceThreshold();
        if (threshold <= 0) {
            threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;
        }
        String monthStart = DateUtils.startOfCurrentMonth();
        String today = DateUtils.today();
        long sinceMonth = DateUtils.startOfCurrentMonthMillis();
        Set<String> unique = new HashSet<>(studentIds);
        int created = 0;
        for (String studentId : unique) {
            if (studentId == null || studentId.isEmpty()) {
                continue;
            }
            StudentEntity student = db.studentDao().findOwned(studentId, ownerId);
            if (student == null) {
                continue;
            }
            int present = 0;
            int absent = 0;
            int late = 0;
            int leave = 0;
            for (AttendanceRecordEntity record : db.attendanceRecordDao().listByStudent(ownerId, studentId)) {
                if (!DateUtils.inInclusiveRange(record.date, monthStart, today)) {
                    continue;
                }
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
            int marked = present + absent + late + leave;
            if (marked <= 0) {
                continue;
            }
            double rate = AttendancePolicy.rate(present, absent, late, leave, 0);
            if (rate > threshold) {
                continue;
            }
            if (db.notificationDao().findRecent(ownerId, "LOW_ATTENDANCE", studentId, sinceMonth) != null) {
                continue;
            }
            NotificationEntity notification = new NotificationEntity();
            notification.id = "n_" + UUID.randomUUID();
            notification.ownerUserId = ownerId;
            notification.type = "LOW_ATTENDANCE";
            notification.message = String.format("%s's monthly attendance has dropped to %.0f%%.",
                    student.name, rate);
            notification.relatedStudentId = studentId;
            notification.createdAtMillis = System.currentTimeMillis();
            db.notificationDao().insert(notification);
            created++;
        }
        return created;
    }
}
