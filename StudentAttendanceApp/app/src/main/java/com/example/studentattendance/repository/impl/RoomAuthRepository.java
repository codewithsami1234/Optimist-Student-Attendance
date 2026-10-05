package com.example.studentattendance.repository.impl;

import android.content.Context;

import com.example.studentattendance.BuildConfig;
import com.example.studentattendance.data.local.AcademicSeeder;
import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.RoomAccountStore;
import com.example.studentattendance.data.local.auth.AuthOutcome;
import com.example.studentattendance.data.local.auth.GoogleIdTokenVerifier;
import com.example.studentattendance.data.local.auth.GoogleIdentity;
import com.example.studentattendance.data.local.auth.GoogleTokenInfoClient;
import com.example.studentattendance.data.local.auth.LocalAuthEngine;
import com.example.studentattendance.data.local.auth.PasswordHasher;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.utils.GoogleSignInHelper;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Local Room-backed authentication. Email/password accounts are hashed in
 * SQLite; Google Sign-In still uses Credential Manager + Google tokeninfo,
 * then stores the verified user locally. No cloud database is used.
 */
public class RoomAuthRepository implements AuthRepository {

    private final PreferenceManager preferenceManager;
    private final LocalAuthEngine engine;
    private final GoogleIdTokenVerifier googleVerifier;
    private User currentUser;

    public RoomAuthRepository(Context context, PreferenceManager preferenceManager, AppDatabase database) {
        this.preferenceManager = preferenceManager;
        this.engine = new LocalAuthEngine(
                new RoomAccountStore(database.accountDao()),
                new PasswordHasher(),
                userId -> AcademicSeeder.seedIfEmpty(database, userId));
        this.googleVerifier = new GoogleIdTokenVerifier(
                new GoogleTokenInfoClient(),
                BuildConfig.GOOGLE_WEB_CLIENT_ID);
        this.currentUser = preferenceManager.getSavedUser();
    }

    RoomAuthRepository(PreferenceManager preferenceManager, LocalAuthEngine engine,
                       GoogleIdTokenVerifier googleVerifier) {
        this.preferenceManager = preferenceManager;
        this.engine = engine;
        this.googleVerifier = googleVerifier;
        this.currentUser = preferenceManager.getSavedUser();
    }

    private void persistSession(AuthOutcome outcome) {
        currentUser = outcome.user;
        preferenceManager.saveUser(outcome.user);
        preferenceManager.saveAuthToken(outcome.sessionToken);
        preferenceManager.setLoggedIn(true);
    }

    @Override
    public void login(String email, String password, ApiCallback<User> callback) {
        LocalExec.io(() -> {
            AuthOutcome outcome = engine.login(email, password);
            persistSession(outcome);
            return outcome.user;
        }, callback);
    }

    @Override
    public void register(String name, String email, String password, String designation, ApiCallback<User> callback) {
        LocalExec.io(() -> {
            AuthOutcome outcome = engine.register(name, email, password, designation);
            currentUser = null;
            preferenceManager.clearSession();
            return outcome.user;
        }, callback);
    }

    @Override
    public void loginWithGoogle(String idToken, ApiCallback<User> callback) {
        if (!GoogleSignInHelper.isConfigured()
                || !GoogleSignInHelper.isValidWebClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
            callback.onError(GoogleSignInHelper.NOT_CONNECTED_MESSAGE);
            return;
        }
        LocalExec.io(() -> {
            GoogleIdentity identity = googleVerifier.verify(idToken);
            AuthOutcome outcome = engine.loginWithGoogle(identity);
            persistSession(outcome);
            return outcome.user;
        }, callback);
    }

    @Override
    public void logout() {
        currentUser = null;
        preferenceManager.clearSession();
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
        String token = preferenceManager.getAuthToken();
        return preferenceManager.isLoggedIn()
                && getCurrentUser() != null
                && token != null
                && !token.trim().isEmpty();
    }

    @Override
    public void updateProfile(User user, ApiCallback<User> callback) {
        LocalExec.io(() -> {
            User session = getCurrentUser();
            if (session == null) {
                throw new LocalDataException("Please sign in again.");
            }
            User updated = engine.updateProfile(session.getId(), user);
            currentUser = updated;
            preferenceManager.saveUser(updated);
            return updated;
        }, callback);
    }
}
