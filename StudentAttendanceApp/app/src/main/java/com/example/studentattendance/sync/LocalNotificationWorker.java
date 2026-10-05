package com.example.studentattendance.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentattendance.utils.PreferenceManager;

/**
 * Posts unread in-app notifications to the system tray. Does not scan the
 * roster for low attendance.
 */
public class LocalNotificationWorker extends Worker {

    public LocalNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context app = getApplicationContext();
            PreferenceManager prefs = new PreferenceManager(app);
            if (!prefs.isLoggedIn()) {
                return Result.success();
            }
            LocalNotificationPublisher.publishUnreadNow(app);
            return Result.success();
        } catch (Exception ignored) {
            return Result.retry();
        }
    }
}
