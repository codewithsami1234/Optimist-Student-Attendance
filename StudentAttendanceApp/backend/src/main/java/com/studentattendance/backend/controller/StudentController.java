package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.ImportResultDto;
import com.studentattendance.backend.dto.StudentAttendanceReportDto;
import com.studentattendance.backend.dto.StudentTransferRequest;
import com.studentattendance.backend.dto.StudentUpdateRequest;
import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.service.AnalyticsService;
import com.studentattendance.backend.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final AnalyticsService analyticsService;

    public StudentController(StudentService studentService, AnalyticsService analyticsService) {
        this.studentService = studentService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{studentId}/report")
    public ResponseEntity<StudentAttendanceReportDto> getStudentReport(@PathVariable String studentId) {
        return ResponseEntity.ok(analyticsService.getStudentReport(studentId));
    }

    @GetMapping
    public ResponseEntity<List<Student>> getStudents(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String semesterId,
            @RequestParam(required = false) String sectionId,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        return ResponseEntity.ok(studentService.getStudents(departmentId, semesterId, sectionId, includeArchived));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudents(
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        return ResponseEntity.ok(includeArchived
                ? studentService.getAllStudentsIncludingArchived()
                : studentService.getAllStudents());
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.addStudent(student));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable String studentId,
                                                  @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(studentId, request));
    }

    @PostMapping("/{studentId}/transfer")
    public ResponseEntity<Student> transferStudent(@PathVariable String studentId,
                                                    @Valid @RequestBody StudentTransferRequest request) {
        return ResponseEntity.ok(studentService.transferStudent(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importStudents(@RequestBody List<Student> students) {
        return ResponseEntity.ok(studentService.importStudents(students));
    }
}
