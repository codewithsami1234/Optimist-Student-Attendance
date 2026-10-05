package com.studentattendance.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * A single student status entry within an AttendanceSession. Field names
 * mirror com.example.studentattendance.model.AttendanceRecord exactly.
 * <p>
 * Note: the {@code session} back-reference below exists purely for JPA's
 * bidirectional mapping and has no counterpart on the Android
 * AttendanceRecord model. It is marked {@code @JsonIgnore} so that Jackson
 * doesn't try to serialize it - without this, returning an AttendanceSession
 * (which eagerly fetches its records) from a controller recurses forever
 * (session -> records -> record.session -> records -> ...) and blows the
 * stack on every save/history/report request.
 */
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @Column(name = "id", length = 128)
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @Column(name = "student_id", nullable = false, length = 64)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "roll_number", nullable = false, length = 32)
    private String rollNumber;

    @Column(name = "section_id", nullable = false, length = 64)
    private String sectionId;

    @Column(name = "semester_id", nullable = false, length = 64)
    private String semesterId;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    @Column(name = "subject_id", length = 64)
    private String subjectId;

    @Column(name = "attendance_date", nullable = false, length = 10)
    private String date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AttendanceStatus status;

    @Column(name = "marked_by")
    private String markedBy;

    @Column(name = "marked_at_millis")
    private long markedAtMillis;

    public AttendanceRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public AttendanceSession getSession() { return session; }
    public void setSession(AttendanceSession session) { this.session = session; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }
    public long getMarkedAtMillis() { return markedAtMillis; }
    public void setMarkedAtMillis(long markedAtMillis) { this.markedAtMillis = markedAtMillis; }
}
