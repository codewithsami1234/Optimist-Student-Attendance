package com.example.studentattendance.repository.impl;

import java.util.List;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.SectionStats;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AnalyticsRepository;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridAnalyticsRepository implements AnalyticsRepository {

    private final Context appContext;
    private final RetrofitAnalyticsRepository remote;
    private final RoomAnalyticsRepository local;
    private final PendingSyncStore store;

    public HybridAnalyticsRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                      PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitAnalyticsRepository(appContext);
        this.local = new RoomAnalyticsRepository(prefs, db);
        this.store = store;
    }

    @Override
    public void getDashboardStats(ApiCallback<DashboardStats> callback) {
        HybridRead.fetch(appContext, store, remote::getDashboardStats, local::getDashboardStats,
                ignored -> { }, false, callback);
    }

    @Override
    public void getSectionStats(String departmentIdOrNull, String semesterIdOrNull, String subjectIdOrNull,
                                 ApiCallback<List<SectionStats>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getSectionStats(store.resolveId(departmentIdOrNull),
                        store.resolveId(semesterIdOrNull), store.resolveId(subjectIdOrNull), cb),
                cb -> local.getSectionStats(departmentIdOrNull, semesterIdOrNull, subjectIdOrNull, cb),
                ignored -> { }, false, callback);
    }

    @Override
    public void getTrend(String departmentIdOrNull, String subjectIdOrNull, int days,
                          ApiCallback<List<DailyTrend>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getTrend(store.resolveId(departmentIdOrNull),
                        store.resolveId(subjectIdOrNull), days, cb),
                cb -> local.getTrend(departmentIdOrNull, subjectIdOrNull, days, cb),
                ignored -> { }, false, callback);
    }

    @Override
    public void getLowAttendance(Double threshold, int limit, ApiCallback<List<StudentAttendanceSummary>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getLowAttendance(threshold, limit, cb),
                cb -> local.getLowAttendance(threshold, limit, cb),
                ignored -> { }, false, callback);
    }

    @Override
    public void getStudentReport(String studentId, ApiCallback<StudentAttendanceReport> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getStudentReport(store.resolveId(studentId), cb),
                cb -> local.getStudentReport(studentId, cb),
                ignored -> { }, false, callback);
    }
}
