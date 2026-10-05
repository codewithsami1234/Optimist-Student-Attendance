package com.example.studentattendance.repository;

import java.util.List;

import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.SectionStats;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.network.ApiCallback;

public interface AnalyticsRepository {

    void getDashboardStats(ApiCallback<DashboardStats> callback);

    void getSectionStats(String departmentIdOrNull, String semesterIdOrNull, String subjectIdOrNull,
                         ApiCallback<List<SectionStats>> callback);

    void getTrend(String departmentIdOrNull, String subjectIdOrNull, int days, ApiCallback<List<DailyTrend>> callback);

    default void getScopedTrend(String sectionIdOrNull, String studentIdOrNull, int days,
                                ApiCallback<List<DailyTrend>> callback) {
        getTrend(null, null, days, callback);
    }

    void getLowAttendance(Double threshold, int limit, ApiCallback<List<StudentAttendanceSummary>> callback);

    void getStudentReport(String studentId, ApiCallback<StudentAttendanceReport> callback);
}
