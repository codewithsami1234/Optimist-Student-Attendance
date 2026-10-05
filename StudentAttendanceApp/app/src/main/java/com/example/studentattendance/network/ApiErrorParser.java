package com.example.studentattendance.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import com.example.studentattendance.network.dto.ApiErrorBody;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Turns a failed Retrofit {@link Response} (4xx/5xx) into a short,
 * user-facing message. Network failures are mapped by cause so a dead
 * emulator host is not reported as a generic timeout.
 */
public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {
    }

    public static String parse(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                String raw = errorBody.string();
                ApiErrorBody parsed = GSON.fromJson(raw, ApiErrorBody.class);
                if (parsed != null && parsed.getMessage() != null && !parsed.getMessage().isEmpty()) {
                    return parsed.getMessage();
                }
            } catch (Exception ignored) {
                // Fall through to the generic message below.
            }
        }
        if (response.code() == 401) {
            return "Your session has expired. Please log in again.";
        }
        if (response.code() == 404) {
            return "The requested data could not be found.";
        }
        if (response.code() == 429) {
            return "Too many attempts. Please wait a minute and try again.";
        }
        return "Something went wrong (HTTP " + response.code() + "). Please try again.";
    }

    /** Message for a request that never reached the server (no connectivity, DNS failure, timeout, etc). */
    public static String networkFailureMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            if (cause instanceof SocketTimeoutException
                    || cause instanceof UnknownHostException
                    || cause instanceof ConnectException
                    || cause instanceof SSLHandshakeException) {
                break;
            }
            cause = cause.getCause();
        }
        if (cause instanceof UnknownHostException) {
            return "Cannot find the attendance server. Check your internet connection and API URL.";
        }
        if (cause instanceof ConnectException) {
            return "Could not connect to the attendance server. If it just woke up, wait a few seconds and try again.";
        }
        if (cause instanceof SSLHandshakeException) {
            return "A secure connection could not be established. The API must use HTTPS.";
        }
        if (cause instanceof SocketTimeoutException) {
            return "The attendance server is still starting. Wait a few seconds and try again.";
        }
        if (t instanceof IOException) {
            return "Could not reach the server. Please check your internet connection and try again.";
        }
        return "An unexpected error occurred. Please try again.";
    }
}
