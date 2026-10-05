package com.example.studentattendance.repository.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.HybridWrite;
import com.example.studentattendance.sync.PendingOp;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.RoomCacheStore;
import com.example.studentattendance.sync.SyncOpType;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridAttendanceAdjustmentRepository implements AttendanceAdjustmentRepository {

    private final Context appContext;
    private final RetrofitAttendanceAdjustmentRepository remote;
    private final RoomAttendanceAdjustmentRepository local;
    private final RoomCacheStore cache;
    private final PendingSyncStore store;

    public HybridAttendanceAdjustmentRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                                 RoomCacheStore cache, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitAttendanceAdjustmentRepository(appContext);
        this.local = new RoomAttendanceAdjustmentRepository(prefs, db);
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void createAdjustment(String studentId, int delta, String reason,
                                  ApiCallback<AttendanceAdjustment> callback) {
        HybridWrite.mutate(appContext,
                cb -> remote.createAdjustment(store.resolveId(studentId), delta, reason, cb),
                cb -> local.createAdjustment(studentId, delta, reason, cb),
                cache::upsertAdjustment,
                ignored -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("studentId", studentId);
                    payload.put("delta", delta);
                    payload.put("reason", reason);
                    store.enqueue(PendingOp.create(SyncOpType.CREATE_ADJUSTMENT, null, payload));
                },
                callback);
    }

    @Override
    public void getHistory(String studentId, ApiCallback<List<AttendanceAdjustment>> callback) {
        HybridRead.fetch(appContext, store,
                cb -> remote.getHistory(store.resolveId(studentId), cb),
                cb -> local.getHistory(studentId, cb),
                list -> {
                    if (list == null) {
                        return;
                    }
                    for (AttendanceAdjustment adjustment : list) {
                        cache.upsertAdjustment(adjustment);
                    }
                },
                false, callback);
    }
}
