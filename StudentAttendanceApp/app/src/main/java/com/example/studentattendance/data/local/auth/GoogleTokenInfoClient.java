package com.example.studentattendance.data.local.auth;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Calls Google's public OAuth tokeninfo endpoint. */
public final class GoogleTokenInfoClient implements GoogleIdTokenVerifier.TokenInfoClient {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    @Override
    public String fetchTokenInfo(String idToken) throws IOException {
        HttpUrl url = HttpUrl.parse("https://oauth2.googleapis.com/tokeninfo")
                .newBuilder()
                .addQueryParameter("id_token", idToken)
                .build();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            ResponseBody body = response.body();
            String text = body != null ? body.string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Google tokeninfo HTTP " + response.code());
            }
            return text;
        }
    }
}
