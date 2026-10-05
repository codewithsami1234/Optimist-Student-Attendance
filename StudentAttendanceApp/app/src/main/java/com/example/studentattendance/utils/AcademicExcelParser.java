package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.studentattendance.model.DepartmentShift;
import com.example.studentattendance.model.ImportRowError;

/**
 * Reads a teacher workbook into rows the existing academic tables can store.
 * Roll numbers stay strings. Repeated department, shift, semester, section,
 * and course values are kept so the importer can reuse one structure.
 */
public final class AcademicExcelParser {

    private static final Pattern SEMESTER_DIGITS = Pattern.compile("(\\d+)");

    private AcademicExcelParser() {
    }

    public static final class Row {
        public final int rowNumber;
        public final String departmentName;
        public final String shift;
        public final int semesterNumber;
        public final String sectionName;
        public final String courseName;
        public final String courseCode;
        public final String studentName;
        public final String rollNumber;

        public Row(int rowNumber, String departmentName, String shift, int semesterNumber,
                   String sectionName, String courseName, String courseCode,
                   String studentName, String rollNumber) {
            this.rowNumber = rowNumber;
            this.departmentName = departmentName;
            this.shift = shift;
            this.semesterNumber = semesterNumber;
            this.sectionName = sectionName;
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.studentName = studentName;
            this.rollNumber = rollNumber;
        }
    }

    public static final class Result {
        public String fatalError;
        public int totalRows;
        public int validRows;
        public int invalidRows;
        public final List<ImportRowError> errors = new ArrayList<>();
        public final List<Row> rows = new ArrayList<>();
    }

    public static Result parse(List<List<String>> sheetRows) {
        Result result = new Result();
        if (sheetRows == null || sheetRows.isEmpty()) {
            result.fatalError = "The Excel file has no rows. Add a header row and at least one student.";
            return result;
        }
        int headerIndex = -1;
        int[] columns = null;
        List<String> missing = requiredLabels();
        for (int i = 0; i < sheetRows.size(); i++) {
            int[] candidate = mapColumns(sheetRows.get(i));
            List<String> candidateMissing = missingColumns(candidate);
            if (candidateMissing.size() < missing.size()) {
                missing = candidateMissing;
                columns = candidate;
                headerIndex = i;
            }
            if (candidateMissing.isEmpty()) {
                columns = candidate;
                headerIndex = i;
                break;
            }
        }
        if (columns != null && headerIndex >= 0 && missing.isEmpty()) {
            return parseColumnWise(result, sheetRows, headerIndex, columns);
        }
        if (hasKeyValueRows(sheetRows) || hasStudentTable(sheetRows)) {
            return parseRowWise(result, sheetRows);
        }
        result.fatalError = missingColumnsMessage(missing);
        return result;
    }

    private static Result parseColumnWise(Result result, List<List<String>> sheetRows,
                                          int headerIndex, int[] columns) {
        for (int i = headerIndex + 1; i < sheetRows.size(); i++) {
            List<String> cols = sheetRows.get(i);
            if (isBlank(cols)) {
                continue;
            }
            int rowNumber = i + 1;
            result.totalRows++;
            String department = cell(cols, columns[0]);
            String shiftRaw = cell(cols, columns[1]);
            String semesterRaw = cell(cols, columns[2]);
            String section = cell(cols, columns[3]);
            String courseName = cell(cols, columns[4]);
            String courseCode = cell(cols, columns[5]);
            String studentName = cell(cols, columns[6]);
            String roll = ExcelIdentifiers.normalizeRollNumber(cell(cols, columns[7]));
            String error = validate(department, shiftRaw, semesterRaw, section,
                    courseName, courseCode, studentName, roll);
            if (error != null) {
                result.invalidRows++;
                ImportRowError rowError = new ImportRowError(rowNumber, error);
                rowError.setRollNumber(roll);
                result.errors.add(rowError);
                continue;
            }
            int semesterNumber = parseSemesterNumber(semesterRaw);
            result.rows.add(new Row(
                    rowNumber,
                    department,
                    DepartmentShift.normalize(shiftRaw),
                    semesterNumber,
                    section,
                    courseName,
                    courseCode.trim().toUpperCase(Locale.US),
                    studentName,
                    roll));
            result.validRows++;
        }
        if (result.totalRows == 0) {
            result.fatalError = "The Excel file has no student rows to import.";
        }
        return result;
    }

