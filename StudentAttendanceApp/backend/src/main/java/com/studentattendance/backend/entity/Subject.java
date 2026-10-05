package com.studentattendance.backend.entity;

import jakarta.persistence.*;

/**
 * A course/subject taught in a department (e.g. "Data Structures", or the
 * default "General" subject used for homeroom-style section-day attendance).
 */
@Entity
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(name = "uq_subjects_department_code", columnNames = {"department_id", "code"})
})
public class Subject {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false)
    private boolean archived;

    public Subject() {
    }

    public Subject(String id, String departmentId, String name, String code) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
        this.archived = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
