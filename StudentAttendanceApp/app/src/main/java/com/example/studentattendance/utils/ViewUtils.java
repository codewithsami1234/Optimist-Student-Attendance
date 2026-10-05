package com.example.studentattendance.utils;

import android.view.View;
import android.widget.Toast;
import android.content.Context;

/**
 * Small helpers for toggling loading/empty/error/content view states, used
 * identically across every list-based screen (Departments, Students,
 * Attendance History, Reports).
 */
public final class ViewUtils {

    private ViewUtils() {
    }

    public static void showOnly(View toShow, View... others) {
        if (toShow != null) {
            toShow.setVisibility(View.VISIBLE);
        }
        for (View v : others) {
            if (v != null && v != toShow) {
                v.setVisibility(View.GONE);
            }
        }
    }

    public static void toast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /** Alias for {@link #toast} - used by newer (Phase 4) screens for naming symmetry with {@link #toastLong}. */
    public static void toastShort(Context context, String message) {
        toast(context, message);
    }

    public static void toastLong(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
