package com.studentattendance.backend.domain;

import com.studentattendance.backend.entity.AttendanceStatus;

/**
 * Single attendance-percentage policy used by dashboard, reports, student
 * detail, section stats, trends, low-attendance alerts, adjustments, and
 * exports. Keep this in lock-step with
 * {@code com.example.studentattendance.utils.AttendancePolicy} on Android.
 * <p>
 * Rules:
 * <ul>
 *   <li>PRESENT and LATE count as attended (present-like).</li>
 *   <li>ABSENT counts against the student (in the denominator, not present-like).</li>
 *   <li>LEAVE is excused: excluded from both numerator and denominator.</li>
 *   <li>NOT_MARKED is ignored (should never be persisted).</li>
 *   <li>Teacher adjustments add to the present-like count (may be negative).</li>
 *   <li>Rate is clamped to 0–100. If there are no countable days, rate is 0.</li>
 * </ul>
 */
public final class AttendancePolicy {

    public static final double DEFAULT_LOW_THRESHOLD = 75.0;
    public static final double MIN_THRESHOLD = 1.0;
    public static final double MAX_THRESHOLD = 100.0;

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

    public static double clamp(double rate) {
        return Math.max(0, Math.min(100, rate));
    }

    public static double sanitizeThreshold(Double threshold) {
        if (threshold == null || Double.isNaN(threshold) || Double.isInfinite(threshold)) {
            return DEFAULT_LOW_THRESHOLD;
        }
        return Math.max(MIN_THRESHOLD, Math.min(MAX_THRESHOLD, threshold));
    }
}
