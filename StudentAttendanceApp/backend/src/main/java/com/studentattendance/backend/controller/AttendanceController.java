package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.AttendanceSaveRequest;
import com.studentattendance.backend.entity.AttendanceSession;
import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<AttendanceSession> saveSession(@AuthenticationPrincipal User currentUser,
                                                           @Valid @RequestBody AttendanceSaveRequest request) {
        String markedBy = currentUser != null ? currentUser.getName() : null;
        return ResponseEntity.ok(attendanceService.saveSession(request, markedBy));
    }

    @GetMapping("/session")
    public ResponseEntity<AttendanceSession> getSession(
            @RequestParam String departmentId,
            @RequestParam String semesterId,
            @RequestParam String sectionId,
            @RequestParam String date,
            @RequestParam(required = false) String subjectId) {
        AttendanceSession session = attendanceService.getSession(departmentId, semesterId, sectionId, subjectId, date);
        return session == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSession>> getSessions(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String semesterId,
            @RequestParam(required = false) String sectionId,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(attendanceService.getSessionsPaged(
                departmentId, semesterId, sectionId, subjectId, fromDate, toDate, page, size));
    }
}
