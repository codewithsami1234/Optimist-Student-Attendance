package com.studentattendance.backend.dto;

/**
 * One point on the attendance-rate-over-time trend chart (Phase 3 "advanced
 * attendance analytics"). Field names mirror
 * com.example.studentattendance.model.DailyTrend on the Android side.
 */
public class DailyTrendDto {

    private String date;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int leaveCount;
    private int totalMarked;
    private double attendanceRate;

    public DailyTrendDto() {
    }

    public DailyTrendDto(String date, int presentCount, int absentCount, int lateCount, int leaveCount) {
        this.date = date;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
        this.leaveCount = leaveCount;
        this.totalMarked = presentCount + absentCount + lateCount + leaveCount;
        this.attendanceRate = com.studentattendance.backend.domain.AttendancePolicy.rate(
                presentCount, absentCount, lateCount, leaveCount, 0);
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
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
}
