package com.example.studentattendance.network.dto;

public class SubjectCreateRequestDto {
    private final String name;
    private final String code;

    public SubjectCreateRequestDto(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
