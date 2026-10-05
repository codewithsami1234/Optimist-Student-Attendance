package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.studentattendance.model.AttendanceStatus;

/**
 * Validates attendance Excel rows before any Room write. Matching is by
 * roll number inside a known class, never by student name alone.
 */
public final class AttendanceImportParser {

    public static final class Row {
        public int rowNumber;
        public String rollNumber;
        public String studentName;
        public String date;
        public AttendanceStatus status;
        public String courseCode;
    }

    public static final class Result {
        public final List<Row> validRows = new ArrayList<>();
        public int emptyCount;
        public int invalidCount;
        public int duplicateCount;
        public final List<String> errors = new ArrayList<>();
    }

    private AttendanceImportParser() {
    }

    public static Result parse(List<List<String>> rows) {
        Result result = new Result();
        if (rows == null || rows.isEmpty()) {
            result.errors.add("The workbook is empty.");
            result.invalidCount++;
            return result;
        }
        int headerIndex = -1;
        int rollCol = -1;
        int nameCol = -1;
        int dateCol = -1;
        int statusCol = -1;
        int codeCol = -1;
        for (int i = 0; i < rows.size(); i++) {
            List<String> cols = rows.get(i);
            if (isBlank(cols)) {
                continue;
            }
            rollCol = findColumn(cols, "roll");
            dateCol = findColumn(cols, "date");
            statusCol = findColumn(cols, "status");
            nameCol = findColumn(cols, "name");
            codeCol = findColumn(cols, "code");
            if (codeCol < 0) {
                codeCol = findColumn(cols, "course");
            }
            if (rollCol >= 0 && dateCol >= 0 && statusCol >= 0) {
                headerIndex = i;
                break;
            }
        }
        if (headerIndex < 0) {
            result.errors.add("Missing required columns. Expected Roll Number, Date, and Status.");
            result.invalidCount++;
            return result;
        }
        Set<String> seen = new HashSet<>();
        for (int i = headerIndex + 1; i < rows.size(); i++) {
            List<String> cols = rows.get(i);
            int rowNumber = i + 1;
            if (isBlank(cols)) {
                result.emptyCount++;
                continue;
            }
            String roll = ExcelIdentifiers.normalizeRollNumber(cell(cols, rollCol));
            String date = cell(cols, dateCol);
            String statusText = cell(cols, statusCol);
            String name = cell(cols, nameCol);
            String code = cell(cols, codeCol);
            if (roll.isEmpty() && date.isEmpty() && statusText.isEmpty()) {
                result.emptyCount++;
                continue;
            }
            AttendanceStatus status = AttendanceStatus.fromLabel(statusText);
            if (roll.isEmpty()) {
                result.invalidCount++;
                result.errors.add("Row " + rowNumber + ": missing roll number.");
                continue;
            }
            if (!isIsoDate(date)) {
                result.invalidCount++;
                result.errors.add("Row " + rowNumber + ": invalid date " + date + ".");
                continue;
            }
            if (status == null || status == AttendanceStatus.NOT_MARKED) {
                result.invalidCount++;
                result.errors.add("Row " + rowNumber + ": invalid attendance status.");
                continue;
            }
            String key = roll.toLowerCase(Locale.US) + "|" + date;
            if (seen.contains(key)) {
                result.duplicateCount++;
                result.errors.add("Row " + rowNumber + ": duplicate roll " + roll + " on " + date + ".");
                continue;
            }
            seen.add(key);
            Row row = new Row();
            row.rowNumber = rowNumber;
            row.rollNumber = roll;
            row.studentName = name;
            row.date = date;
            row.status = status;
            row.courseCode = HistoryFilter.normalizeCode(code);
            result.validRows.add(row);
        }
        return result;
    }

    private static boolean isIsoDate(String value) {
        if (value == null || value.length() != 10) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (i == 4 || i == 7) {
                if (ch != '-') {
                    return false;
                }
            } else if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlank(List<String> cols) {
        if (cols == null || cols.isEmpty()) {
            return true;
        }
        for (String col : cols) {
            if (col != null && !col.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static int findColumn(List<String> cols, String token) {
        for (int i = 0; i < cols.size(); i++) {
            String value = cell(cols, i).toLowerCase(Locale.US);
            if (value.contains(token)) {
                return i;
            }
        }
        return -1;
    }

    private static String cell(List<String> cols, int index) {
        if (cols == null || index < 0 || index >= cols.size() || cols.get(index) == null) {
            return "";
        }
        return cols.get(index).trim();
    }
}
