package com.example.studentattendance.ui.login;

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
import com.example.studentattendance.utils.AuthSession;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Holds login form state so screen rotations don't lose in-flight work or
 * error messages. Remember Me only pre-fills the email. A Room account
 * never skips Sign-In; a session is created only after successful login.
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final PreferenceManager preferenceManager;
    private final MutableLiveData<NetworkResult<User>> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = RepositoryProvider.getInstance(application).getAuthRepository();
        preferenceManager = new PreferenceManager(application);
    }

    public LiveData<NetworkResult<User>> getLoginResult() {
        return loginResult;
    }

    public boolean isAlreadyLoggedIn() {
        return AuthSession.shouldRestoreAfterRestart(authRepository.isLoggedIn());
    }

    public boolean isRememberMe() {
        return preferenceManager.isRememberMe();
    }

    public String getRememberedEmail() {
        return preferenceManager.getRememberedEmail();
    }

    public void discardUnrememberedSession() {
        // Session restore is based on a stored local login, not Remember Me.
    }

    public void login(String email, String password, boolean rememberMe) {
        loginResult.setValue(NetworkResult.loading());
        authRepository.login(email, password, new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                preferenceManager.setRememberMe(rememberMe, email);
                loginResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                loginResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loginWithGoogle(String idToken, boolean rememberMe) {
        loginResult.setValue(NetworkResult.loading());
        authRepository.loginWithGoogle(idToken, new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                String email = data != null ? data.getEmail() : "";
                preferenceManager.setRememberMe(rememberMe, email);
                loginResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                loginResult.setValue(NetworkResult.error(message));
            }
        });
    }
}
