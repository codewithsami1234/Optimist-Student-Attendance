package com.example.studentattendance.network.dto;

public class SectionUpdateRequestDto {
    private final String name;
    private final Boolean archived;

    public SectionUpdateRequestDto(String name, Boolean archived) {
        this.name = name;
        this.archived = archived;
    }
}
