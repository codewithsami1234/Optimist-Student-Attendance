package com.example.studentattendance.sync;

import android.content.Context;

import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.network.ApiCallback;

public final class HybridRead {

    public interface CacheWriter<T> {
        void write(T data);
    }

    private HybridRead() {
    }

    public static <T> void fetch(Context context,
                                  PendingSyncStore store,
                                  RemoteCall<T> remote,
                                  LocalCall<T> local,
                                  CacheWriter<T> cacheWriter,
                                  boolean nullIsValid,
                                  ApiCallback<T> callback) {
        fetch(context, store, remote, local, cacheWriter, nullIsValid, true, callback);
    }

    public static <T> void fetch(Context context,
                                  PendingSyncStore store,
                                  RemoteCall<T> remote,
                                  LocalCall<T> local,
                                  CacheWriter<T> cacheWriter,
                                  boolean nullIsValid,
                                  boolean notifyStale,
                                  ApiCallback<T> callback) {
        remote.enqueue(new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                LocalExec.io(() -> {
                    if (cacheWriter != null && data != null) {
                        cacheWriter.write(data);
                    }
                    store.markPullSuccess();
                    return data;
                }, callback);
            }

            @Override
            public void onRetryableError(String message) {
                local.enqueue(new ApiCallback<T>() {
                    @Override
                    public void onSuccess(T data) {
                        boolean usable = nullIsValid
                                ? HybridPolicy.isUsableCache(data, true) || data == null
                                : HybridPolicy.isUsableCache(data, store.hasCompletedPull());
                        if (usable) {
                            if (notifyStale && (!nullIsValid || data != null)) {
                                StaleDataNotifier.show(context, StaleDataNotifier.STALE_CACHE);
                            }
                            callback.onCached(data, StaleDataNotifier.STALE_CACHE);
                        } else {
                            callback.onError(message);
                        }
                    }

                    @Override
                    public void onError(String ignored) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public interface RemoteCall<T> {
        void enqueue(ApiCallback<T> callback);
    }

    public interface LocalCall<T> {
        void enqueue(ApiCallback<T> callback);
    }
}
