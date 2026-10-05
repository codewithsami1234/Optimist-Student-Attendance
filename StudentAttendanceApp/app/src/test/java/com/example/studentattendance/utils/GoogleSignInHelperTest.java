package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GoogleSignInHelperTest {

    private static final String EXPECTED_UNCONFIGURED =
            "Google Sign-In isn\u2019t connected yet. Please use email and password, or configure Google Sign-In to continue with your Google account.";

    @Test
    public void unconfiguredMessageIsExact() {
        assertEquals(EXPECTED_UNCONFIGURED, GoogleSignInHelper.NOT_CONNECTED_MESSAGE);
    }

    @Test
    public void missingOrPlaceholderClientIdIsUnconfigured() {
        assertFalse(GoogleSignInHelper.isValidWebClientId(null));
        assertFalse(GoogleSignInHelper.isValidWebClientId(""));
        assertFalse(GoogleSignInHelper.isValidWebClientId("   "));
        assertFalse(GoogleSignInHelper.isValidWebClientId("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"));
        assertFalse(GoogleSignInHelper.isValidWebClientId("example.apps.googleusercontent.com"));
        assertFalse(GoogleSignInHelper.isValidWebClientId("not-a-client-id"));
        assertFalse(GoogleSignInHelper.isValidWebClientId("https://student-attendance-api.onrender.com/"));
    }

    @Test
    public void productionWebClientIdIsConfigured() {
        assertTrue(GoogleSignInHelper.isValidWebClientId(
                "123456789-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com"));
        assertTrue(GoogleSignInHelper.isValidWebClientId(
                "  9988776655-abc_DEF-123.apps.googleusercontent.com  "));
    }

    @Test
    public void isConfiguredFollowsTheWebClientIdInBuildConfig() {
        assertEquals(
                GoogleSignInHelper.isValidWebClientId(com.example.studentattendance.BuildConfig.GOOGLE_WEB_CLIENT_ID),
                GoogleSignInHelper.isConfigured());
    }

    @Test
    public void oauthMisconfigurationUsesTheSameUnconfiguredMessage() {
        assertEquals(EXPECTED_UNCONFIGURED,
                GoogleSignInHelper.mapErrorDetails(false, true, "No credentials"));
        assertEquals(EXPECTED_UNCONFIGURED,
                GoogleSignInHelper.mapErrorDetails(false, false, "DEVELOPER_ERROR: 10:"));
        assertEquals(EXPECTED_UNCONFIGURED,
                GoogleSignInHelper.mapErrorDetails(false, false, "Invalid client ID from API console"));
        assertEquals("Google Sign-In was cancelled.",
                GoogleSignInHelper.mapErrorDetails(true, false, null));
        assertEquals("Google Sign-In failed. Please try again.",
                GoogleSignInHelper.mapErrorDetails(false, false, "network timeout"));
    }
}
