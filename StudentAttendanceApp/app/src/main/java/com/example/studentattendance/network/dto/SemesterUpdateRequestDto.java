package com.example.studentattendance.network.dto;

public class SemesterUpdateRequestDto {
    private final String name;
    private final Integer semesterNumber;
    private final Boolean archived;

    public SemesterUpdateRequestDto(String name, Integer semesterNumber, Boolean archived) {
        this.name = name;
        this.semesterNumber = semesterNumber;
        this.archived = archived;
    }
}
