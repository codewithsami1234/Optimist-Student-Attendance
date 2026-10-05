package com.example.studentattendance.sync;

import java.util.HashMap;
import java.util.Map;

public final class MemoryKvStore implements KvStore {

    private final Map<String, String> values = new HashMap<>();

    @Override
    public String get(String key, String defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public void put(String key, String value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }
}
