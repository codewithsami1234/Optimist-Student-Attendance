package com.studentattendance.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sections", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sections_semester_name", columnNames = {"semester_id", "name"})
})
public class Section {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "semester_id", nullable = false, length = 64)
    private String semesterId;

    @Column(nullable = false)
    private String name;

    @Column(name = "student_count", nullable = false)
    private int studentCount;

    @Column(nullable = false)
    private boolean archived;

    public Section() {
    }

    public Section(String id, String semesterId, String name, int studentCount) {
        this.id = id;
        this.semesterId = semesterId;
        this.name = name;
        this.studentCount = studentCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
