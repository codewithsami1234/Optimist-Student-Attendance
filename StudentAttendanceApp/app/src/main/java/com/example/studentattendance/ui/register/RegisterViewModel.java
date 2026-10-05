package com.example.studentattendance.ui.register;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Creates a professor account in local SQLite. The teacher must sign in
 * afterwards; registration does not open the Dashboard.
 */
public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final PreferenceManager preferenceManager;
    private final MutableLiveData<NetworkResult<User>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<User>> googleResult = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = RepositoryProvider.getInstance(application).getAuthRepository();
        preferenceManager = new PreferenceManager(application);
    }

    public LiveData<NetworkResult<User>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<NetworkResult<User>> getGoogleResult() {
        return googleResult;
    }

    public void register(String name, String email, String password, String designation) {
        registerResult.setValue(NetworkResult.loading());
        authRepository.register(name, email, password, designation, new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                preferenceManager.clearSession();
                registerResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                registerResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        googleResult.setValue(NetworkResult.loading());
        authRepository.loginWithGoogle(idToken, new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                if (data != null) {
                    preferenceManager.setRememberMe(true, data.getEmail());
                }
                googleResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                googleResult.setValue(NetworkResult.error(message));
            }
        });
    }
}
