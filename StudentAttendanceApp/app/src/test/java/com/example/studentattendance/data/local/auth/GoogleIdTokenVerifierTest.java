package com.example.studentattendance.data.local.auth;

import java.io.IOException;

import org.junit.Test;

import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.utils.GoogleSignInHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GoogleIdTokenVerifierTest {

    private static final String AUDIENCE = "123456789-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com";

    @Test
    public void parseAcceptsGoogleTokenInfoJson() {
        String json = "{"
                + "\"iss\":\"https://accounts.google.com\","
                + "\"aud\":\"" + AUDIENCE + "\","
                + "\"email\":\"prof@gmail.com\","
                + "\"email_verified\":\"true\","
                + "\"sub\":\"google-sub-99\","
                + "\"name\":\"Google Professor\""
                + "}";
        GoogleIdentity identity = GoogleIdTokenVerifier.parseTokenInfo(json, AUDIENCE);
        assertEquals("google-sub-99", identity.subject);
        assertEquals("prof@gmail.com", identity.email);
        assertEquals("Google Professor", identity.name);
    }

    @Test
    public void parseRejectsWrongAudienceAndUnverifiedEmail() {
        try {
            GoogleIdTokenVerifier.parseTokenInfo("{"
                    + "\"iss\":\"https://accounts.google.com\","
                    + "\"aud\":\"other-client\","
                    + "\"email\":\"prof@gmail.com\","
                    + "\"email_verified\":\"true\","
                    + "\"sub\":\"sub\","
                    + "\"name\":\"N\""
                    + "}", AUDIENCE);
            fail();
        } catch (LocalDataException e) {
            assertTrue(e.getMessage().contains("not authorised"));
        }
        try {
            GoogleIdTokenVerifier.parseTokenInfo("{"
                    + "\"iss\":\"https://accounts.google.com\","
                    + "\"aud\":\"" + AUDIENCE + "\","
                    + "\"email\":\"prof@gmail.com\","
                    + "\"email_verified\":\"false\","
                    + "\"sub\":\"sub\","
                    + "\"name\":\"N\""
                    + "}", AUDIENCE);
            fail();
        } catch (LocalDataException e) {
            assertTrue(e.getMessage().contains("verified email"));
        }
    }

    @Test
    public void verifyUsesTokenInfoClientThenReturnsIdentity() {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(idToken -> {
            assertEquals("real-google-id-token", idToken);
            return "{"
                    + "\"iss\":\"accounts.google.com\","
                    + "\"aud\":\"" + AUDIENCE + "\","
                    + "\"email\":\"prof@gmail.com\","
                    + "\"email_verified\":\"true\","
                    + "\"sub\":\"sub-live\","
                    + "\"name\":\"Live User\""
                    + "}";
        }, AUDIENCE);
        GoogleIdentity identity = verifier.verify("real-google-id-token");
        assertEquals("sub-live", identity.subject);
        assertEquals("prof@gmail.com", identity.email);
    }

    @Test
    public void verifyMapsNetworkFailureToClearError() {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(idToken -> {
            throw new IOException("offline");
        }, AUDIENCE);
        try {
            verifier.verify("token");
            fail();
        } catch (LocalDataException e) {
            assertTrue(e.getMessage().contains("internet connection"));
        }
    }

    @Test
    public void emptyTokenAndMissingClientIdAreRejected() {
        try {
            new GoogleIdTokenVerifier(idToken -> "{}", "").verify("token");
            fail();
        } catch (LocalDataException e) {
            assertEquals(GoogleSignInHelper.NOT_CONNECTED_MESSAGE, e.getMessage());
        }
        try {
            new GoogleIdTokenVerifier(idToken -> "{}", "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com").verify("token");
            fail();
        } catch (LocalDataException e) {
            assertEquals(GoogleSignInHelper.NOT_CONNECTED_MESSAGE, e.getMessage());
        }
        try {
            new GoogleIdTokenVerifier(idToken -> "{}", AUDIENCE).verify(" ");
            fail();
        } catch (LocalDataException e) {
            assertTrue(e.getMessage().contains("ID token"));
        }
    }
}
