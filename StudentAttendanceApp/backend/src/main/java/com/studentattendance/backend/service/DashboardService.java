package com.studentattendance.backend.service;

import com.studentattendance.backend.domain.AttendancePolicy;
import com.studentattendance.backend.dto.DashboardStatsDto;
import com.studentattendance.backend.entity.AttendanceRecord;
import com.studentattendance.backend.entity.AttendanceSession;
import com.studentattendance.backend.entity.AttendanceStatus;
import com.studentattendance.backend.entity.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final AnalyticsService analyticsService;

    public DashboardService(StudentService studentService, AttendanceService attendanceService,
                             AnalyticsService analyticsService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.analyticsService = analyticsService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        List<Student> students = studentService.getAllStudents();
        List<AttendanceSession> sessions = attendanceService.getSessions(null, null, null, null, null, null);

        String today = LocalDate.now().toString();
        int present = 0, absent = 0, late = 0, leave = 0;
        for (AttendanceSession session : sessions) {
            if (!today.equals(session.getDate())) {
                continue;
            }
            for (AttendanceRecord record : session.getRecords()) {
                AttendanceStatus status = record.getStatus();
                if (status == AttendanceStatus.PRESENT) present++;
                else if (status == AttendanceStatus.ABSENT) absent++;
                else if (status == AttendanceStatus.LATE) late++;
                else if (status == AttendanceStatus.LEAVE) leave++;
            }
        }
        double rate = AttendancePolicy.rate(present, absent, late, leave, 0);
        int presentToday = present + late;

        int limit = Math.min(5, sessions.size());
        List<AttendanceSession> recent = sessions.subList(0, limit);

        double threshold = analyticsService.currentThreshold();
        DashboardStatsDto dto = new DashboardStatsDto(students.size(), presentToday, absent, rate, recent,
                analyticsService.getTrend(null, null, 7),
                analyticsService.getLowAttendance(threshold, 5));
        dto.setLeaveToday(leave);
        dto.setLateToday(late);
        dto.setLowAttendanceThreshold(threshold);
        return dto;
    }
}
