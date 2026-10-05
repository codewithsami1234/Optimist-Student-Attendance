package com.studentattendance.backend;

import com.studentattendance.backend.domain.AttendancePolicy;
import com.studentattendance.backend.entity.AttendanceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendancePolicyTest {

    @Test
    void presentAndLateCountAsAttendedLeaveIsExcused() {
        assertEquals(75.0, AttendancePolicy.rate(2, 1, 1, 1, 0), 0.001);
        assertTrue(AttendancePolicy.isPresentLike(AttendanceStatus.PRESENT));
        assertTrue(AttendancePolicy.isPresentLike(AttendanceStatus.LATE));
        assertFalse(AttendancePolicy.countsTowardTotal(AttendanceStatus.LEAVE));
    }

    @Test
    void zeroCountableDaysIsZero() {
        assertEquals(0.0, AttendancePolicy.rate(0, 0, 0, 4, 0), 0.001);
    }

    @Test
    void adjustmentsClampBetweenZeroAndOneHundred() {
        assertEquals(100.0, AttendancePolicy.rate(1, 1, 0, 0, 1), 0.001);
        assertEquals(0.0, AttendancePolicy.rate(0, 2, 0, 0, -9), 0.001);
    }

    @Test
    void thresholdFallsBackToDefault() {
        assertEquals(75.0, AttendancePolicy.sanitizeThreshold(null), 0.001);
        assertEquals(1.0, AttendancePolicy.sanitizeThreshold(0.0), 0.001);
        assertEquals(100.0, AttendancePolicy.sanitizeThreshold(140.0), 0.001);
    }
}
