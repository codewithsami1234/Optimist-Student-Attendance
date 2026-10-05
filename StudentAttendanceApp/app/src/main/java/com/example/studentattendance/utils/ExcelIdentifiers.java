package com.example.studentattendance.utils;

/**
 * Helpers for Excel identifier cells (especially roll numbers). Roll numbers are
 * strings, never mathematical numbers — avoid storing {@code 123.0}.
 */
public final class ExcelIdentifiers {

    private ExcelIdentifiers() {
    }

    /**
     * Normalizes a roll-number cell value read from Excel/CSV.
     * <ul>
     *   <li>{@code 123.0} / {@code 123.00} → {@code 123} (Excel numeric formatting)</li>
     *   <li>{@code 00123}, {@code BSIT-001}, {@code 23-IT-045} → unchanged</li>
     *   <li>{@code 123.5} → unchanged (not a whole-number Excel artifact)</li>
     * </ul>
     */
    public static String normalizeRollNumber(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return "";
        }
        // Excel often stores whole numbers as 123.0 in the OOXML <v> node.
        if (value.matches("-?\\d+\\.0+")) {
            int dot = value.indexOf('.');
            return value.substring(0, dot);
        }
        return value;
    }
}
