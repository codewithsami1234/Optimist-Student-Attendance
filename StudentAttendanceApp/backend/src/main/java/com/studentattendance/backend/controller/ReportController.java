package com.studentattendance.backend.controller;

import com.studentattendance.backend.entity.AttendanceSession;
import com.studentattendance.backend.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Raw data source for the Reports screen. Per-student and per-department
 * aggregation (StudentReport, attendance-rate breakdowns) is computed
 * client-side in ReportsViewModel exactly as in Phase 1 - this endpoint
 * simply supplies every saved session so that logic keeps working unchanged.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AttendanceService attendanceService;

    public ReportController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSession>> getSessionsForReports() {
        return ResponseEntity.ok(attendanceService.getSessionsForReports());
    }
}
