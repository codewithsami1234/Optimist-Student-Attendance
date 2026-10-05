package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.DailyTrendDto;
import com.studentattendance.backend.dto.SectionStatsDto;
import com.studentattendance.backend.dto.StudentAttendanceSummaryDto;
import com.studentattendance.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/section-stats")
    public ResponseEntity<List<SectionStatsDto>> getSectionStats(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String semesterId,
            @RequestParam(required = false) String subjectId) {
        return ResponseEntity.ok(analyticsService.getSectionStats(departmentId, semesterId, subjectId));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<DailyTrendDto>> getTrend(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String subjectId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(analyticsService.getTrend(departmentId, subjectId, days));
    }

    @GetMapping("/low-attendance")
    public ResponseEntity<List<StudentAttendanceSummaryDto>> getLowAttendance(
            @RequestParam(required = false) Double threshold,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getLowAttendance(threshold, limit));
    }
}
