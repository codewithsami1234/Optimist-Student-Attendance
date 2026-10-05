package com.studentattendance.backend.dto;

import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.entity.User;

/**
 * Field names mirror com.example.studentattendance.model.User exactly
 * (id, name, email, role, designation, avatarInitials) - deserializes
 * directly into that Android class with no custom mapping.
 */
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String designation;
    private String avatarInitials;
    private double lowAttendanceThreshold = 75.0;

    public UserDto() {
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.designation = user.getDesignation();
        this.avatarInitials = Student.computeInitials(user.getName());
        this.lowAttendanceThreshold = user.getLowAttendanceThreshold() <= 0
                ? 75.0 : user.getLowAttendanceThreshold();
    }

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
    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }
    public double getLowAttendanceThreshold() { return lowAttendanceThreshold; }
    public void setLowAttendanceThreshold(double lowAttendanceThreshold) { this.lowAttendanceThreshold = lowAttendanceThreshold; }
}
