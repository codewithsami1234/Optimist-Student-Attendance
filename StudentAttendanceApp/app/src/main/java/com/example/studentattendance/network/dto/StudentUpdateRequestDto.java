package com.example.studentattendance.network.dto;

public class StudentUpdateRequestDto {
    private final String name;
    private final String rollNumber;
    private final String email;
    private final String phone;
    private final Boolean archived;

    public StudentUpdateRequestDto(String name, String rollNumber, String email, String phone, Boolean archived) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.email = email;
        this.phone = phone;
        this.archived = archived;
    }
}
