package com.studentattendance.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 16)
    private String code;

    @Column(name = "total_students", nullable = false)
    private int totalStudents;

    @Column(name = "head_of_department")
    private String headOfDepartment;

    /** Phase 4: the professor this department/class-structure belongs to. Departments are private per account. */
    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(nullable = false)
    private boolean archived;

    public Department() {
    }

    public Department(String id, String name, String code, int totalStudents, String headOfDepartment, String ownerId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalStudents = totalStudents;
        this.headOfDepartment = headOfDepartment;
        this.ownerId = ownerId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public String getHeadOfDepartment() { return headOfDepartment; }
    public void setHeadOfDepartment(String headOfDepartment) { this.headOfDepartment = headOfDepartment; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
