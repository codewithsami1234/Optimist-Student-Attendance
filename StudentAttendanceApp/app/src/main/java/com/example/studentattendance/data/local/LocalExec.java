package com.example.studentattendance.data.local;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import com.example.studentattendance.network.ApiCallback;

/**
 * Runs Room work off the main thread and delivers callbacks on the UI thread,
 * matching the existing ViewModel / {@link ApiCallback} contract.
 */
public final class LocalExec {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private LocalExec() {
    }

    public static <T> void io(Callable<T> work, ApiCallback<T> callback) {
        IO.execute(() -> {
            try {
                T result = work.call();
                MAIN.post(() -> callback.onSuccess(result));
            } catch (LocalDataException e) {
                MAIN.post(() -> callback.onError(e.getMessage()));
            } catch (Exception e) {
                MAIN.post(() -> callback.onError("Something went wrong. Please try again."));
            }
        });
    }

    public static void ioVoid(Runnable work, ApiCallback<Void> callback) {
        io(() -> {
            work.run();
            return null;
        }, callback);
    }
}
