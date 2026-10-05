package com.example.studentattendance.network;

import java.io.IOException;

import androidx.annotation.NonNull;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Retries requests that never reached the server (connection reset / DNS blip).
 * POST bodies are retried only on transport failure, not on HTTP 5xx, so
 * registration cannot be applied twice after the server already created the account.
 */
public final class RetryInterceptor implements Interceptor {

    private static final int MAX_ATTEMPTS = 2;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        IOException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return chain.proceed(request);
            } catch (IOException e) {
                last = e;
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
        throw last;
    }
}
