package com.example.studentattendance.network.dto;

public class DepartmentUpdateRequestDto {
    private final String name;
    private final String code;
    private final String headOfDepartment;
    private final Boolean archived;

    public DepartmentUpdateRequestDto(String name, String code, String headOfDepartment, Boolean archived) {
        this.name = name;
        this.code = code;
        this.headOfDepartment = headOfDepartment;
        this.archived = archived;
    }
}
