package com.studentattendance.backend.dto;

import com.studentattendance.backend.entity.AttendanceSession;
import java.util.List;

/**
 * Phase 3: dashboard insights. In addition to the Phase 2 "today" snapshot,
 * now also carries a 7-day attendance trend and a low-attendance early
 * warning list so the Dashboard screen can render real analytics instead of
 * aggregating every session client-side.
 */
public class DashboardStatsDto {
    private int totalStudents;
    private int presentToday;
    private int absentToday;
    private double attendanceRateToday;
    private List<AttendanceSession> recentSessions;
    private List<DailyTrendDto> weeklyTrend;
    private List<StudentAttendanceSummaryDto> lowAttendanceStudents;
    private int leaveToday;
    private int lateToday;
    private double lowAttendanceThreshold;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(int totalStudents, int presentToday, int absentToday,
                              double attendanceRateToday, List<AttendanceSession> recentSessions,
                              List<DailyTrendDto> weeklyTrend, List<StudentAttendanceSummaryDto> lowAttendanceStudents) {
        this.totalStudents = totalStudents;
        this.presentToday = presentToday;
        this.absentToday = absentToday;
        this.attendanceRateToday = attendanceRateToday;
        this.recentSessions = recentSessions;
        this.weeklyTrend = weeklyTrend;
        this.lowAttendanceStudents = lowAttendanceStudents;
    }

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
    public List<DailyTrendDto> getWeeklyTrend() { return weeklyTrend; }
    public void setWeeklyTrend(List<DailyTrendDto> weeklyTrend) { this.weeklyTrend = weeklyTrend; }
    public List<StudentAttendanceSummaryDto> getLowAttendanceStudents() { return lowAttendanceStudents; }
    public void setLowAttendanceStudents(List<StudentAttendanceSummaryDto> lowAttendanceStudents) { this.lowAttendanceStudents = lowAttendanceStudents; }
    public int getLeaveToday() { return leaveToday; }
    public void setLeaveToday(int leaveToday) { this.leaveToday = leaveToday; }
    public int getLateToday() { return lateToday; }
    public void setLateToday(int lateToday) { this.lateToday = lateToday; }
    public double getLowAttendanceThreshold() { return lowAttendanceThreshold; }
    public void setLowAttendanceThreshold(double lowAttendanceThreshold) { this.lowAttendanceThreshold = lowAttendanceThreshold; }
}
