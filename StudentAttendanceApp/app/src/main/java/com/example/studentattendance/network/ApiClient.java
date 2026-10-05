package com.example.studentattendance.network;

import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.example.studentattendance.BuildConfig;
import com.example.studentattendance.utils.PreferenceManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final long CONNECT_TIMEOUT_SECONDS = 15;
    private static final long READ_TIMEOUT_SECONDS = 60;
    private static final long WRITE_TIMEOUT_SECONDS = 60;

    private static volatile Retrofit retrofit;
    private static volatile ApiService apiService;
    private static volatile String activeBaseUrl;

    private ApiClient() {
    }

    public static String defaultBaseUrl() {
        return ApiUrlPolicy.defaultCloudUrl();
    }

    public static String normalize(String url) {
        String normalized = ApiUrlPolicy.normalize(url);
        return normalized.isEmpty() ? ApiUrlPolicy.defaultCloudUrl() : normalized;
    }

    public static ApiService getApiService(Context context) {
        PreferenceManager prefs = new PreferenceManager(context.getApplicationContext());
        String baseUrl = ApiUrlPolicy.resolve(prefs.getApiBaseUrl());
        if (apiService == null || retrofit == null || !baseUrl.equals(activeBaseUrl)) {
            synchronized (ApiClient.class) {
                if (apiService == null || retrofit == null || !baseUrl.equals(activeBaseUrl)) {
                    retrofit = buildRetrofit(context.getApplicationContext(), baseUrl);
                    apiService = retrofit.create(ApiService.class);
                    activeBaseUrl = baseUrl;
                }
            }
        }
        return apiService;
    }

    public static void reset() {
        synchronized (ApiClient.class) {
            retrofit = null;
            apiService = null;
            activeBaseUrl = null;
        }
    }

    public static void wakeServer(Context context) {
        try {
            getApiService(context).health().enqueue(new retrofit2.Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, String>> call,
                                       retrofit2.Response<java.util.Map<String, String>> response) {
                    // Cold-start ping; ignore body.
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, String>> call, Throwable t) {
                    // First request may fail while the host boots; login/register retry after.
                }
            });
        } catch (Exception ignored) {
            // Never crash from a warm-up ping.
        }
    }

    private static Retrofit buildRetrofit(Context appContext, String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        PreferenceManager preferenceManager = new PreferenceManager(appContext);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(preferenceManager))
                .addInterceptor(new SessionExpiryInterceptor(appContext, preferenceManager))
                .addInterceptor(new RetryInterceptor())
                .addInterceptor(logging)
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(READ_TIMEOUT_SECONDS + CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        return new Retrofit.Builder()
                .baseUrl(ApiUrlPolicy.normalize(baseUrl))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
