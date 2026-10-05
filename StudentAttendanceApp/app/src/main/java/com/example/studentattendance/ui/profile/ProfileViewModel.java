package com.example.studentattendance.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.PreferenceManager;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final PreferenceManager preferenceManager;
    private final MutableLiveData<NetworkResult<User>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<Boolean>> resetResult = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = RepositoryProvider.getInstance(application).getAuthRepository();
        preferenceManager = new PreferenceManager(application);
    }

    public User getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<NetworkResult<User>> getUpdateResult() {
        return updateResult;
    }

    public void updateProfile(String name, String designation) {
        User user = authRepository.getCurrentUser();
        if (user == null) {
            return;
        }
        user.setName(name);
        user.setDesignation(designation);
        persistProfile(user);
    }

    public void updateThreshold(double threshold) {
        User user = authRepository.getCurrentUser();
        if (user == null) {
            return;
        }
        if (threshold < 1) {
            threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;
        }
        user.setLowAttendanceThreshold(Math.max(1, Math.min(100, threshold)));
        persistProfile(user);
    }

    private void persistProfile(User user) {
        updateResult.setValue(NetworkResult.loading());
        authRepository.updateProfile(user, new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                updateResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                updateResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public boolean isDarkModeEnabled() {
        return preferenceManager.isDarkModeEnabled();
    }

    public void setDarkModeEnabled(boolean enabled) {
        preferenceManager.setDarkModeEnabled(enabled);
        AppCompatDelegate.setDefaultNightMode(enabled
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public boolean areNotificationsEnabled() {
        return preferenceManager.areNotificationsEnabled();
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferenceManager.setNotificationsEnabled(enabled);
    }

    public String getApiBaseUrl() {
        return preferenceManager.getApiBaseUrl();
    }

    public boolean setApiBaseUrl(String url) {
        return preferenceManager.setApiBaseUrl(url);
    }

    public LiveData<NetworkResult<Boolean>> getResetResult() {
        return resetResult;
    }

    public void resetAcademicData() {
        resetResult.setValue(NetworkResult.loading());
        com.example.studentattendance.data.local.LocalExec.ioVoid(() -> {
            com.example.studentattendance.data.local.AppDatabase db =
                    com.example.studentattendance.data.local.AppDatabase.getInstance(getApplication());
            String ownerId = com.example.studentattendance.data.local.LocalOwner.requireId(preferenceManager);
            com.example.studentattendance.data.local.AcademicDataReset.execute(db, ownerId);
        }, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                resetResult.setValue(NetworkResult.success(Boolean.TRUE));
            }

            @Override
            public void onError(String message) {
                resetResult.setValue(NetworkResult.error(message));
            }
        });
    }
}
