package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents an authenticated app user (teacher / admin).
 */
public class User implements Serializable {

    private String id;
    private String name;
    private String email;
    private String role; // e.g. "Teacher", "Admin"
    private String designation;
    private String avatarInitials;
    private double lowAttendanceThreshold = 75.0;

    public User() {
    }

    public User(String id, String name, String email, String role, String designation) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.designation = designation;
        this.avatarInitials = computeInitials(name);
    }

    private String computeInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.avatarInitials = computeInitials(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public double getLowAttendanceThreshold() {
        return lowAttendanceThreshold <= 0 ? 75.0 : lowAttendanceThreshold;
    }

    public void setLowAttendanceThreshold(double lowAttendanceThreshold) {
        this.lowAttendanceThreshold = lowAttendanceThreshold;
    }
}
