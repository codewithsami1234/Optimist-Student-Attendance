package com.example.studentattendance.network;

/**
 * Generic asynchronous callback used by every repository interface.
 * Mock repository implementations invoke this on a background-simulated
 * delay via {@link android.os.Handler}; a real Retrofit-backed implementation
 * would invoke it from its own network callback thread hop. UI code is
 * therefore fully decoupled from the transport mechanism.
 *
 * @param <T> result payload type.
 */
public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(String message);

    /**
     * Network failed but a local cache (or queued write) is being shown.
     * Default keeps older callers working; hybrid repositories use this so
     * the UI can surface a stale/offline warning instead of failing silently.
     */
    default void onCached(T data, String warning) {
        onSuccess(data);
    }

    /**
     * Transport / 5xx failure that is safe to retry or serve from cache.
     * Business 4xx errors must keep calling {@link #onError(String)}.
     */
    default void onRetryableError(String message) {
        onError(message);
    }
}
