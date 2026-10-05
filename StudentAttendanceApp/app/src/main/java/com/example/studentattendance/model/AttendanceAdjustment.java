package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * A single teacher-made attendance relief/adjustment, permanently recorded.
 * Field names mirror com.studentattendance.backend.entity.AttendanceAdjustment exactly for Gson.
 */
public class AttendanceAdjustment implements Serializable {
    private String id;
    private String studentId;
    private String departmentId;
    private int delta;
    private String reason;
    private String adjustedByUserId;
    private String adjustedByName;
    private long createdAtMillis;

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
