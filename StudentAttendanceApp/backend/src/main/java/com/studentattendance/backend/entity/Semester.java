package com.studentattendance.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters", uniqueConstraints = {
        @UniqueConstraint(name = "uq_semesters_department_number", columnNames = {"department_id", "semester_number"})
})
public class Semester {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    @Column(nullable = false)
    private String name;

    @Column(name = "semester_number", nullable = false)
    private int semesterNumber;

    @Column(nullable = false)
    private boolean archived;

    public Semester() {
    }

    public Semester(String id, String departmentId, String name, int semesterNumber) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.semesterNumber = semesterNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
