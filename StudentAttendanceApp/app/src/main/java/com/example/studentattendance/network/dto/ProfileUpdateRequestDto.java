package com.example.studentattendance.network.dto;

import com.example.studentattendance.model.User;

public class ProfileUpdateRequestDto {
    private final String id;
    private final String name;
    private final String email;
    private final String role;
    private final String designation;
    private final Double lowAttendanceThreshold;

    public ProfileUpdateRequestDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.designation = user.getDesignation();
        this.lowAttendanceThreshold = user.getLowAttendanceThreshold();
    }
}
