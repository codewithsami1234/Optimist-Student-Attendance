package com.example.studentattendance.repository.impl;

import java.util.List;
import java.util.Map;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiErrorParser;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.repository.NotificationRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitNotificationRepository implements NotificationRepository {

    private final Context appContext;

    public RetrofitNotificationRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void getNotifications(ApiCallback<List<Notification>> callback) {
        RetrofitExec.enqueue(api().getNotifications(0, 50), callback);
    }

    @Override
    public void getUnreadCount(ApiCallback<Long> callback) {
        api().getUnreadNotificationCount().enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Long>> call,
                                    @NonNull Response<Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long count = response.body().get("unreadCount");
                    callback.onSuccess(count == null ? 0L : count);
                } else if (response.isSuccessful()) {
                    callback.onSuccess(0L);
                } else if (RetrofitExec.isRetryableHttp(response.code())) {
                    callback.onRetryableError(ApiErrorParser.parse(response));
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Long>> call, @NonNull Throwable t) {
                callback.onRetryableError(ApiErrorParser.networkFailureMessage(t));
            }
        });
    }

    @Override
    public void markRead(String notificationId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().markNotificationRead(notificationId), callback);
    }

    @Override
    public void markAllRead(ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().markAllNotificationsRead(), callback);
    }
}
