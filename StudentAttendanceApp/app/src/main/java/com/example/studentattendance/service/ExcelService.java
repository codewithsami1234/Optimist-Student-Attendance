package com.example.studentattendance.service;

import android.net.Uri;

import java.util.List;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.CsvParseResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.network.ApiCallback;

public interface ExcelService {

    void exportAttendance(Uri destinationUri, List<AttendanceSession> sessions, ApiCallback<Integer> callback);

    /**
     * Exports one specific attendance session/day without percentage columns
     * or a summary sheet. Used by Session Detail → Export Excel only.
     */
    default void exportSessionDayAttendance(Uri destinationUri, AttendanceSession session,
                                            ApiCallback<Integer> callback) {
        callback.onError("Excel export is not available.");
    }

    void exportAttendanceSummary(Uri destinationUri, List<StudentReport> reports, ApiCallback<Integer> callback);

    default void exportConsolidatedAttendanceReport(
            Uri destinationUri,
            com.example.studentattendance.utils.AttendanceExportRows.ReportMeta meta,
            List<AttendanceSession> sessions,
            java.util.Map<String, Integer> adjustmentDeltas,
            ApiCallback<Integer> callback) {
        callback.onError("Excel export is not available.");
    }

    void exportStudents(Uri destinationUri, List<Student> students, ApiCallback<Integer> callback);

    void writeStudentTemplate(Uri destinationUri, ApiCallback<Integer> callback);

    void exportStudentsCsv(Uri destinationUri, List<Student> students, ApiCallback<Integer> callback);

    void writeStudentTemplateCsv(Uri destinationUri, ApiCallback<Integer> callback);

    void parseStudentImport(Uri sourceUri, String departmentId, String semesterId, String sectionId,
                             ApiCallback<CsvParseResult> callback);

    default void parseAcademicImport(Uri sourceUri,
                                     ApiCallback<com.example.studentattendance.utils.AcademicExcelParser.Result> callback) {
        callback.onError("Excel import is not available.");
    }

    default void parseAttendanceImport(Uri sourceUri,
                                       ApiCallback<com.example.studentattendance.utils.AttendanceImportParser.Result> callback) {
        callback.onError("Attendance import is not available.");
    }

    default void exportCustomSheet(Uri destinationUri, String sheetName, List<String[]> rows,
                                   ApiCallback<Integer> callback) {
        callback.onError("Excel export is not available.");
    }

    default void exportSemesterReport(Uri destinationUri,
                                      com.example.studentattendance.utils.SemesterReport.Snapshot snapshot,
                                      com.example.studentattendance.utils.HistoryFilter filter,
                                      ApiCallback<Integer> callback) {
        callback.onError("Excel export is not available.");
    }
}
