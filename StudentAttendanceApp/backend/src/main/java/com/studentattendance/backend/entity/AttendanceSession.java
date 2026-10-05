package com.studentattendance.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A saved attendance-taking session: one department + semester + section +
 * date, and the individual student records within it. Field names mirror
 * com.example.studentattendance.model.AttendanceSession exactly so the
 * Android Gson converter can deserialize this response directly into that
 * class with no manual DTO mapping.
 */
@Entity
@Table(name = "attendance_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sessions_section_subject_date",
                columnNames = {"section_id", "subject_id", "attendance_date"})
})
public class AttendanceSession {

    @Id
    @Column(name = "id", length = 128)
    private String id;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "semester_id", nullable = false, length = 64)
    private String semesterId;

    @Column(name = "semester_name", nullable = false)
    private String semesterName;

    @Column(name = "section_id", nullable = false, length = 64)
    private String sectionId;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "subject_id", nullable = false, length = 64)
    private String subjectId;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "attendance_date", nullable = false, length = 10)
    private String date;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AttendanceRecord> records = new ArrayList<>();

    @Column(name = "created_at_millis")
    private long createdAtMillis;

    public AttendanceSession() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<AttendanceRecord> getRecords() { return records; }
    public void setRecords(List<AttendanceRecord> records) { this.records = records; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }

    public int getPresentCount() { return countByStatus(AttendanceStatus.PRESENT); }
    public int getAbsentCount() { return countByStatus(AttendanceStatus.ABSENT); }
    public int getLateCount() { return countByStatus(AttendanceStatus.LATE); }
    public int getLeaveCount() { return countByStatus(AttendanceStatus.LEAVE); }

    private int countByStatus(AttendanceStatus status) {
        int count = 0;
        for (AttendanceRecord r : records) {
            if (r.getStatus() == status) {
                count++;
            }
        }
        return count;
    }
}
