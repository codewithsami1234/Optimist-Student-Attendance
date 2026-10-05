package com.studentattendance.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Body for POST /api/attendance - mirrors AttendanceRepository#saveSession on Android. */
public class AttendanceSaveRequest {

    @NotBlank(message = "departmentId is required")
    private String departmentId;
    @NotBlank(message = "departmentName is required")
    private String departmentName;
    @NotBlank(message = "semesterId is required")
    private String semesterId;
    @NotBlank(message = "semesterName is required")
    private String semesterName;
    @NotBlank(message = "sectionId is required")
    private String sectionId;
    @NotBlank(message = "sectionName is required")
    private String sectionName;
    private String subjectId;
    private String subjectName;
    @NotBlank(message = "date is required")
    private String date;

    @NotEmpty(message = "There are no students to save attendance for.")
    @Valid
    private List<AttendanceRecordInput> records;

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
    public List<AttendanceRecordInput> getRecords() { return records; }
    public void setRecords(List<AttendanceRecordInput> records) { this.records = records; }
}
