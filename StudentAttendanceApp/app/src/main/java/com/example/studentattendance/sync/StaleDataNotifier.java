package com.example.studentattendance.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Debounced toast so a screen that fires several hybrid reads does not spam
 * the teacher, while still making cache fallback visible.
 */
public final class StaleDataNotifier {

    public static final String STALE_CACHE =
            "Couldn't reach the server. Showing last saved data.";
    public static final String QUEUED_WRITE =
            "Saved on this device. It will sync when you're back online.";

    private static final long DEBOUNCE_MS = 4000L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static long lastShownAt;
    private static String lastMessage;

    private StaleDataNotifier() {
    }

    public static void show(Context context, String message) {
        if (context == null || message == null || message.isEmpty()) {
            return;
        }
        MAIN.post(() -> {
            long now = System.currentTimeMillis();
            if (message.equals(lastMessage) && now - lastShownAt < DEBOUNCE_MS) {
                return;
            }
            lastMessage = message;
            lastShownAt = now;
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        });
    }
}
