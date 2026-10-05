package com.example.studentattendance.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.CsvParseResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.utils.AcademicExcelParser;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.AttendanceImportParser;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.CsvImportValidator;
import com.example.studentattendance.utils.ExcelIdentifiers;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.SemesterReport;

public class CsvExcelService implements ExcelService {

    public static final String STUDENT_TEMPLATE_HEADER = "Student Name,Roll Number";

    private final Context appContext;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CsvExcelService(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    @Override
    public void exportAttendance(Uri destinationUri, List<AttendanceSession> sessions, ApiCallback<Integer> callback) {
        exportAttendanceWorkbook(destinationUri, sessions, null, callback);
    }

    @Override
    public void exportSessionDayAttendance(Uri destinationUri, AttendanceSession session,
                                           ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                if (session == null) {
                    postError(callback, "No attendance session was selected for export.");
                    return;
                }
                AttendanceExportRows.SessionDayReport report =
                        AttendanceExportRows.sessionDayReport(session);
                if (report.studentCount <= 0) {
                    postError(callback, "No attendance records were found for this date.");
                    return;
                }
                List<XlsxIo.Sheet> sheets = new ArrayList<>();
                sheets.add(new XlsxIo.Sheet("Attendance", report.rows,
                        report.tableHeaderRowIndex, true));
                XlsxIo.write(out, sheets);
                handler.post(() -> callback.onSuccess(report.studentCount));
            } catch (IOException e) {
                postError(callback, "Failed to export data: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportConsolidatedAttendanceReport(
            Uri destinationUri,
            com.example.studentattendance.utils.AttendanceExportRows.ReportMeta meta,
            List<AttendanceSession> sessions,
            java.util.Map<String, Integer> adjustmentDeltas,
            ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                AttendanceExportRows.ConsolidatedReport report =
                        AttendanceExportRows.consolidatedReport(meta, sessions, adjustmentDeltas);
                if (report.studentCount <= 0) {
                    postError(callback,
                            "No attendance records were found for the selected department, semester, section, course, and date range.");
                    return;
                }
                List<XlsxIo.Sheet> sheets = new ArrayList<>();
                sheets.add(new XlsxIo.Sheet(
                        "Attendance Report",
                        report.rows,
                        report.tableHeaderRowIndex,
                        true));
                XlsxIo.write(out, sheets);
                final int count = report.studentCount;
                handler.post(() -> callback.onSuccess(count));
            } catch (IOException e) {
                postError(callback, "Failed to export data: " + e.getMessage());
            }
        }).start();
    }

    public void exportAttendanceWorkbook(Uri destinationUri, List<AttendanceSession> sessions,
                                         List<StudentReport> reports, ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                AttendanceExportRows.ConsolidatedReport records =
                        AttendanceExportRows.recordsReport(sessions);
                AttendanceExportRows.ConsolidatedReport summary =
                        AttendanceExportRows.summaryReport(sessions);
                List<String[]> summaryRows = summary.rows;
                int summaryHeader = summary.tableHeaderRowIndex;
                if (reports != null) {
                    summaryRows = new ArrayList<>();
                    summaryRows.add(new String[]{"Attendance Summary"});
                    summaryRows.add(new String[]{""});
                    summaryRows.add(new String[]{"#", "Student Name", "Roll No", "Present", "Absent",
                            "Late", "Leave", "Adjustment", "Countable", "Attendance %"});
                    summaryHeader = 2;
                    int serial = 1;
                    for (StudentReport report : reports) {
                        int countable = report.getPresentDays() + report.getLateDays() + report.getAbsentDays();
                        summaryRows.add(new String[]{
                                String.valueOf(serial++),
                                safe(report.getStudentName()),
                                ExcelIdentifiers.normalizeRollNumber(report.getRollNumber()),
                                String.valueOf(report.getPresentDays()),
                                String.valueOf(report.getAbsentDays()),
                                String.valueOf(report.getLateDays()),
                                String.valueOf(report.getLeaveDays()),
                                String.valueOf(report.getAdjustmentDelta()),
                                String.valueOf(countable),
                                String.format(Locale.US, "%.1f%%", report.getAttendancePercentage())
                        });
                    }
                }
                List<XlsxIo.Sheet> sheets = new ArrayList<>();
                sheets.add(new XlsxIo.Sheet("Attendance", records.rows,
                        records.tableHeaderRowIndex, true));
                sheets.add(new XlsxIo.Sheet("Summary", summaryRows, summaryHeader, true));
                XlsxIo.write(out, sheets);
                final int finalCount = records.studentCount;
                handler.post(() -> callback.onSuccess(finalCount));
            } catch (IOException e) {
                postError(callback, "Failed to export data: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportAttendanceSummary(Uri destinationUri, List<StudentReport> reports, ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{"Attendance Summary"});
                rows.add(new String[]{""});
                rows.add(new String[]{
                        "#", "Student Name", "Roll No", "Present", "Absent", "Late", "Leave",
                        "Adjustment", "Countable", "Attendance %"
                });
                int headerIndex = 2;
                if (reports != null) {
                    int serial = 1;
                    for (StudentReport report : reports) {
                        int countable = report.getPresentDays() + report.getLateDays() + report.getAbsentDays();
                        rows.add(new String[]{
                                String.valueOf(serial++),
                                safe(report.getStudentName()),
                                ExcelIdentifiers.normalizeRollNumber(report.getRollNumber()),
                                String.valueOf(report.getPresentDays()),
                                String.valueOf(report.getAbsentDays()),
                                String.valueOf(report.getLateDays()),
                                String.valueOf(report.getLeaveDays()),
                                String.valueOf(report.getAdjustmentDelta()),
                                String.valueOf(countable),
                                String.format(Locale.US, "%.1f%%",
                                        AttendancePolicy.rate(report.getPresentDays(), report.getAbsentDays(),
                                                report.getLateDays(), report.getLeaveDays(), report.getAdjustmentDelta()))
                        });
                    }
                }
                List<XlsxIo.Sheet> sheets = new ArrayList<>();
                sheets.add(new XlsxIo.Sheet("Summary", rows, headerIndex, true));
                XlsxIo.write(out, sheets);
                final int count = reports == null ? 0 : reports.size();
                handler.post(() -> callback.onSuccess(count));
            } catch (IOException e) {
                postError(callback, "Failed to export summary: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportStudents(Uri destinationUri, List<Student> students, ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{"Student Roster"});
                rows.add(new String[]{""});
                rows.add(new String[]{"Student Name", "Roll Number"});
                if (students != null) {
                    for (Student s : students) {
                        rows.add(new String[]{
                                safe(s.getName()),
                                ExcelIdentifiers.normalizeRollNumber(s.getRollNumber())
                        });
                    }
                }
                XlsxIo.write(out, java.util.Collections.singletonList(
                        new XlsxIo.Sheet("Students", rows, 2, true)));
                final int count = students == null ? 0 : students.size();
                handler.post(() -> callback.onSuccess(count));
            } catch (IOException e) {
                postError(callback, "Failed to export students: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void writeStudentTemplate(Uri destinationUri, ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{"Student Import Template"});
                rows.add(new String[]{""});
                rows.add(new String[]{"Student Name", "Roll Number"});
                rows.add(new String[]{"Example Student", "CS-001"});
                XlsxIo.write(out, java.util.Collections.singletonList(
                        new XlsxIo.Sheet("Students", rows, 2, true)));
                handler.post(() -> callback.onSuccess(1));
            } catch (IOException e) {
                postError(callback, "Failed to write template: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportStudentsCsv(Uri destinationUri, List<Student> students, ApiCallback<Integer> callback) {
        new Thread(() -> {
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Student Name", "Roll Number"});
            if (students != null) {
                for (Student s : students) {
                    rows.add(new String[]{safe(s.getName()), safe(s.getRollNumber())});
                }
            }
            writeCsv(destinationUri, rows, students == null ? 0 : students.size(), callback,
                    "Failed to export students: ");
        }).start();
    }

    @Override
    public void writeStudentTemplateCsv(Uri destinationUri, ApiCallback<Integer> callback) {
        new Thread(() -> {
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Student Name", "Roll Number"});
            rows.add(new String[]{"Example Student", "CS-001"});
            writeCsv(destinationUri, rows, 1, callback, "Failed to write template: ");
        }).start();
    }

    private void writeCsv(Uri destinationUri, List<String[]> rows, int successCount,
                          ApiCallback<Integer> callback, String errorPrefix) {
        try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
            if (out == null) {
                postError(callback, "Could not open the selected file for writing.");
                return;
            }
            out.write(toCsv(rows).getBytes(StandardCharsets.UTF_8));
            handler.post(() -> callback.onSuccess(successCount));
        } catch (IOException e) {
            postError(callback, errorPrefix + e.getMessage());
        }
    }

    static String toCsv(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        if (rows != null) {
            for (String[] row : rows) {
                sb.append(csvLine(row)).append('\n');
            }
        }
        return sb.toString();
    }

    static String csvLine(String... cells) {
        StringBuilder line = new StringBuilder();
        if (cells == null) {
            return "";
        }
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(csvEscape(cells[i]));
        }
        return line.toString();
    }

    static String csvEscape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public void parseAcademicImport(Uri sourceUri, ApiCallback<AcademicExcelParser.Result> callback) {
        new Thread(() -> {
            try {
                byte[] bytes = readAll(sourceUri);
                if (bytes == null) {
                    postError(callback, "Could not open the selected file for reading.");
                    return;
                }
                if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xD0 && (bytes[1] & 0xFF) == 0xCF) {
                    postError(callback, "Please choose a .xlsx Excel workbook. Older .xls files are not supported.");
                    return;
                }
                if (!XlsxIo.looksLikeZip(bytes)) {
                    postError(callback, "Please choose a valid Excel workbook (.xlsx).");
                    return;
                }
                List<List<String>> rows;
                try {
                    rows = XlsxIo.readFirstSheet(bytes);
                } catch (Exception e) {
                    postError(callback, "This file is not a valid Excel workbook (.xlsx).");
                    return;
                }
                AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);
                if (result.fatalError != null) {
                    postError(callback, result.fatalError);
                    return;
                }
                handler.post(() -> callback.onSuccess(result));
            } catch (IOException e) {
                postError(callback, "Failed to import Excel: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void parseStudentImport(Uri sourceUri, String departmentId, String semesterId, String sectionId,
                                    ApiCallback<CsvParseResult> callback) {
        new Thread(() -> {
            try {
                byte[] bytes = readAll(sourceUri);
                if (bytes == null) {
                    postError(callback, "Could not open the selected file for reading.");
                    return;
                }
                List<List<String>> rows;
                if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xD0 && (bytes[1] & 0xFF) == 0xCF) {
                    postError(callback, "Please choose a .xlsx Excel workbook. Older .xls files are not supported.");
                    return;
                }
                if (XlsxIo.looksLikeZip(bytes)) {
                    try {
                        rows = XlsxIo.readFirstSheet(bytes);
                    } catch (Exception e) {
                        postError(callback, "This file is not a valid Excel workbook (.xlsx).");
                        return;
                    }
                } else {
                    rows = readCsvRows(new String(bytes, StandardCharsets.UTF_8));
                }
                CsvParseResult result = parseStudentRows(rows, departmentId, semesterId, sectionId);
                if (result.validStudents.isEmpty() && result.errors.isEmpty()) {
                    postError(callback, "No valid rows found. Expected columns: Student Name and Roll Number.");
                    return;
                }
                handler.post(() -> callback.onSuccess(result));
            } catch (IOException e) {
                postError(callback, "Failed to import students: " + e.getMessage());
            }
        }).start();
    }

    static CsvParseResult parseStudentRows(List<List<String>> rows, String departmentId,
                                           String semesterId, String sectionId) {
        CsvParseResult result = new CsvParseResult();
        Set<String> seenRolls = new HashSet<>();
        long counter = System.currentTimeMillis();
        int nameCol = 0;
        int rollCol = 1;
        boolean headerConsumed = false;
        int rowNumber = 0;
        if (rows == null) {
            return result;
        }
        for (List<String> cols : rows) {
            rowNumber++;
            if (isBlankRow(cols)) {
                continue;
            }
            if (!headerConsumed) {
                int detectedName = findColumn(cols, "name");
                int detectedRoll = findColumn(cols, "roll");
                if (detectedName >= 0 && detectedRoll >= 0) {
                    result.headerLine = joinHeader(cols);
                    String headerError = CsvImportValidator.validateHeader(result.headerLine);
                    if (headerError != null) {
                        result.errors.add(headerError);
                        result.invalidCount++;
                    }
                    nameCol = detectedName;
                    rollCol = detectedRoll;
                    headerConsumed = true;
                    continue;
                }
                // Skip professional title/metadata preamble until the real header appears.
                continue;
            }
            String name = cell(cols, nameCol);
            String roll = ExcelIdentifiers.normalizeRollNumber(cell(cols, rollCol));
            if (name.isEmpty() && roll.isEmpty()) {
                continue;
            }
            if (looksLikeRoll(name) && looksLikeName(roll) && !looksLikeRoll(roll)) {
                String swapped = name;
                name = roll;
                roll = ExcelIdentifiers.normalizeRollNumber(swapped);
            }
            String rowError = CsvImportValidator.validateRow(rowNumber, roll, name);
            if (rowError != null) {
                result.invalidCount++;
                result.errors.add(rowError);
                continue;
            }
            String rollKey = roll.toLowerCase(Locale.US);
            if (seenRolls.contains(rollKey)) {
                result.duplicateCount++;
                result.errors.add("Row " + rowNumber + ": duplicate roll number " + roll + " in this file.");
                continue;
            }
            seenRolls.add(rollKey);
            result.validStudents.add(new Student(
                    "st_import_" + (counter++),
                    roll, name, departmentId, semesterId, sectionId, "", ""));
        }
        return result;
    }

    private static boolean isBlankRow(List<String> cols) {
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

    private static String joinHeader(List<String> cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cell(cols, i));
        }
        return sb.toString();
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

    private static boolean looksLikeRoll(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeName(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private CsvParseResult parseRows(List<List<String>> rows, String departmentId, String semesterId, String sectionId) {
        return parseStudentRows(rows, departmentId, semesterId, sectionId);
    }

    private List<List<String>> readCsvRows(String text) {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] cols = parseCsvLine(line);
                List<String> row = new ArrayList<>();
                java.util.Collections.addAll(row, cols);
                rows.add(row);
            }
        } catch (IOException ignored) {
            return rows;
        }
        return rows;
    }

    private byte[] readAll(Uri uri) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private void postError(ApiCallback<?> callback, String message) {
        handler.post(() -> callback.onError(message));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }

    @Override
    public void parseAttendanceImport(Uri sourceUri, ApiCallback<AttendanceImportParser.Result> callback) {
        new Thread(() -> {
            try {
                byte[] bytes = readAll(sourceUri);
                if (bytes == null) {
                    postError(callback, "Could not open the selected file for reading.");
                    return;
                }
                if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xD0 && (bytes[1] & 0xFF) == 0xCF) {
                    postError(callback, "Please choose a .xlsx Excel workbook. Older .xls files are not supported.");
                    return;
                }
                if (!XlsxIo.looksLikeZip(bytes)) {
                    postError(callback, "Please choose a valid Excel workbook (.xlsx).");
                    return;
                }
                List<List<String>> rows;
                try {
                    rows = XlsxIo.readFirstSheet(bytes);
                } catch (Exception e) {
                    postError(callback, "This file is not a valid Excel workbook (.xlsx).");
                    return;
                }
                AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
                handler.post(() -> callback.onSuccess(result));
            } catch (IOException e) {
                postError(callback, "Failed to import attendance: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportCustomSheet(Uri destinationUri, String sheetName, List<String[]> rows,
                                  ApiCallback<Integer> callback) {
        new Thread(() -> {
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                List<String[]> safeRows = rows == null ? new ArrayList<>() : rows;
                XlsxIo.write(out, java.util.Collections.singletonList(
                        new XlsxIo.Sheet(sheetName == null || sheetName.isEmpty() ? "Attendance" : sheetName,
                                safeRows)));
                handler.post(() -> callback.onSuccess(Math.max(0, safeRows.size() - 1)));
            } catch (IOException e) {
                postError(callback, "Failed to export data: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void exportSemesterReport(Uri destinationUri, SemesterReport.Snapshot snapshot,
                                     HistoryFilter filter, ApiCallback<Integer> callback) {
        new Thread(() -> {
            if (snapshot == null || !snapshot.hasVisibleStudents) {
                postError(callback, "No Data to Export. Please adjust the report filters and try again.");
                return;
            }
            try (OutputStream out = appContext.getContentResolver().openOutputStream(destinationUri)) {
                if (out == null) {
                    postError(callback, "Could not open the selected file for writing.");
                    return;
                }
                List<XlsxIo.Sheet> sheets = new ArrayList<>();
                List<String[]> summaryRows = SemesterReport.summarySheet(snapshot, filter);
                sheets.add(new XlsxIo.Sheet("Semester Summary", summaryRows, -1, true));
                SemesterReport.SheetBuild studentSummary =
                        SemesterReport.studentSummaryReport(snapshot);
                sheets.add(new XlsxIo.Sheet("Student Summary", studentSummary.rows,
                        studentSummary.tableHeaderRowIndex, true));
                if (snapshot != null) {
                    int index = 0;
                    for (SemesterReport.CourseGroup group : snapshot.courses) {
                        if (group == null || group.students.isEmpty()) {
                            continue;
                        }
                        String name = SemesterReport.sheetNameFor(group, index);
                        String unique = name;
                        int suffix = 2;
                        while (containsSheet(sheets, unique)) {
                            unique = name + " " + suffix;
                            suffix++;
                        }
                        SemesterReport.SheetBuild matrix = SemesterReport.dateMatrixReport(group);
                        sheets.add(new XlsxIo.Sheet(unique, matrix.rows,
                                matrix.tableHeaderRowIndex, true));
                        index++;
                    }
                }
                XlsxIo.write(out, sheets);
                int count = snapshot == null ? 0 : snapshot.visibleRows.size();
                handler.post(() -> callback.onSuccess(count));
            } catch (IOException e) {
                postError(callback, "Failed to export data: " + e.getMessage());
            }
        }).start();
    }

    private static boolean containsSheet(List<XlsxIo.Sheet> sheets, String name) {
        for (XlsxIo.Sheet sheet : sheets) {
            if (sheet.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
