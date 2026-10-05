package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * One day's attendance-rate trend point. Field names mirror
 * com.studentattendance.backend.dto.DailyTrendDto exactly for Gson.
 */
public class DailyTrend implements Serializable {

    private String date;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int leaveCount;
    private int totalMarked;
    private double attendanceRate;

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
