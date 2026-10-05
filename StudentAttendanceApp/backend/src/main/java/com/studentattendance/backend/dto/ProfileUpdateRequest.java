package com.studentattendance.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {
    private String id;

    @NotBlank(message = "Name cannot be empty.")
    private String name;

    private String email;
    private String role;
    private String designation;
    private Double lowAttendanceThreshold;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public Double getLowAttendanceThreshold() { return lowAttendanceThreshold; }
    public void setLowAttendanceThreshold(Double lowAttendanceThreshold) { this.lowAttendanceThreshold = lowAttendanceThreshold; }
}
