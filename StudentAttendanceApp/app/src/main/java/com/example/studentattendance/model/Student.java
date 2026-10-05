package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents a student enrolled in a specific department/semester/section.
 */
public class Student implements Serializable {

    private String id;
    private String rollNumber;
    private String name;
    private String departmentId;
    private String semesterId;
    private String sectionId;
    private String email;
    private String phone;
    private String avatarInitials;
    private boolean archived;

    public Student() {
    }

    public Student(String id, String rollNumber, String name, String departmentId,
                    String semesterId, String sectionId, String email, String phone) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.name = name;
        this.departmentId = departmentId;
        this.semesterId = semesterId;
        this.sectionId = sectionId;
        this.email = email;
        this.phone = phone;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.avatarInitials = computeInitials(name);
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public String toString() {
        if (rollNumber == null || rollNumber.isEmpty()) {
            return name == null ? "" : name;
        }
        return (name == null ? "" : name) + " · " + rollNumber;
    }
}
