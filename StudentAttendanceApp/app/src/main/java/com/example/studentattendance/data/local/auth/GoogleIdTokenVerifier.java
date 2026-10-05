package com.example.studentattendance.data.local.auth;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.utils.GoogleSignInHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifies a Google ID token with Google's tokeninfo endpoint (OAuth), then
 * persists the resulting identity locally. This is not the app's cloud database.
 */
public final class GoogleIdTokenVerifier {

    private static final Set<String> TRUSTED_ISSUERS = new HashSet<>();

    static {
        TRUSTED_ISSUERS.add("accounts.google.com");
        TRUSTED_ISSUERS.add("https://accounts.google.com");
    }

    private final TokenInfoClient tokenInfoClient;
    private final String expectedAudience;

    public GoogleIdTokenVerifier(TokenInfoClient tokenInfoClient, String expectedAudience) {
        this.tokenInfoClient = tokenInfoClient;
        this.expectedAudience = expectedAudience == null ? "" : expectedAudience.trim();
    }

    public GoogleIdentity verify(String idToken) {
        if (!GoogleSignInHelper.isValidWebClientId(expectedAudience)) {
            throw new LocalDataException(GoogleSignInHelper.NOT_CONNECTED_MESSAGE);
        }
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new LocalDataException("Google Sign-In did not return an ID token.");
        }
        String body;
        try {
            body = tokenInfoClient.fetchTokenInfo(idToken.trim());
        } catch (IOException e) {
            throw new LocalDataException(
                    "Google Sign-In needs an internet connection to verify your account. Please try again.");
        }
        return parseTokenInfo(body, expectedAudience);
    }

    static GoogleIdentity parseTokenInfo(String json, String expectedAudience) {
        if (json == null || json.trim().isEmpty()) {
            throw new LocalDataException("Google sign-in could not be verified. Please try again.");
        }
        JsonObject body;
        try {
            body = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            throw new LocalDataException("Google sign-in could not be verified. Please try again.");
        }
        String issuer = text(body, "iss");
        if (!TRUSTED_ISSUERS.contains(issuer)) {
            throw new LocalDataException("Google sign-in could not be verified. Please try again.");
        }
        String audience = text(body, "aud");
        if (expectedAudience == null || expectedAudience.isEmpty() || !expectedAudience.equals(audience)) {
            throw new LocalDataException("This Google account is not authorised for this app.");
        }
        String verified = text(body, "email_verified");
        if (!"true".equalsIgnoreCase(verified) && !"1".equals(verified)) {
            throw new LocalDataException("Please use a Google account with a verified email address.");
        }
        String email = text(body, "email").trim().toLowerCase();
        if (email.isEmpty() || !email.contains("@")) {
            throw new LocalDataException("Google did not provide a valid email address.");
        }
        String name = text(body, "name");
        if (name.isEmpty()) {
            name = email.substring(0, email.indexOf('@'));
        }
        String subject = text(body, "sub");
        if (subject.isEmpty()) {
            throw new LocalDataException("Google sign-in could not be verified. Please try again.");
        }
        return new GoogleIdentity(subject, email, name);
    }

    private static String text(JsonObject object, String field) {
        JsonElement value = object.get(field);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    public interface TokenInfoClient {
        String fetchTokenInfo(String idToken) throws IOException;
    }
}
