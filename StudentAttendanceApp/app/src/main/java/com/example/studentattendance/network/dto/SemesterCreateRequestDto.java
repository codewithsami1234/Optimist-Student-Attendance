package com.example.studentattendance.network.dto;

public class SemesterCreateRequestDto {
    private final String name;
    private final int semesterNumber;

    public SemesterCreateRequestDto(String name, int semesterNumber) {
        this.name = name;
        this.semesterNumber = semesterNumber;
    }
}
