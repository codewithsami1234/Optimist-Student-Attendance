package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.AttendanceAdjustmentRequest;
import com.studentattendance.backend.entity.AttendanceAdjustment;
import com.studentattendance.backend.service.AttendanceAdjustmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher-controlled attendance relief/adjustment. Every change here is a
 * new, permanent audit-trail row (see AttendanceAdjustment) - nothing here
 * ever edits an original AttendanceRecord.
 */
@RestController
@RequestMapping("/api/students/{studentId}/adjustments")
public class AttendanceAdjustmentController {

    private final AttendanceAdjustmentService adjustmentService;

    public AttendanceAdjustmentController(AttendanceAdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    @PostMapping
    public ResponseEntity<AttendanceAdjustment> createAdjustment(@PathVariable String studentId,
                                                                   @RequestBody AttendanceAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adjustmentService.createAdjustment(studentId, request));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceAdjustment>> getHistory(@PathVariable String studentId) {
        return ResponseEntity.ok(adjustmentService.getHistory(studentId));
    }
}
