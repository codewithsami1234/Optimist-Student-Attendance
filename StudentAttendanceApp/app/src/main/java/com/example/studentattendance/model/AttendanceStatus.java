package com.example.studentattendance.model;

/**
 * Enumerates the possible attendance states for a student on a given date.
 */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    LEAVE("Leave"),
    NOT_MARKED("Not marked");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AttendanceStatus fromLabel(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.US);
        if ("present".equals(normalized) || "p".equals(normalized)) {
            return PRESENT;
        }
        if ("absent".equals(normalized) || "a".equals(normalized)) {
            return ABSENT;
        }
        if ("late".equals(normalized) || "l".equals(normalized)) {
            return LATE;
        }
        if ("leave".equals(normalized) || "lv".equals(normalized)) {
            return LEAVE;
        }
        return null;
    }

    /** One tap cycles Present → Absent → Late → Leave → Present. */
    public AttendanceStatus nextMarked() {
        switch (this) {
            case PRESENT:
                return ABSENT;
            case ABSENT:
                return LATE;
            case LATE:
                return LEAVE;
            case LEAVE:
            case NOT_MARKED:
            default:
                return PRESENT;
        }
    }
}
