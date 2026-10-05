package com.example.studentattendance.repository;

import java.util.List;

import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.ApiCallback;

public interface NotificationRepository {
    void getNotifications(ApiCallback<List<Notification>> callback);
    void getUnreadCount(ApiCallback<Long> callback);
    void markRead(String notificationId, ApiCallback<Void> callback);
    void markAllRead(ApiCallback<Void> callback);
}
