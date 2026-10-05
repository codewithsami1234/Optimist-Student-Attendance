package com.example.studentattendance.model;

import java.io.Serializable;
import java.util.List;

/**
 * Phase 3 consolidated dashboard insights: today's snapshot, a 7-day
 * attendance trend, and a low-attendance early warning list, all computed
 * server-side in a single call. Field names mirror
 * com.studentattendance.backend.dto.DashboardStatsDto exactly for Gson.
 */
public class DashboardStats implements Serializable {

    private int totalStudents;
    private int presentToday;
    private int absentToday;
    private double attendanceRateToday;
    private List<AttendanceSession> recentSessions;
    private List<DailyTrend> weeklyTrend;
    private List<StudentAttendanceSummary> lowAttendanceStudents;
    private List<DailyTrend> monthlyTrend;
    private List<Section> chartSections;
    private List<Student> chartStudents;
    private int leaveToday;
    private int lateToday;
    private double lowAttendanceThreshold;

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getPresentToday() { return presentToday; }
    public void setPresentToday(int presentToday) { this.presentToday = presentToday; }
    public int getAbsentToday() { return absentToday; }
    public void setAbsentToday(int absentToday) { this.absentToday = absentToday; }
    public double getAttendanceRateToday() { return attendanceRateToday; }
    public void setAttendanceRateToday(double attendanceRateToday) { this.attendanceRateToday = attendanceRateToday; }
    public List<AttendanceSession> getRecentSessions() { return recentSessions; }
    public void setRecentSessions(List<AttendanceSession> recentSessions) { this.recentSessions = recentSessions; }
    public List<DailyTrend> getWeeklyTrend() { return weeklyTrend; }
    public void setWeeklyTrend(List<DailyTrend> weeklyTrend) { this.weeklyTrend = weeklyTrend; }
    public List<StudentAttendanceSummary> getLowAttendanceStudents() { return lowAttendanceStudents; }
    public void setLowAttendanceStudents(List<StudentAttendanceSummary> lowAttendanceStudents) {
        this.lowAttendanceStudents = lowAttendanceStudents;
    }
    public int getLeaveToday() { return leaveToday; }
    public void setLeaveToday(int leaveToday) { this.leaveToday = leaveToday; }
    public int getLateToday() { return lateToday; }
    public void setLateToday(int lateToday) { this.lateToday = lateToday; }
    public double getLowAttendanceThreshold() { return lowAttendanceThreshold <= 0 ? 75.0 : lowAttendanceThreshold; }
    public void setLowAttendanceThreshold(double lowAttendanceThreshold) {
        this.lowAttendanceThreshold = lowAttendanceThreshold;
    }
    public List<DailyTrend> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<DailyTrend> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
    public List<Section> getChartSections() { return chartSections; }
    public void setChartSections(List<Section> chartSections) { this.chartSections = chartSections; }
    public List<Student> getChartStudents() { return chartStudents; }
    public void setChartStudents(List<Student> chartStudents) { this.chartStudents = chartStudents; }
}
