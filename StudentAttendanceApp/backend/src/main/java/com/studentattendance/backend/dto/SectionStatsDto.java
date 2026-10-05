package com.studentattendance.backend.dto;

/**
 * Attendance statistics for a single section (Phase 3 "semester/section-wise
 * statistics"). Field names mirror
 * com.example.studentattendance.model.SectionStats on the Android side.
 */
public class SectionStatsDto {

    private String departmentId;
    private String departmentName;
    private String semesterId;
    private String semesterName;
    private String sectionId;
    private String sectionName;
    private int totalStudents;
    private int presentCount;
    private int totalMarked;
    private double attendanceRate;

    public SectionStatsDto() {
    }

    public SectionStatsDto(String departmentId, String departmentName, String semesterId, String semesterName,
                            String sectionId, String sectionName, int totalStudents, int presentCount, int totalMarked) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semesterId = semesterId;
        this.semesterName = semesterName;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.totalMarked = totalMarked;
        this.attendanceRate = totalMarked == 0 ? 0 : (presentCount * 100.0) / totalMarked;
    }

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
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getTotalMarked() { return totalMarked; }
    public void setTotalMarked(int totalMarked) { this.totalMarked = totalMarked; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
}
