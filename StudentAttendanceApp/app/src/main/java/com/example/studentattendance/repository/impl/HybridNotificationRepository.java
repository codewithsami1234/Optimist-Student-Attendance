package com.example.studentattendance.repository.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.NotificationRepository;
import com.example.studentattendance.sync.AppNotificationHelper;
import com.example.studentattendance.sync.HybridRead;
import com.example.studentattendance.sync.HybridWrite;
import com.example.studentattendance.sync.PendingOp;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.RoomCacheStore;
import com.example.studentattendance.sync.SyncOpType;
import com.example.studentattendance.utils.PreferenceManager;

public class HybridNotificationRepository implements NotificationRepository {

    private final Context appContext;
    private final RetrofitNotificationRepository remote;
    private final RoomNotificationRepository local;
    private final RoomCacheStore cache;
    private final PendingSyncStore store;

    public HybridNotificationRepository(Context context, PreferenceManager prefs, AppDatabase db,
                                         RoomCacheStore cache, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitNotificationRepository(appContext);
        this.local = new RoomNotificationRepository(prefs, db);
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void getNotifications(ApiCallback<List<Notification>> callback) {
        HybridRead.fetch(appContext, store, remote::getNotifications, local::getNotifications,
                list -> {
                    cache.upsertNotifications(list);
                    AppNotificationHelper.publishUnread(appContext, list, store);
                },
                false, callback);
    }

    @Override
    public void getUnreadCount(ApiCallback<Long> callback) {
        HybridRead.fetch(appContext, store, remote::getUnreadCount, local::getUnreadCount,
                ignored -> { }, false, false, callback);
    }

    @Override
    public void markRead(String notificationId, ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                cb -> remote.markRead(notificationId, cb),
                cb -> local.markRead(notificationId, cb),
                () -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", notificationId);
                    store.enqueue(PendingOp.create(SyncOpType.MARK_NOTIFICATION_READ, null, payload));
                },
                callback);
    }

    @Override
    public void markAllRead(ApiCallback<Void> callback) {
        HybridWrite.mutateVoid(appContext,
                remote::markAllRead,
                local::markAllRead,
                () -> store.enqueue(PendingOp.create(SyncOpType.MARK_ALL_NOTIFICATIONS_READ, null, new LinkedHashMap<>())),
                callback);
    }
}
