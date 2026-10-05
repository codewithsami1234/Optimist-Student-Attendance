package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Lightweight per-student attendance summary used for the dashboard's
 * "low attendance" insight. Field names mirror
 * com.studentattendance.backend.dto.StudentAttendanceSummaryDto exactly for Gson.
 */
public class StudentAttendanceSummary implements Serializable {

    private String studentId;
    private String studentName;
    private String rollNumber;
    private String sectionId;
    private String sectionName;
    private int presentCount;
    private int totalMarked;
    private double attendanceRate;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getTotalMarked() { return totalMarked; }
    public void setTotalMarked(int totalMarked) { this.totalMarked = totalMarked; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
}
