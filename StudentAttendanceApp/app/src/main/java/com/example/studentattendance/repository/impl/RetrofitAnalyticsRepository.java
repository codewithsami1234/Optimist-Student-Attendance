package com.example.studentattendance.repository.impl;

import java.util.List;

import android.content.Context;

import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.SectionStats;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.repository.AnalyticsRepository;

public class RetrofitAnalyticsRepository implements AnalyticsRepository {

    private final Context appContext;

    public RetrofitAnalyticsRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void getDashboardStats(ApiCallback<DashboardStats> callback) {
        RetrofitExec.enqueue(api().getDashboardStats(), callback);
    }

    @Override
    public void getSectionStats(String departmentIdOrNull, String semesterIdOrNull, String subjectIdOrNull,
                                 ApiCallback<List<SectionStats>> callback) {
        RetrofitExec.enqueue(api().getSectionStats(departmentIdOrNull, semesterIdOrNull, subjectIdOrNull), callback);
    }

    @Override
    public void getTrend(String departmentIdOrNull, String subjectIdOrNull, int days,
                          ApiCallback<List<DailyTrend>> callback) {
        RetrofitExec.enqueue(api().getTrend(departmentIdOrNull, subjectIdOrNull, days), callback);
    }

    @Override
    public void getLowAttendance(Double threshold, int limit, ApiCallback<List<StudentAttendanceSummary>> callback) {
        RetrofitExec.enqueue(api().getLowAttendance(threshold, limit), callback);
    }

    @Override
    public void getStudentReport(String studentId, ApiCallback<StudentAttendanceReport> callback) {
        RetrofitExec.enqueue(api().getStudentReport(studentId), callback);
    }
}
