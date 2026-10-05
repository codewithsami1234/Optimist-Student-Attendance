package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.studentattendance.model.AttendanceStatus;

public class AttendancePolicyTest {

    @Test
    public void presentAndLateCountAsAttended_leaveIsExcused() {
        // 2 present + 1 late + 1 absent + 1 leave → attended 3 / countable 4 = 75
        assertEquals(75.0, AttendancePolicy.rate(2, 1, 1, 1, 0), 0.001);
        assertTrue(AttendancePolicy.isPresentLike(AttendanceStatus.PRESENT));
        assertTrue(AttendancePolicy.isPresentLike(AttendanceStatus.LATE));
        assertFalse(AttendancePolicy.isPresentLike(AttendanceStatus.LEAVE));
        assertFalse(AttendancePolicy.countsTowardTotal(AttendanceStatus.LEAVE));
    }

    @Test
    public void zeroCountableDaysIsZeroNotNaN() {
        assertEquals(0.0, AttendancePolicy.rate(0, 0, 0, 5, 0), 0.001);
    }

    @Test
    public void tapCyclesStatusOrder() {
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.PRESENT.nextMarked());
        assertEquals(AttendanceStatus.LATE, AttendanceStatus.ABSENT.nextMarked());
        assertEquals(AttendanceStatus.LEAVE, AttendanceStatus.LATE.nextMarked());
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.LEAVE.nextMarked());
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.NOT_MARKED.nextMarked());
    }
}
