package com.example.studentattendance.network.dto;

/** Phase 4: request body for a professor creating one of their own departments. */
public class DepartmentCreateRequestDto {
    private final String name;
    private final String code;
    private final String headOfDepartment;

    public DepartmentCreateRequestDto(String name, String code, String headOfDepartment) {
        this.name = name;
        this.code = code;
        this.headOfDepartment = headOfDepartment;
    }
}
