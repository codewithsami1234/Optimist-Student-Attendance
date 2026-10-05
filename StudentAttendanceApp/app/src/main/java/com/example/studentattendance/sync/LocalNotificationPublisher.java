package com.example.studentattendance.sync;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Posts unread Room notification rows through {@link AppNotificationHelper}
 * so alerts appear in the system tray even when the process was killed.
 */
public final class LocalNotificationPublisher {

    private LocalNotificationPublisher() {
    }

    public static void publishUnreadNowAsync(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        new Thread(() -> publishUnreadNow(app), "attendance-notify").start();
    }

    public static void publishUnreadNow(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        PreferenceManager prefs = new PreferenceManager(app);
        if (!prefs.isLoggedIn() || prefs.getSavedUser() == null) {
            return;
        }
        String ownerId = prefs.getSavedUser().getId();
        if (ownerId == null || ownerId.isEmpty()) {
            return;
        }
        try {
            AppDatabase db = AppDatabase.getInstance(app);
            List<Notification> unread = new ArrayList<>();
            for (NotificationEntity entity : db.notificationDao().list(ownerId)) {
                if (!entity.read) {
                    unread.add(entity.toModel());
                }
            }
            AppNotificationHelper.publishUnread(app, unread, new PendingSyncStore(new PrefsKvStore(app)));
        } catch (Exception ignored) {
            // Never crash a save/login path because a tray notification failed.
        }
    }
}
