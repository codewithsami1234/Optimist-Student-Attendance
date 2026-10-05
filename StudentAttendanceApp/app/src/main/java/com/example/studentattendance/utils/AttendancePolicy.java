package com.example.studentattendance.utils;

import com.example.studentattendance.model.AttendanceStatus;

/**
 * Single attendance-percentage policy used by dashboard, reports, student
 * detail, section stats, trends, low-attendance alerts, adjustments, and
 * exports. Keep this in lock-step with
 * {@code com.studentattendance.backend.domain.AttendancePolicy}.
 * <p>
 * PRESENT + LATE = attended; ABSENT counts against; LEAVE is excused
 * (excluded from the rate); adjustments add to the attended count.
 */
public final class AttendancePolicy {

    public static final double DEFAULT_LOW_THRESHOLD = 75.0;

    private AttendancePolicy() {
    }

    public static boolean isPresentLike(AttendanceStatus status) {
        return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
    }

    public static boolean countsTowardTotal(AttendanceStatus status) {
        return status == AttendanceStatus.PRESENT
                || status == AttendanceStatus.LATE
                || status == AttendanceStatus.ABSENT;
    }

    public static double rate(int present, int absent, int late, int leave, int adjustmentDelta) {
        int presentLike = present + late + adjustmentDelta;
        int total = present + late + absent;
        if (total <= 0) {
            return 0;
        }
        return clamp((presentLike * 100.0) / total);
    }

    public static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
