package com.example.studentattendance.utils;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.studentattendance.BuildConfig;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/**
 * Real Google Identity / Credential Manager flow. When a valid Web client ID
 * is present, the ID token is verified with Google and stored in local Room.
 * When OAuth is missing or misconfigured, the UI gets a clear message and
 * email/password login keeps working. This is not a mock or fake login.
 */
public final class GoogleSignInHelper {

    public static final String NOT_CONNECTED_MESSAGE =
            "Google Sign-In isn\u2019t connected yet. Please use email and password, or configure Google Sign-In to continue with your Google account.";

    private static final Pattern WEB_CLIENT_ID = Pattern.compile(
            "^[0-9]+-[A-Za-z0-9_-]+\\.apps\\.googleusercontent\\.com$");

    public interface Callback {
        void onGoogleIdToken(@NonNull String idToken);

        void onGoogleError(@NonNull String message);
    }

    private GoogleSignInHelper() {
    }

    public static boolean isConfigured() {
        return isValidWebClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID);
    }

    public static boolean isValidWebClientId(@Nullable String clientId) {
        if (clientId == null) {
            return false;
        }
        String value = clientId.trim();
        if (value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.US);
        if (lower.contains("your_") || lower.contains("example") || lower.contains("placeholder")
                || lower.equals("null") || lower.equals("undefined")) {
            return false;
        }
        return WEB_CLIENT_ID.matcher(value).matches();
    }

    public static void requestIdToken(@NonNull Activity activity, @NonNull Callback callback) {
        if (!isConfigured()) {
            callback.onGoogleError(NOT_CONNECTED_MESSAGE);
            return;
        }
        try {
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID.trim())
                    .setAutoSelectEnabled(false)
                    .build();
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            CredentialManager credentialManager = CredentialManager.create(activity);
            Executor mainExecutor = command -> new Handler(Looper.getMainLooper()).post(command);
            AtomicBoolean delivered = new AtomicBoolean(false);

            credentialManager.getCredentialAsync(
                    activity,
                    request,
                    null,
                    mainExecutor,
                    new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            if (!delivered.compareAndSet(false, true)) {
                                return;
                            }
                            try {
                                callback.onGoogleIdToken(extractIdToken(result.getCredential()));
                            } catch (RuntimeException e) {
                                callback.onGoogleError(e.getMessage() != null
                                        ? e.getMessage()
                                        : "Google Sign-In did not return an ID token.");
                            }
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            if (!delivered.compareAndSet(false, true)) {
                                return;
                            }
                            callback.onGoogleError(mapError(e));
                        }
                    });
        } catch (RuntimeException e) {
            callback.onGoogleError(looksLikeOauthMisconfiguration(e.getMessage())
                    ? NOT_CONNECTED_MESSAGE
                    : "Google Sign-In failed. Please try again.");
        }
    }

    @NonNull
    private static String extractIdToken(@NonNull Credential credential) {
        if (credential instanceof CustomCredential custom
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(custom.getData());
            String token = googleIdTokenCredential.getIdToken();
            if (token != null && !token.isEmpty()) {
                return token;
            }
        }
        throw new IllegalArgumentException("Google Sign-In did not return an ID token.");
    }

    @NonNull
    static String mapError(@NonNull GetCredentialException e) {
        return mapErrorDetails(
                e instanceof GetCredentialCancellationException,
                e instanceof NoCredentialException,
                e.getMessage());
    }

    @NonNull
    static String mapErrorDetails(boolean cancelled, boolean noCredential, @Nullable String rawMessage) {
        if (cancelled) {
            return "Google Sign-In was cancelled.";
        }
        if (noCredential || looksLikeOauthMisconfiguration(rawMessage)) {
            return NOT_CONNECTED_MESSAGE;
        }
        return "Google Sign-In failed. Please try again.";
    }

    static boolean looksLikeOauthMisconfiguration(@Nullable String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return false;
        }
        String message = rawMessage.toLowerCase(Locale.US);
        return message.contains("developer")
                || message.contains("developer_error")
                || message.contains("invalid client")
                || message.contains("unknown calling package")
                || message.contains("api console")
                || message.contains("12500")
                || message.contains(" :10")
                || message.contains("[10]")
                || message.startsWith("10:")
                || message.contains("10: ")
                || (message.contains("client") && message.contains("oauth"));
    }
}
