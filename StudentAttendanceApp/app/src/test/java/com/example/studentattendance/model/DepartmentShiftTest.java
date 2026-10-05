package com.example.studentattendance.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DepartmentShiftTest {

    @Test
    public void normalizeDefaultsBlankToMorning() {
        assertEquals(DepartmentShift.MORNING, DepartmentShift.normalize(null));
        assertEquals(DepartmentShift.MORNING, DepartmentShift.normalize(""));
        assertEquals(DepartmentShift.MORNING, DepartmentShift.normalize("morning"));
    }

    @Test
    public void normalizeAcceptsEveningLabels() {
        assertEquals(DepartmentShift.EVENING, DepartmentShift.normalize("Evening"));
        assertEquals(DepartmentShift.EVENING, DepartmentShift.normalize("EVENING"));
    }

    @Test
    public void displayLabelDistinguishesShifts() {
        Department morning = new Department("1", "BS IT", "BITM", 0, null, DepartmentShift.MORNING);
        Department evening = new Department("2", "BS IT", "BITE", 0, null, DepartmentShift.EVENING);
        assertEquals("BS IT — Morning", morning.displayLabel());
        assertEquals("BS IT — Evening", evening.displayLabel());
        assertFalse(morning.getId().equals(evening.getId()));
        assertTrue(DepartmentShift.isEvening(evening.getShift()));
    }
}