    /**
     * Field name in one cell, value in a later cell. Academic values carry
     * forward so a repeated block reuses the same class for each student.
     * A later Student Name / Roll No table uses that same class context.
     */
    private static Result parseRowWise(Result result, List<List<String>> sheetRows) {
        RecordContext context = new RecordContext();
        boolean[] seen = new boolean[8];
        int studentNameColumn = -1;
        int rollColumn = -1;
        for (int i = 0; i < sheetRows.size(); i++) {
            List<String> cols = sheetRows.get(i);
            if (isBlank(cols)) {
                continue;
            }
            if (isStudentTableHeader(cols)) {
                int[] mapped = mapColumns(cols);
                studentNameColumn = mapped[6];
                rollColumn = mapped[7];
                seen[6] = true;
                seen[7] = true;
                continue;
            }
            if (isKeyValueRow(cols)) {
                int keyIndex = keyIndex(cols);
                String field = matchField(normalizeHeader(cell(cols, keyIndex)));
                String value = nextValue(cols, keyIndex);
                applySlot(result, context, seen, slotFor(field), value, i + 1);
                continue;
            }
            List<int[]> pairs = academicPairs(cols);
            if (pairs != null) {
                for (int[] pair : pairs) {
                    applySlot(result, context, seen, pair[0], cell(cols, pair[1]), i + 1);
                }
                continue;
            }
            if (studentNameColumn >= 0 && rollColumn >= 0) {
                String studentName = cell(cols, studentNameColumn);
                String roll = cell(cols, rollColumn);
                if (studentName.isEmpty() && roll.isEmpty()) {
                    continue;
                }
                context.set(6, studentName, i + 1);
                context.set(7, roll, i + 1);
                appendRecord(result, context);
            }
        }
        if (context.hasStudentStarted()) {
            appendRecord(result, context);
        }
        if (result.validRows == 0) {
            List<String> neverSeen = missingSlots(seen);
            if (!neverSeen.isEmpty()) {
                result.rows.clear();
                result.errors.clear();
                result.totalRows = 0;
                result.validRows = 0;
                result.invalidRows = 0;
                result.fatalError = missingFieldMessage(neverSeen);
                return result;
            }
        }
        if (result.totalRows == 0) {
            result.fatalError = "The Excel file has no student rows to import.";
        }
        return result;
    }

    private static void appendRecord(Result result, RecordContext context) {
        result.totalRows++;
        String roll = ExcelIdentifiers.normalizeRollNumber(context.roll);
        String error = validate(context.department, context.shift, context.semester, context.section,
                context.courseName, context.courseCode, context.studentName, roll);
        if (error != null) {
            result.invalidRows++;
            ImportRowError rowError = new ImportRowError(context.rowNumber, error);
            rowError.setRollNumber(roll);
            result.errors.add(rowError);
            context.clearStudent();
            return;
        }
        result.rows.add(new Row(
                context.rowNumber,
                context.department,
                DepartmentShift.normalize(context.shift),
                parseSemesterNumber(context.semester),
                context.section,
                context.courseName,
                context.courseCode.trim().toUpperCase(Locale.US),
                context.studentName,
                roll));
        result.validRows++;
        context.clearStudent();
    }

    private static void applySlot(Result result, RecordContext context, boolean[] seen,
                                  int slot, String value, int excelRow) {
        if (slot >= 0) {
            seen[slot] = true;
        }
        if (context.hasCompleteStudent() && context.isFilled(slot)) {
            appendRecord(result, context);
        }
        context.set(slot, value, excelRow);
    }

