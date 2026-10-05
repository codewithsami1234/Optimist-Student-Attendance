package com.example.studentattendance.network;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.studentattendance.ui.login.LoginActivity;
import com.example.studentattendance.utils.PreferenceManager;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Phase 3: secure session handling. A 401 response from the backend means
 * the JWT was rejected - it expired, was tampered with, or the user was
 * removed server-side (see JwtAuthenticationFilter/SecurityConfig on the
 * backend). Rather than let every screen individually guess at what a bare
 * "Unauthorized" error means, this interceptor treats it as a single,
 * app-wide signal: the session is over.
 * <p>
 * On the first such response, it synchronously clears the saved session
 * (token + cached user) and relaunches {@link LoginActivity} with
 * {@code FLAG_ACTIVITY_CLEAR_TASK}, so the user is dropped back at the
 * login screen - regardless of which screen they were on - instead of
 * seeing confusing "failed to load" errors on a dead session. The login
 * request itself is exempt, since a wrong password there is a normal login
 * failure, not an expired session.
 */
public class SessionExpiryInterceptor implements Interceptor {

    /** Several requests can fail with 401 within milliseconds of each other once a token expires; debounce them. */
    private static final long DEBOUNCE_MILLIS = 3000L;

    private final Context appContext;
    private final PreferenceManager preferenceManager;
    private final AtomicLong lastHandledAtMillis = new AtomicLong(0L);

    public SessionExpiryInterceptor(Context context, PreferenceManager preferenceManager) {
        this.appContext = context.getApplicationContext();
        this.preferenceManager = preferenceManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        String path = chain.request().url().encodedPath();
        boolean isPublicAuthRequest = path.endsWith("/api/auth/login")
                || path.endsWith("/api/auth/register")
                || path.endsWith("/api/auth/google")
                || path.endsWith("/api/health");
        long now = System.currentTimeMillis();
        if (response.code() == 401 && !isPublicAuthRequest && preferenceManager.isLoggedIn()
                && !preferenceManager.isLocalSession()
                && now - lastHandledAtMillis.get() > DEBOUNCE_MILLIS) {
            lastHandledAtMillis.set(now);
            preferenceManager.clearSession();
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.putExtra(LoginActivity.EXTRA_SESSION_EXPIRED, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        }
        return response;
    }
}
