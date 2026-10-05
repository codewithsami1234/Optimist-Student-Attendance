package com.example.studentattendance.repository.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.HybridWrite;
import com.example.studentattendance.sync.PendingOp;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.RoomCacheStore;
import com.example.studentattendance.sync.SyncOpType;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridAttendanceRepository implements AttendanceRepository {

    private final Context appContext;
    private final RetrofitAttendanceRepository remote;
    private final RoomAttendanceRepository local;
    private final RoomCacheStore cache;
    private final PendingSyncStore store;

    public HybridAttendanceRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                       RoomCacheStore cache, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitAttendanceRepository(appContext);
        this.local = new RoomAttendanceRepository(prefs, db);
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void saveSession(String departmentId, String departmentName, String semesterId,
                             String semesterName, String sectionId, String sectionName,
                             String subjectId, String subjectName, String date, int lectureNumber,
                             Map<String, AttendanceStatus> studentIdToStatus,
                             Map<String, String> studentIdToName,
                             Map<String, String> studentIdToRoll,
                             ApiCallback<AttendanceSession> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.saveSession(
                        store.resolveId(departmentId), departmentName,
                        store.resolveId(semesterId), semesterName,
                        store.resolveId(sectionId), sectionName,
                        store.resolveId(subjectId), subjectName, date, lectureNumber,
                        resolveStatusMap(studentIdToStatus), studentIdToName, studentIdToRoll, cb),
                cb -> local.saveSession(departmentId, departmentName, semesterId, semesterName,
                        sectionId, sectionName, subjectId, subjectName, date, lectureNumber,
                        studentIdToStatus, studentIdToName, studentIdToRoll, cb),
                cache::upsertSession,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("departmentId", departmentId);
                    payload.put("departmentName", departmentName);
                    payload.put("semesterId", semesterId);
                    payload.put("semesterName", semesterName);
                    payload.put("sectionId", sectionId);
                    payload.put("sectionName", sectionName);
                    payload.put("subjectId", subjectId);
                    payload.put("subjectName", subjectName);
                    payload.put("date", date);
                    payload.put("statusByStudentId", stringifyStatuses(studentIdToStatus));
                    payload.put("nameByStudentId", studentIdToName);
                    payload.put("rollByStudentId", studentIdToRoll);
                    store.enqueue(PendingOp.create(SyncOpType.SAVE_ATTENDANCE, null, payload));
                },
                callback);
    }

    @Override
    public void getSession(String departmentId, String semesterId, String sectionId, String subjectId,
                             String date, int lectureNumber,
                             ApiCallback<AttendanceSession> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getSession(store.resolveId(departmentId), store.resolveId(semesterId),
                        store.resolveId(sectionId), store.resolveId(subjectId), date, lectureNumber, cb),
                cb -> local.getSession(departmentId, semesterId, sectionId, subjectId, date, lectureNumber, cb),
                cache::upsertSession,
                true, callback);
    }

    @Override
    public void getSessions(String departmentIdOrNull, String semesterIdOrNull, String sectionIdOrNull,
                             String subjectIdOrNull, String fromDateOrNull, String toDateOrNull,
                             ApiCallback<List<AttendanceSession>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getSessions(store.resolveId(departmentIdOrNull), store.resolveId(semesterIdOrNull),
                        store.resolveId(sectionIdOrNull), store.resolveId(subjectIdOrNull),
                        fromDateOrNull, toDateOrNull, cb),
                cb -> local.getSessions(departmentIdOrNull, semesterIdOrNull, sectionIdOrNull,
                        subjectIdOrNull, fromDateOrNull, toDateOrNull, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (AttendanceSession session : list) {
                        cache.upsertSession(session);
                    }
                },
                false, callback);
    }

    @Override
    public void getSessionsForReports(ApiCallback<List<AttendanceSession>> callback) {
        HybridRead.fetch(appContext, store, remote::getSessionsForReports, local::getSessionsForReports,
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (AttendanceSession session : list) {
                        cache.upsertSession(session);
                    }
                },
                false, callback);
    }

    private Map<String, AttendanceStatus> resolveStatusMap(Map<String, AttendanceStatus> source) {
        if (source == null) {
            return null;
        }
        Map<String, AttendanceStatus> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, AttendanceStatus> entry : source.entrySet()) {
            resolved.put(store.resolveId(entry.getKey()), entry.getValue());
        }
        return resolved;
    }

    private Map<String, String> stringifyStatuses(Map<String, AttendanceStatus> source) {
        Map<String, String> out = new LinkedHashMap<>();
        if (source == null) {
            return out;
        }
        for (Map.Entry<String, AttendanceStatus> entry : source.entrySet()) {
            out.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().name());
        }
        return out;
    }
}
