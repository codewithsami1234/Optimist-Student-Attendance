package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.network.dto.AttendanceRecordInputDto;
import com.example.studentattendance.network.dto.AttendanceSaveRequestDto;
import com.example.studentattendance.repository.AttendanceRepository;

public class RetrofitAttendanceRepository implements AttendanceRepository {

    private final Context appContext;

    public RetrofitAttendanceRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void saveSession(String departmentId, String departmentName, String semesterId,
                             String semesterName, String sectionId, String sectionName,
                             String subjectId, String subjectName, String date, int lectureNumber,
                             Map<String, AttendanceStatus> studentIdToStatus,
                             Map<String, String> studentIdToName,
                             Map<String, String> studentIdToRoll,
                             ApiCallback<AttendanceSession> callback) {
        if (studentIdToStatus == null || studentIdToStatus.isEmpty()) {
            callback.onError("There are no students to save attendance for.");
            return;
        }
        for (AttendanceStatus status : studentIdToStatus.values()) {
            if (status == null || status == AttendanceStatus.NOT_MARKED) {
                callback.onError("Please mark attendance for every student before saving.");
                return;
            }
        }
        List<AttendanceRecordInputDto> records = new ArrayList<>();
        for (Map.Entry<String, AttendanceStatus> entry : studentIdToStatus.entrySet()) {
            String studentId = entry.getKey();
            records.add(new AttendanceRecordInputDto(
                    studentId,
                    studentIdToName.get(studentId),
                    studentIdToRoll.get(studentId),
                    entry.getValue()));
        }
        AttendanceSaveRequestDto request = new AttendanceSaveRequestDto(
                departmentId, departmentName, semesterId, semesterName, sectionId, sectionName,
                subjectId, subjectName, date, records);
        RetrofitExec.enqueue(api().saveAttendanceSession(request), callback);
    }

    @Override
    public void getSession(String departmentId, String semesterId, String sectionId, String subjectId,
                             String date, int lectureNumber,
                             ApiCallback<AttendanceSession> callback) {
        RetrofitExec.enqueue(api().getAttendanceSession(departmentId, semesterId, sectionId, date, subjectId), callback);
    }

    @Override
    public void getSessions(String departmentIdOrNull, String semesterIdOrNull, String sectionIdOrNull,
                             String subjectIdOrNull, String fromDateOrNull, String toDateOrNull,
                             ApiCallback<List<AttendanceSession>> callback) {
        RetrofitExec.enqueue(api().getAttendanceSessions(departmentIdOrNull, semesterIdOrNull, sectionIdOrNull,
                subjectIdOrNull, fromDateOrNull, toDateOrNull, 0, 200), callback);
    }

    @Override
    public void getSessionsForReports(ApiCallback<List<AttendanceSession>> callback) {
        RetrofitExec.enqueue(api().getSessionsForReports(), callback);
    }
}
