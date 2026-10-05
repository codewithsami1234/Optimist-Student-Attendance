package com.example.studentattendance.sync;

import android.content.Context;

import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.network.ApiCallback;

public final class HybridWrite {

    public interface CacheWriter<T> {
        void write(T data);
    }

    public interface EnqueueLocal<T> {
        void enqueue(T localResult);
    }

    private HybridWrite() {
    }

    public static <T> void mutate(Context context,
                                   RemoteCall<T> remote,
                                   LocalCall<T> local,
                                   CacheWriter<T> cacheRemote,
                                   EnqueueLocal<T> enqueueLocal,
                                   ApiCallback<T> callback) {
        remote.enqueue(new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                LocalExec.io(() -> {
                    if (cacheRemote != null && data != null) {
                        cacheRemote.write(data);
                    }
                    return data;
                }, new ApiCallback<T>() {
                    @Override
                    public void onSuccess(T cached) {
                        SyncScheduler.enqueueNow(context);
                        callback.onSuccess(cached);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onSuccess(data);
                    }
                });
            }

            @Override
            public void onRetryableError(String message) {
                local.enqueue(new ApiCallback<T>() {
                    @Override
                    public void onSuccess(T data) {
                        if (enqueueLocal != null) {
                            enqueueLocal.enqueue(data);
                        }
                        SyncScheduler.enqueueNow(context);
                        StaleDataNotifier.show(context, StaleDataNotifier.QUEUED_WRITE);
                        callback.onCached(data, StaleDataNotifier.QUEUED_WRITE);
                    }

                    @Override
                    public void onError(String localMessage) {
                        callback.onError(localMessage);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void mutateVoid(Context context,
                                   RemoteCall<Void> remote,
                                   LocalCall<Void> local,
                                   Runnable enqueueLocal,
                                   ApiCallback<Void> callback) {
        mutate(context, remote, local, data -> { }, data -> enqueueLocal.run(), callback);
    }

    public interface RemoteCall<T> {
        void enqueue(ApiCallback<T> callback);
    }

    public interface LocalCall<T> {
        void enqueue(ApiCallback<T> callback);
    }
}
