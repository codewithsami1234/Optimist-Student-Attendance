package com.example.studentattendance.network.dto;

public class SubjectUpdateRequestDto {
    private final String name;
    private final String code;
    private final Boolean archived;

    public SubjectUpdateRequestDto(String name, String code, Boolean archived) {
        this.name = name;
        this.code = code;
        this.archived = archived;
    }
}
