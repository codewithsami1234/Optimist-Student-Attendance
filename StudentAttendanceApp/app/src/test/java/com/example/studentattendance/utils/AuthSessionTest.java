package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class AuthSessionTest {

    @Test
    public void existingSessionNeverSkipsSignIn() {
        assertFalse(AuthSession.shouldRestoreAfterRestart(true, true));
        assertFalse(AuthSession.shouldRestoreAfterRestart(false, true));
        assertFalse(AuthSession.shouldRestoreAfterRestart(true));
    }

    @Test
    public void accountWithoutActiveSessionGoesToSignIn() {
        assertFalse(AuthSession.shouldRestoreAfterRestart(false));
        assertFalse(AuthSession.shouldRestoreAfterRestart(true, false));
    }

    @Test
    public void registeredAccountIsNotAnAuthenticatedSession() {
        boolean roomAccountExists = true;
        boolean authenticatedSession = false;
        assertFalse(AuthSession.shouldRestoreAfterRestart(roomAccountExists, authenticatedSession));
        assertFalse(AuthSession.shouldRestoreAfterRestart(authenticatedSession));
    }

    @Test
    public void leftoverLoggedInFlagStillRequiresSignInAfterRestart() {
        assertFalse(AuthSession.shouldRestoreAfterRestart(true));
    }
}
