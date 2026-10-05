package com.example.studentattendance;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.sync.AppNotificationHelper;
import com.example.studentattendance.sync.SyncScheduler;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Process-wide entry point. Theme, the Room repository graph, notification
 * channel, and WorkManager low-attendance checks are initialized here.
 */
public class AttendanceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager prefs = new PreferenceManager(this);
        AppCompatDelegate.setDefaultNightMode(prefs.isDarkModeEnabled()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        try {
            RepositoryProvider.getInstance(this);
        } catch (RuntimeException ignored) {
            // Login still opens; Room is created again on first screen that needs it.
        }
        AppNotificationHelper.ensureChannel(this);
        SyncScheduler.schedule(this);
    }
}
