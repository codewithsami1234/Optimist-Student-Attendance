package com.studentattendance.backend.dto;

import com.studentattendance.backend.entity.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** One row inside an AttendanceSaveRequest. */
public class AttendanceRecordInput {

    @NotBlank(message = "studentId is required")
    private String studentId;

    private String studentName;
    private String rollNumber;

    @NotNull(message = "status is required for every student")
    private AttendanceStatus status;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
