package com.example.studentattendance.ui.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.NotificationRepository;
import com.example.studentattendance.service.RepositoryProvider;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository;
    private final MutableLiveData<NetworkResult<List<Notification>>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Long> unreadCount = new MutableLiveData<>(0L);

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = RepositoryProvider.getInstance(application).getNotificationRepository();
    }

    public LiveData<NetworkResult<List<Notification>>> getNotifications() {
        return notifications;
    }

    public LiveData<Long> getUnreadCount() {
        return unreadCount;
    }

    public void load() {
        notifications.setValue(NetworkResult.loading());
        notificationRepository.getNotifications(new ApiCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                notifications.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                notifications.setValue(NetworkResult.error(message));
            }
        });
        refreshUnreadCount();
    }

    public void refreshUnreadCount() {
        notificationRepository.getUnreadCount(new ApiCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                unreadCount.setValue(data == null ? 0L : data);
            }

            @Override
            public void onError(String message) {
                unreadCount.setValue(0L);
            }
        });
    }

    public void markRead(String notificationId) {
        notificationRepository.markRead(notificationId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                load();
            }

            @Override
            public void onError(String message) {
                notifications.setValue(NetworkResult.error(message));
            }
        });
    }

    public void markAllRead() {
        notificationRepository.markAllRead(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                load();
            }

            @Override
            public void onError(String message) {
                notifications.setValue(NetworkResult.error(message));
            }
        });
    }
}
