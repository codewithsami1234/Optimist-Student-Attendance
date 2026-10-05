package com.example.studentattendance.repository;

import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.network.ApiCallback;

public interface AttendanceRepository {

    void saveSession(String departmentId, String departmentName, String semesterId,
                      String semesterName, String sectionId, String sectionName,
                      String subjectId, String subjectName, String date, int lectureNumber,
                      Map<String, AttendanceStatus> studentIdToStatus,
                      Map<String, String> studentIdToName,
                      Map<String, String> studentIdToRoll,
                      ApiCallback<AttendanceSession> callback);

    void getSession(String departmentId, String semesterId, String sectionId, String subjectId,
                     String date, int lectureNumber,
                     ApiCallback<AttendanceSession> callback);

    void getSessions(String departmentIdOrNull, String semesterIdOrNull, String sectionIdOrNull,
                      String subjectIdOrNull, String fromDateOrNull, String toDateOrNull,
                      ApiCallback<List<AttendanceSession>> callback);

    void getSessionsForReports(ApiCallback<List<AttendanceSession>> callback);

    default void importAttendance(String departmentId, String semesterId, String sectionId,
                                  String subjectId,
                                  java.util.List<com.example.studentattendance.utils.AttendanceImportParser.Row> rows,
                                  ApiCallback<com.example.studentattendance.model.ImportResult> callback) {
        callback.onError("Attendance import is not available.");
    }
}
