package com.example.studentattendance.sync;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.ui.notifications.NotificationsActivity;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * System-tray alerts for low-attendance / adjustment events. FCM is not
 * required: WorkManager polls the API (or Room cache) and posts through
 * {@link NotificationChannel}.
 */
public final class AppNotificationHelper {

    public static final String CHANNEL_ID = "attendance_alerts";
    private static final String CHANNEL_NAME = "Attendance alerts";

    private AppNotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Low-attendance warnings and attendance adjustments.");
        manager.createNotificationChannel(channel);
    }

    public static boolean canPost(Context context) {
        PreferenceManager prefs = new PreferenceManager(context);
        if (!prefs.areNotificationsEnabled()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void publishUnread(Context context, List<Notification> notifications, PendingSyncStore store) {
        if (notifications == null || !canPost(context)) {
            return;
        }
        ensureChannel(context);
        for (Notification notification : notifications) {
            if (notification == null || notification.isRead() || notification.getId() == null) {
                continue;
            }
            if (store.postedNotificationIds().contains(notification.getId())) {
                continue;
            }
            post(context, notification);
            store.addPostedNotificationId(notification.getId());
        }
    }

    private static void post(Context context, Notification notification) {
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notification.getId().hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title;
        if ("LOW_ATTENDANCE".equals(notification.getType())) {
            title = context.getString(R.string.notification_title_low);
        } else if ("ATTENDANCE_RECORDED".equals(notification.getType())) {
            title = context.getString(R.string.notification_title_attendance);
        } else {
            title = context.getString(R.string.notification_title_update);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_attendance)
                .setContentTitle(title)
                .setContentText(notification.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(notification.getId().hashCode(), builder.build());
    }
}
