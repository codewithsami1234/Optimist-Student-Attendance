package com.example.studentattendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.studentattendance.model.User;

/**
 * Thin wrapper around {@link SharedPreferences} for session state
 * (logged-in flag, cached user, and simple app settings such as
 * notification/dark-mode toggles shown on the Settings screen).
 */
public class PreferenceManager {

    private static final String PREFS_NAME = "attendance_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_DESIGNATION = "user_designation";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    private static final String KEY_API_BASE_URL = "api_base_url";
    private static final String KEY_THRESHOLD = "low_attendance_threshold";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void saveUser(User user) {
        prefs.edit()
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_ROLE, user.getRole())
                .putString(KEY_USER_DESIGNATION, user.getDesignation())
                .putFloat(KEY_THRESHOLD, (float) user.getLowAttendanceThreshold())
                .apply();
    }

    public User getSavedUser() {
        String id = prefs.getString(KEY_USER_ID, null);
        if (id == null) {
            return null;
        }
        User user = new User(
                id,
                prefs.getString(KEY_USER_NAME, ""),
                prefs.getString(KEY_USER_EMAIL, ""),
                prefs.getString(KEY_USER_ROLE, "Teacher"),
                prefs.getString(KEY_USER_DESIGNATION, "")
        );
        user.setLowAttendanceThreshold(prefs.getFloat(KEY_THRESHOLD, 75f));
        return user;
    }

    public void clearUser() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_DESIGNATION)
                .apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
    }

    /**
     * Phase 2: the JWT issued by POST /api/auth/login. Stored here (rather
     * than in a field on the repository) so it survives process death and so
     * {@link com.example.studentattendance.network.AuthInterceptor} can read
     * it for every outgoing request without any repository wiring.
     */
    public void saveAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply();
    }

    /** Local Room sessions use a `local_` token that must never be sent to the API. */
    public boolean isLocalSession() {
        String token = getAuthToken();
        return token != null && token.startsWith("local_");
    }

    public boolean isCloudSession() {
        String token = getAuthToken();
        if (token == null || token.isEmpty() || token.startsWith("local_")) {
            return false;
        }
        int dots = 0;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '.') {
                dots++;
            }
        }
        return dots >= 2;
    }

    /**
     * Phase 3: full session teardown used both for a normal logout and for
     * an automatic logout when the server rejects the JWT as expired or
     * invalid (see {@link com.example.studentattendance.network.SessionExpiryInterceptor}).
     */
    public void clearSession() {
        setLoggedIn(false);
        clearUser();
        clearAuthToken();
    }

    public void setRememberMe(boolean rememberMe, String email) {
        prefs.edit()
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .putString(KEY_REMEMBERED_EMAIL, rememberMe && email != null ? email : "")
                .apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getRememberedEmail() {
        return prefs.getString(KEY_REMEMBERED_EMAIL, "");
    }

    public String getApiBaseUrl() {
        String stored = prefs.getString(KEY_API_BASE_URL, null);
        if (stored != null && !stored.isEmpty()) {
            if (!com.example.studentattendance.network.ApiUrlPolicy.isAllowed(stored)) {
                if (com.example.studentattendance.network.ApiUrlPolicy.isEmulatorLoopback(stored)) {
                    prefs.edit().remove(KEY_API_BASE_URL).apply();
                }
                return com.example.studentattendance.network.ApiUrlPolicy.defaultCloudUrl();
            }
            return com.example.studentattendance.network.ApiUrlPolicy.normalize(stored);
        }
        return com.example.studentattendance.network.ApiUrlPolicy.defaultCloudUrl();
    }

    public boolean setApiBaseUrl(String url) {
        if (!com.example.studentattendance.network.ApiUrlPolicy.isAllowed(url)) {
            return false;
        }
        prefs.edit().putString(KEY_API_BASE_URL,
                com.example.studentattendance.network.ApiUrlPolicy.normalize(url)).apply();
        com.example.studentattendance.network.ApiClient.reset();
        return true;
    }
}
