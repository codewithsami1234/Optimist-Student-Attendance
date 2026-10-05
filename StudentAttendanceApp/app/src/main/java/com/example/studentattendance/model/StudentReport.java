package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Aggregated attendance statistics for a single student, used on the Reports screen.
 */
public class StudentReport implements Serializable {

    private String studentId;
    private String studentName;
    private String rollNumber;
    private int presentDays;
    private int absentDays;
    private int lateDays;
    private int leaveDays;
    private int totalDays;
    private int adjustmentDelta;
    private int weekPresent;
    private int weekAbsent;
    private int weekLate;
    private int weekLeave;
    private int monthPresent;
    private int monthAbsent;
    private int monthLate;
    private int monthLeave;

    public StudentReport(String studentId, String studentName, String rollNumber) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
    }

    public void increment(AttendanceStatus status) {
        increment(status, null, null, null);
    }

    public void increment(AttendanceStatus status, String date, String weekFrom, String monthFrom) {
        totalDays++;
        switch (status) {
            case PRESENT:
                presentDays++;
                break;
            case ABSENT:
                absentDays++;
                break;
            case LATE:
                lateDays++;
                break;
            case LEAVE:
                leaveDays++;
                break;
            default:
                break;
        }
        if (date == null) {
            return;
        }
        if (weekFrom != null && date.compareTo(weekFrom) >= 0) {
            bumpWeek(status);
        }
        if (monthFrom != null && date.compareTo(monthFrom) >= 0) {
            bumpMonth(status);
        }
    }

    private void bumpWeek(AttendanceStatus status) {
        switch (status) {
            case PRESENT:
                weekPresent++;
                break;
            case ABSENT:
                weekAbsent++;
                break;
            case LATE:
                weekLate++;
                break;
            case LEAVE:
                weekLeave++;
                break;
            default:
                break;
        }
    }

    private void bumpMonth(AttendanceStatus status) {
        switch (status) {
            case PRESENT:
                monthPresent++;
                break;
            case ABSENT:
                monthAbsent++;
                break;
            case LATE:
                monthLate++;
                break;
            case LEAVE:
                monthLeave++;
                break;
            default:
                break;
        }
    }

    public double getAttendancePercentage() {
        return com.example.studentattendance.utils.AttendancePolicy.rate(
                presentDays, absentDays, lateDays, leaveDays, adjustmentDelta);
    }

    public double getWeeklyPercentage() {
        return com.example.studentattendance.utils.AttendancePolicy.rate(
                weekPresent, weekAbsent, weekLate, weekLeave, 0);
    }

    public double getMonthlyPercentage() {
        return com.example.studentattendance.utils.AttendancePolicy.rate(
                monthPresent, monthAbsent, monthLate, monthLeave, 0);
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public int getLateDays() {
        return lateDays;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public int getAdjustmentDelta() {
        return adjustmentDelta;
    }

    public void setAdjustmentDelta(int adjustmentDelta) {
        this.adjustmentDelta = adjustmentDelta;
    }
}
