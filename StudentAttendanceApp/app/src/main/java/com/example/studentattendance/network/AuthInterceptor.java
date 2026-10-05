package com.example.studentattendance.network;

import java.io.IOException;

import androidx.annotation.NonNull;

import com.example.studentattendance.utils.PreferenceManager;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Attaches "Authorization: Bearer <token>" to every outgoing request once
 * the user has logged in. The login request itself has no token yet, which
 * is fine - the backends /api/auth/** endpoints are explicitly public
 * (see SecurityConfig on the server) and simply ignore a missing header.
 */
public class AuthInterceptor implements Interceptor {

    private final PreferenceManager preferenceManager;

    public AuthInterceptor(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        boolean publicEndpoint = path.endsWith("/api/auth/login")
                || path.endsWith("/api/auth/register")
                || path.endsWith("/api/auth/google")
                || path.endsWith("/api/health");
        String token = preferenceManager.getAuthToken();
        if (publicEndpoint || token == null || token.isEmpty() || preferenceManager.isLocalSession()) {
            return chain.proceed(original);
        }
        Request authorized = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authorized);
    }
}
