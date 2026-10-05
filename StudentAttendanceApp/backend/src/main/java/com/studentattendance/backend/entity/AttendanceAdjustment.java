package com.studentattendance.backend.entity;

import jakarta.persistence.*;

/**
 * A single attendance relief/adjustment a professor applies to a student's
 * attendance count for a genuine reason (medical leave, a technical error
 * during marking, an approved excused absence, etc). This table is
 * append-only: creating an adjustment never edits or deletes any
 * {@link AttendanceRecord} row. Every student's effective attendance rate
 * is instead computed as (raw records) + (sum of their adjustment deltas),
 * so the original day-by-day history stays a perfect, untouched audit
 * trail and every relief is itself its own permanent audit entry.
 */
@Entity
@Table(name = "attendance_adjustments")
public class AttendanceAdjustment {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "student_id", nullable = false, length = 64)
    private String studentId;

    @Column(name = "department_id", nullable = false, length = 64)
    private String departmentId;

    /** Positive to credit extra present days, negative to revoke previously-credited ones. Never zero. */
    @Column(nullable = false)
    private int delta;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "adjusted_by_user_id", nullable = false, length = 64)
    private String adjustedByUserId;

    @Column(name = "adjusted_by_name", nullable = false)
    private String adjustedByName;

    @Column(name = "created_at_millis", nullable = false)
    private long createdAtMillis;

    public AttendanceAdjustment() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAdjustedByUserId() { return adjustedByUserId; }
    public void setAdjustedByUserId(String adjustedByUserId) { this.adjustedByUserId = adjustedByUserId; }
    public String getAdjustedByName() { return adjustedByName; }
    public void setAdjustedByName(String adjustedByName) { this.adjustedByName = adjustedByName; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
}
