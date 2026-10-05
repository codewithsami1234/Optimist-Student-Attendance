package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.NotificationRepository;
import com.example.studentattendance.utils.PreferenceManager;

public class RoomNotificationRepository implements NotificationRepository {

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomNotificationRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void getNotifications(ApiCallback<List<Notification>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<Notification> result = new ArrayList<>();
            for (NotificationEntity entity : db.notificationDao().list(ownerId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void getUnreadCount(ApiCallback<Long> callback) {
        LocalExec.io(() -> db.notificationDao().unreadCount(LocalOwner.requireId(prefs)), callback);
    }

    @Override
    public void markRead(String notificationId, ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> db.notificationDao().markRead(notificationId, LocalOwner.requireId(prefs)), callback);
    }

    @Override
    public void markAllRead(ApiCallback<Void> callback) {
        LocalExec.ioVoid(() -> db.notificationDao().markAllRead(LocalOwner.requireId(prefs)), callback);
    }
}
