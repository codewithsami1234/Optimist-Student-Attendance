package com.studentattendance.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"section_id", "roll_number"})
})
public class Student {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "roll_number", nullable = false, length = 32)
    private String rollNumber;

    @Column(nullable = false)
    private String name;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    @Column(name = "semester_id", nullable = false, length = 64)
    private String semesterId;

    @Column(name = "section_id", nullable = false, length = 64)
    private String sectionId;

    private String email;

    private String phone;

    @Column(name = "avatar_initials", length = 4)
    private String avatarInitials;

    @Column(nullable = false)
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

    public static String computeInitials(String name) {
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.avatarInitials = computeInitials(name); }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
