package com.example.studentattendance.model;

import java.io.Serializable;
import java.util.List;

/**
 * Full attendance history + aggregated counts for one student, backing the
 * Student Attendance Report detail screen (Phase 3). Field names mirror
 * com.studentattendance.backend.dto.StudentAttendanceReportDto exactly for Gson.
 */
public class StudentAttendanceReport implements Serializable {

    private String studentId;
    private String studentName;
    private String rollNumber;
    private String departmentId;
    private String departmentName;
    private String semesterId;
    private String semesterName;
    private String sectionId;
    private String sectionName;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int leaveCount;
    private int totalMarked;
    private double attendanceRate;
    private int adjustmentDelta;
    private int adjustedPresentCount;
    private List<AttendanceRecord> records;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
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
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }
    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }
    public int getLeaveCount() { return leaveCount; }
    public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }
    public int getTotalMarked() { return totalMarked; }
    public void setTotalMarked(int totalMarked) { this.totalMarked = totalMarked; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    public int getAdjustmentDelta() { return adjustmentDelta; }
    public void setAdjustmentDelta(int adjustmentDelta) { this.adjustmentDelta = adjustmentDelta; }
    public int getAdjustedPresentCount() { return adjustedPresentCount; }
    public void setAdjustedPresentCount(int adjustedPresentCount) { this.adjustedPresentCount = adjustedPresentCount; }
    public List<AttendanceRecord> getRecords() { return records; }
    public void setRecords(List<AttendanceRecord> records) { this.records = records; }
}
