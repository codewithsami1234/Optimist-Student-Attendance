package com.example.studentattendance.sync;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefsKvStore implements KvStore {

    private final SharedPreferences prefs;

    public PrefsKvStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences("attendance_sync_queue", Context.MODE_PRIVATE);
    }

    @Override
    public String get(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    @Override
    public void put(String key, String value) {
        if (value == null) {
            prefs.edit().remove(key).apply();
        } else {
            prefs.edit().putString(key, value).apply();
        }
    }
}