    private static boolean hasKeyValueRows(List<List<String>> sheetRows) {
        for (List<String> cols : sheetRows) {
            if (isKeyValueRow(cols)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStudentTable(List<List<String>> sheetRows) {
        for (List<String> cols : sheetRows) {
            if (isStudentTableHeader(cols)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Student Name and Roll No headers, with no academic column labels.
     * Academic labels on the same row stay a column-wise sheet.
     */
    private static boolean isStudentTableHeader(List<String> cols) {
        int[] columns = mapColumns(cols);
        if (columns[6] < 0 || columns[7] < 0) {
            return false;
        }
        for (int slot = 0; slot < 6; slot++) {
            if (columns[slot] >= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Several academic labels and their values on one row, such as
     * Department Name | BS IT | Shift | Morning.
     */
    private static List<int[]> academicPairs(List<String> cols) {
        if (cols == null) {
            return null;
        }
        List<Integer> labelIndexes = new ArrayList<>();
        for (int i = 0; i < cols.size(); i++) {
            String field = matchField(normalizeHeader(cell(cols, i)));
            if (field == null) {
                continue;
            }
            int slot = slotFor(field);
            if (slot < 0 || slot >= 6) {
                return null;
            }
            labelIndexes.add(i);
        }
        if (labelIndexes.size() < 2) {
            return null;
        }
        List<int[]> pairs = new ArrayList<>();
        for (int n = 0; n < labelIndexes.size(); n++) {
            int labelIndex = labelIndexes.get(n);
            int start = labelIndex + 1;
            int end = n + 1 < labelIndexes.size() ? labelIndexes.get(n + 1) : cols.size();
            int valueIndex = -1;
            for (int i = start; i < end; i++) {
                if (!cell(cols, i).isEmpty()) {
                    valueIndex = i;
                    break;
                }
            }
            if (valueIndex < 0) {
                return null;
            }
            int slot = slotFor(matchField(normalizeHeader(cell(cols, labelIndex))));
            pairs.add(new int[]{slot, valueIndex});
        }
        return pairs;
    }

    private static boolean isKeyValueRow(List<String> cols) {
        if (cols == null) {
            return false;
        }
        int fieldCells = 0;
        int key = -1;
        for (int i = 0; i < cols.size(); i++) {
            if (matchField(normalizeHeader(cell(cols, i))) != null) {
                fieldCells++;
                if (key < 0) {
                    key = i;
                }
            }
        }
        return fieldCells == 1 && !nextValue(cols, key).isEmpty();
    }

    private static int keyIndex(List<String> cols) {
        for (int i = 0; i < cols.size(); i++) {
            if (matchField(normalizeHeader(cell(cols, i))) != null) {
                return i;
            }
        }
        return -1;
    }

    private static String nextValue(List<String> cols, int keyIndex) {
        for (int i = keyIndex + 1; i < cols.size(); i++) {
            String value = cell(cols, i);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static List<String> missingSlots(boolean[] seen) {
        List<String> missing = new ArrayList<>();
        String[] labels = requiredLabels().toArray(new String[0]);
        for (int i = 0; i < seen.length; i++) {
            if (!seen[i] && isRequiredSlot(i)) {
                missing.add(labels[i]);
            }
        }
        return missing;
    }

    static String missingFieldMessage(List<String> missing) {
        if (missing == null || missing.isEmpty()) {
            return "Missing required field.";
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < missing.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(missing.get(i));
        }
        if (missing.size() == 1) {
            return "Missing required field: " + names;
        }
        return "Missing required fields: " + names
                + ". Required fields are Department Name, Semester, "
                + "Course/Subject Name, Course Code, Student Name, and Roll No.";
    }

    private static final class RecordContext {
        private String department = "";
        private String shift = "";
        private String semester = "";
        private String section = "";
        private String courseName = "";
        private String courseCode = "";
        private String studentName = "";
        private String roll = "";
        private final boolean[] filled = new boolean[8];
        private int rowNumber = 1;

        private boolean hasCompleteStudent() {
            return filled[6] && filled[7];
        }

        private boolean hasStudentStarted() {
            return filled[6] || filled[7];
        }

        private boolean isFilled(int slot) {
            return slot >= 0 && slot < filled.length && filled[slot];
        }

        private void set(int slot, String value, int excelRow) {
            if (slot < 0) {
                return;
            }
            filled[slot] = value != null && !value.trim().isEmpty();
            switch (slot) {
                case 0:
                    department = value;
                    break;
                case 1:
                    shift = value;
                    break;
                case 2:
                    semester = value;
                    break;
                case 3:
                    section = value;
                    break;
                case 4:
                    courseName = value;
                    break;
                case 5:
                    courseCode = value;
                    break;
                case 6:
                    studentName = value;
                    rowNumber = excelRow;
                    break;
                case 7:
                    roll = value;
                    rowNumber = excelRow;
                    break;
                default:
                    break;
            }
        }

        private void clearStudent() {
            studentName = "";
            roll = "";
            filled[6] = false;
            filled[7] = false;
        }
    }

    private static String validate(String department, String shiftRaw, String semesterRaw,
                                   String section, String courseName, String courseCode,
                                   String studentName, String roll) {
        List<String> problems = new ArrayList<>();
        if (department.isEmpty()) {
            problems.add("Department Name is missing.");
        }
        if (semesterRaw == null || semesterRaw.trim().isEmpty()) {
            problems.add("Semester is missing.");
        } else {
            Integer semester = semesterOrNull(semesterRaw);
            if (semester == null || semester < 1 || semester > 8) {
                problems.add("Semester must be a number from 1 to 8.");
            }
        }
        if (courseName.isEmpty()) {
            problems.add("Course/Subject Name is missing.");
        }
        if (courseCode.isEmpty()) {
            problems.add("Course Code is missing.");
        }
        if (studentName.isEmpty()) {
            problems.add("Student Name is missing.");
        }
        if (roll.isEmpty()) {
            problems.add("Roll No is missing.");
        }
        if (problems.isEmpty()) {
            return null;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < problems.size(); i++) {
            if (i > 0) {
                message.append(' ');
            }
            message.append(problems.get(i));
        }
        return message.toString();
    }

    /**
     * Same default as Add Department: a blank or unrecognized shift is Morning.
     */
    static String resolveShift(String raw) {
        return DepartmentShift.normalize(raw);
    }

    static int parseSemesterNumber(String raw) {
        Integer value = semesterOrNull(raw);
        return value == null ? -1 : value;
    }

    private static Integer semesterOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.matches("-?\\d+\\.0+")) {
            value = value.substring(0, value.indexOf('.'));
        }
        if (value.matches("\\d+")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Matcher matcher = SEMESTER_DIGITS.matcher(value);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static int[] mapColumns(List<String> header) {
        int[] columns = new int[]{-1, -1, -1, -1, -1, -1, -1, -1};
        if (header == null) {
            return columns;
        }
        for (int i = 0; i < header.size(); i++) {
            String field = matchField(normalizeHeader(header.get(i)));
            if (field == null) {
                continue;
            }
            int slot = slotFor(field);
            if (slot >= 0 && columns[slot] < 0) {
                columns[slot] = i;
            }
        }
        return columns;
    }

    private static int slotFor(String field) {
        switch (field) {
            case "department":
                return 0;
            case "shift":
                return 1;
            case "semester":
                return 2;
            case "section":
                return 3;
            case "courseName":
                return 4;
            case "courseCode":
                return 5;
            case "studentName":
                return 6;
            case "roll":
                return 7;
            default:
                return -1;
        }
    }

    private static String matchField(String normalized) {
        if (normalized.isEmpty()) {
            return null;
        }
        if (isAny(normalized, "course code", "subject code", "coursecode", "code")) {
            return "courseCode";
        }
        if (isAny(normalized, "course subject name", "course name", "subject name",
                "course subject", "coursename", "subjectname", "course", "subject")) {
            return "courseName";
        }
        if (isAny(normalized, "student name", "studentname", "student", "name")) {
            return "studentName";
        }
        if (isAny(normalized, "department name", "departmentname", "department")) {
            return "department";
        }
        if (isAny(normalized, "roll no", "roll number", "rollno", "rollnumber", "roll")) {
            return "roll";
        }
        if (isAny(normalized, "shift")) {
            return "shift";
        }
        if (isAny(normalized, "semester", "semester number", "semesternumber",
                "semester name", "semestername")) {
            return "semester";
        }
        if (isAny(normalized, "section", "section name", "sectionname")) {
            return "section";
        }
        return null;
    }

    private static boolean isAny(String value, String... options) {
        for (String option : options) {
            if (value.equals(option)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> missingColumns(int[] columns) {
        List<String> missing = new ArrayList<>();
        String[] labels = requiredLabels().toArray(new String[0]);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] < 0 && isRequiredSlot(i)) {
                missing.add(labels[i]);
            }
        }
        return missing;
    }

    private static List<String> requiredLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Department Name");
        labels.add("Shift");
        labels.add("Semester");
        labels.add("Section");
        labels.add("Course/Subject Name");
        labels.add("Course Code");
        labels.add("Student Name");
        labels.add("Roll No");
        return labels;
    }

    /** Matches Add Department: shift and section may be omitted. */
    private static boolean isRequiredSlot(int slot) {
        return slot != 1 && slot != 3;
    }

    static String missingColumnsMessage(List<String> missing) {
        if (missing == null || missing.isEmpty()) {
            return "Missing required column.";
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < missing.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(missing.get(i));
        }
        if (missing.size() == 1) {
            return "Missing required column: " + names;
        }
        return "Missing required columns: " + names
                + ". Required columns are Department Name, Semester, "
                + "Course/Subject Name, Course Code, Student Name, and Roll No.";
    }

    private static String normalizeHeader(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim();
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

    private static String cell(List<String> cols, int index) {
        if (cols == null || index < 0 || index >= cols.size() || cols.get(index) == null) {
            return "";
        }
        return cols.get(index).trim();
    }
}
