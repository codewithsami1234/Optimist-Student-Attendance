package com.example.studentattendance.network.dto;

import com.google.gson.annotations.SerializedName;

public class GoogleAuthRequestDto {
    @SerializedName("idToken")
    private final String idToken;

    public GoogleAuthRequestDto(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }
}
