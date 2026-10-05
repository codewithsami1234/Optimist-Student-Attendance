package com.example.studentattendance.sync;

/** Tiny key/value contract so the sync queue can be unit-tested without SharedPreferences. */
public interface KvStore {
    String get(String key, String defaultValue);
    void put(String key, String value);
}
