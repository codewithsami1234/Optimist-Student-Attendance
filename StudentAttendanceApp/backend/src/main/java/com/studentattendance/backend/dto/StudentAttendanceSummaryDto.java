package com.studentattendance.backend.dto;

/**
 * Lightweight per-student attendance summary (no full record history) used
 * for the "low attendance" dashboard insight. Field names mirror
 * com.example.studentattendance.model.StudentAttendanceSummary on Android.
 */
public class StudentAttendanceSummaryDto {

    private String studentId;
    private String studentName;
    private String rollNumber;
    private String sectionId;
    private String sectionName;
    private int presentCount;
    private int totalMarked;
    private double attendanceRate;

    public StudentAttendanceSummaryDto() {
    }

    public StudentAttendanceSummaryDto(String studentId, String studentName, String rollNumber,
                                        String sectionId, String sectionName, int presentCount, int totalMarked) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.presentCount = presentCount;
        this.totalMarked = totalMarked;
        this.attendanceRate = totalMarked == 0 ? 0 : (presentCount * 100.0) / totalMarked;
    }

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
