package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.DepartmentCreateRequest;
import com.studentattendance.backend.dto.DepartmentUpdateRequest;
import com.studentattendance.backend.dto.SectionCreateRequest;
import com.studentattendance.backend.dto.SectionUpdateRequest;
import com.studentattendance.backend.dto.SemesterCreateRequest;
import com.studentattendance.backend.dto.SemesterUpdateRequest;
import com.studentattendance.backend.entity.Department;
import com.studentattendance.backend.entity.Section;
import com.studentattendance.backend.entity.Semester;
import com.studentattendance.backend.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentService.getDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    @PutMapping("/departments/{departmentId}")
    public ResponseEntity<Department> updateDepartment(@PathVariable String departmentId,
                                                        @RequestBody DepartmentUpdateRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, request));
    }

    @DeleteMapping("/departments/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments/{departmentId}/semesters")
    public ResponseEntity<List<Semester>> getSemesters(@PathVariable String departmentId) {
        return ResponseEntity.ok(departmentService.getSemesters(departmentId));
    }

    @PostMapping("/departments/{departmentId}/semesters")
    public ResponseEntity<Semester> createSemester(@PathVariable String departmentId,
                                                    @RequestBody SemesterCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createSemester(departmentId, request));
    }

    @PutMapping("/semesters/{semesterId}")
    public ResponseEntity<Semester> updateSemester(@PathVariable String semesterId,
                                                    @RequestBody SemesterUpdateRequest request) {
        return ResponseEntity.ok(departmentService.updateSemester(semesterId, request));
    }

    @DeleteMapping("/semesters/{semesterId}")
    public ResponseEntity<Void> deleteSemester(@PathVariable String semesterId) {
        departmentService.deleteSemester(semesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/semesters/{semesterId}/sections")
    public ResponseEntity<List<Section>> getSections(@PathVariable String semesterId) {
        return ResponseEntity.ok(departmentService.getSections(semesterId));
    }

    @PostMapping("/semesters/{semesterId}/sections")
    public ResponseEntity<Section> createSection(@PathVariable String semesterId,
                                                  @RequestBody SectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createSection(semesterId, request));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<Section> updateSection(@PathVariable String sectionId,
                                                  @RequestBody SectionUpdateRequest request) {
        return ResponseEntity.ok(departmentService.updateSection(sectionId, request));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable String sectionId) {
        departmentService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }
}
