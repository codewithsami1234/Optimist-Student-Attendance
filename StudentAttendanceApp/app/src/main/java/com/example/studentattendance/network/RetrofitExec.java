package com.example.studentattendance.network;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Shared Retrofit enqueue so repositories don't duplicate success/error plumbing. */
public final class RetrofitExec {

    private RetrofitExec() {
    }

    public static <T> void enqueue(@NonNull Call<T> call, @NonNull ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> ignored, @NonNull Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else if (isRetryableHttp(response.code())) {
                    callback.onRetryableError(ApiErrorParser.parse(response));
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> ignored, @NonNull Throwable t) {
                callback.onRetryableError(ApiErrorParser.networkFailureMessage(t));
            }
        });
    }

    public static boolean isRetryableHttp(int code) {
        return code == 408 || code == 429 || code >= 500;
    }
}
