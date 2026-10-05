package com.example.studentattendance.sync;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Posts unread Room notifications to the system tray. Periodic
 * low-attendance scanning is not scheduled.
 */
public final class SyncScheduler {

    public static final String UNIQUE_PERIODIC = "cloud-sync-periodic";
    public static final String UNIQUE_IMMEDIATE = "cloud-sync-now";
    public static final String UNIQUE_NOTIFY_PERIODIC = "local-notification-periodic";
    public static final String UNIQUE_NOTIFY_NOW = "local-notification-now";

    private SyncScheduler() {
    }

    public static void schedule(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        wm.cancelUniqueWork(UNIQUE_PERIODIC);
        wm.cancelUniqueWork(UNIQUE_IMMEDIATE);
        wm.cancelUniqueWork(UNIQUE_NOTIFY_PERIODIC);
    }

    public static void enqueueNow(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        OneTimeWorkRequest notifyNow = new OneTimeWorkRequest.Builder(LocalNotificationWorker.class).build();
        wm.enqueueUniqueWork(UNIQUE_NOTIFY_NOW, ExistingWorkPolicy.REPLACE, notifyNow);
    }
}
