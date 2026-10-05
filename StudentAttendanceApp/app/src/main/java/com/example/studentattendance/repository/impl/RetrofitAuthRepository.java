package com.example.studentattendance.repository.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiErrorParser;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.dto.AuthResponseDto;
import com.example.studentattendance.network.dto.GoogleAuthRequestDto;
import com.example.studentattendance.network.dto.LoginRequestDto;
import com.example.studentattendance.network.dto.ProfileUpdateRequestDto;
import com.example.studentattendance.network.dto.RegisterRequestDto;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Optional Retrofit-backed auth against the Spring API. The live app uses
 * {@link RoomAuthRepository} so register and login work without a network.
 */
public class RetrofitAuthRepository implements AuthRepository {

    private final Context appContext;
    private final PreferenceManager preferenceManager;
    private User currentUser;

    public RetrofitAuthRepository(Context context, PreferenceManager preferenceManager) {
        this.appContext = context.getApplicationContext();
        this.preferenceManager = preferenceManager;
        this.currentUser = preferenceManager.getSavedUser();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    private void persistSession(AuthResponseDto body, ApiCallback<User> callback) {
        User user = body.getUser();
        currentUser = user;
        preferenceManager.saveUser(user);
        preferenceManager.saveAuthToken(body.getToken());
        preferenceManager.setLoggedIn(true);
        callback.onSuccess(user);
    }

    @Override
    public void login(String email, String password, ApiCallback<User> callback) {
        if (!ValidationUtils.hasInternet(appContext)) {
            callback.onError("No internet connection. Connect to Wi‑Fi or mobile data and try again.");
            return;
        }
        if (email == null || password == null) {
            callback.onError("Please enter both email and password.");
            return;
        }
        api().login(new LoginRequestDto(email.trim(), password)).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    persistSession(response.body(), callback);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                callback.onError(ApiErrorParser.networkFailureMessage(t));
            }
        });
    }

    @Override
    public void register(String name, String email, String password, String designation, ApiCallback<User> callback) {
        if (!ValidationUtils.hasInternet(appContext)) {
            callback.onError("No internet connection. Connect to Wi‑Fi or mobile data and try again.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Please enter your name.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("Please enter a valid email address.");
            return;
        }
        if (!ValidationUtils.isValidNewPassword(password)) {
            callback.onError("Password must be at least 8 characters and include a letter and a number.");
            return;
        }
        api().register(new RegisterRequestDto(name.trim(), email.trim(), password, designation))
                .enqueue(new Callback<AuthResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            persistSession(response.body(), callback);
                        } else {
                            callback.onError(ApiErrorParser.parse(response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                        callback.onError(ApiErrorParser.networkFailureMessage(t));
                    }
                });
    }

    @Override
    public void loginWithGoogle(String idToken, ApiCallback<User> callback) {
        if (!ValidationUtils.hasInternet(appContext)) {
            callback.onError("No internet connection. Connect to Wi‑Fi or mobile data and try again.");
            return;
        }
        if (idToken == null || idToken.isEmpty()) {
            callback.onError("Google Sign-In did not return an ID token.");
            return;
        }
        api().loginWithGoogle(new GoogleAuthRequestDto(idToken)).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    persistSession(response.body(), callback);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                callback.onError(ApiErrorParser.networkFailureMessage(t));
            }
        });
    }

    @Override
    public void logout() {
        currentUser = null;
        preferenceManager.setLoggedIn(false);
        preferenceManager.clearUser();
        // Fire logout while the token is still stored so AuthInterceptor can
        // attach it and the server can blacklist the JWT. Clear the token
        // after the call completes (or fails).
        api().logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                preferenceManager.clearAuthToken();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                preferenceManager.clearAuthToken();
            }
        });
    }

    @Override
    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = preferenceManager.getSavedUser();
        }
        return currentUser;
    }

    @Override
    public boolean isLoggedIn() {
        return preferenceManager.isLoggedIn() && getCurrentUser() != null
                && preferenceManager.getAuthToken() != null;
    }

    @Override
    public void updateProfile(User user, ApiCallback<User> callback) {
        if (user == null || user.getName() == null || user.getName().trim().isEmpty()) {
            callback.onError("Name cannot be empty.");
            return;
        }
        api().updateProfile(new ProfileUpdateRequestDto(user)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    preferenceManager.saveUser(currentUser);
                    callback.onSuccess(currentUser);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                callback.onError(ApiErrorParser.networkFailureMessage(t));
            }
        });
    }
}
