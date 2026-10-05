package com.example.studentattendance.utils;

/**
 * Local session restore rules. A registered Room account is never treated as
 * an active login. Cold start always requires Sign-In; a session is created
 * only after successful credential verification.
 */
public final class AuthSession {

    private AuthSession() {
    }

    public static boolean shouldRestoreAfterRestart(boolean rememberMe, boolean loggedIn) {
        return false;
    }

    public static boolean shouldRestoreAfterRestart(boolean loggedIn) {
        return false;
    }
}
