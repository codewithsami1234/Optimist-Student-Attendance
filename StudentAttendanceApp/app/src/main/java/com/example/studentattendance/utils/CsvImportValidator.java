package com.example.studentattendance.utils;

import java.util.Locale;

/** Pure CSV import row/header rules, unit-testable without Android. */
public final class CsvImportValidator {

    public static final String REQUIRED_HEADER = "Student Name,Roll Number";

    private CsvImportValidator() {
    }

    public static String validateHeader(String headerLine) {
        if (headerLine == null || headerLine.trim().isEmpty()) {
            return "Header row is missing. Expected: " + REQUIRED_HEADER;
        }
        String lower = headerLine.toLowerCase(Locale.US);
        if (!lower.contains("roll") || !lower.contains("name")) {
            return "Header must include Student Name and Roll Number columns. Expected: " + REQUIRED_HEADER;
        }
        return null;
    }

    public static String validateRow(int rowNumber, String roll, String name) {
        if (roll == null || roll.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return "Row " + rowNumber + ": roll number and name are required.";
        }
        return null;
    }
}
