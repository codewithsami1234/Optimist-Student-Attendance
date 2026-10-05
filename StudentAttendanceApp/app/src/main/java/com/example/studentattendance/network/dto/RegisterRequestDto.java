package com.example.studentattendance.network.dto;

/** Phase 4: request body for a professor creating their own private account. */
public class RegisterRequestDto {
    private final String name;
    private final String email;
    private final String password;
    private final String designation;

    public RegisterRequestDto(String name, String email, String password, String designation) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.designation = designation;
    }
}
