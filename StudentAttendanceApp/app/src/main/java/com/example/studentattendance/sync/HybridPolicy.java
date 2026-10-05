package com.example.studentattendance.sync;

import java.util.List;

import com.example.studentattendance.network.ApiCallback;

public final class HybridPolicy {

    private HybridPolicy() {
    }

    public static boolean isUsableCache(Object data, boolean hasCompletedPull) {
        if (data == null) {
            return false;
        }
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            return !list.isEmpty() || hasCompletedPull;
        }
        return true;
    }

    public static <T> void deliverCache(ApiCallback<T> callback, T data, String warning) {
        callback.onCached(data, warning);
    }
}
