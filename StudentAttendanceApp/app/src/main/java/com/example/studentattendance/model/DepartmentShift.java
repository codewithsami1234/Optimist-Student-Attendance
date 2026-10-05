package com.example.studentattendance.model;

/**
 * Department shift (Morning / Evening). Stored as stable codes; labels are for UI.
 */
public final class DepartmentShift {

    public static final String MORNING = "MORNING";
    public static final String EVENING = "EVENING";

    private DepartmentShift() {
    }

    public static String normalize(String shift) {
        if (shift == null) {
            return MORNING;
        }
        String value = shift.trim();
        if (EVENING.equalsIgnoreCase(value) || "evening".equalsIgnoreCase(value)) {
            return EVENING;
        }
        return MORNING;
    }

    public static String label(String shift) {
        return EVENING.equals(normalize(shift)) ? "Evening" : "Morning";
    }

    public static boolean isEvening(String shift) {
        return EVENING.equals(normalize(shift));
    }
}
