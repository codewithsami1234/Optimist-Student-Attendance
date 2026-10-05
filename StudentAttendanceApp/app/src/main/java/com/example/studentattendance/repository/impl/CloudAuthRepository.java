package com.example.studentattendance.repository.impl;

import android.content.Context;

import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.SyncScheduler;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Cloud auth is the source of truth. A stored JWT still allows offline use of
 * cached data after a successful login.
 */
public class CloudAuthRepository implements AuthRepository {

    private final Context appContext;
    private final RetrofitAuthRepository remote;
    private final PendingSyncStore store;

    public CloudAuthRepository(Context context, PreferenceManager preferenceManager, PendingSyncStore store) {
        this.appContext = context.getApplicationContext();
        this.remote = new RetrofitAuthRepository(appContext, preferenceManager);
        this.store = store;
    }

    @Override
    public void login(String email, String password, ApiCallback<User> callback) {
        remote.login(email, password, afterAuth(callback));
    }

    @Override
    public void register(String name, String email, String password, String designation, ApiCallback<User> callback) {
        remote.register(name, email, password, designation, afterAuth(callback));
    }

    @Override
    public void loginWithGoogle(String idToken, ApiCallback<User> callback) {
        remote.loginWithGoogle(idToken, afterAuth(callback));
    }

    @Override
    public void logout() {
        store.clearAll();
        remote.logout();
    }

    @Override
    public User getCurrentUser() {
        return remote.getCurrentUser();
    }

    @Override
    public boolean isLoggedIn() {
        return remote.isLoggedIn();
    }

    @Override
    public void updateProfile(User user, ApiCallback<User> callback) {
        remote.updateProfile(user, callback);
    }

    private ApiCallback<User> afterAuth(ApiCallback<User> callback) {
        return new ApiCallback<User>() {
            @Override
            public void onSuccess(User data) {
                SyncScheduler.enqueueNow(appContext);
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }
}
