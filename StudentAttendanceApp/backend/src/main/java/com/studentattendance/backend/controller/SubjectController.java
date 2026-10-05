package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.SubjectCreateRequest;
import com.studentattendance.backend.dto.SubjectUpdateRequest;
import com.studentattendance.backend.entity.Subject;
import com.studentattendance.backend.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/departments/{departmentId}/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@PathVariable String departmentId,
                                                      @RequestParam(defaultValue = "false") boolean includeArchived) {
        return ResponseEntity.ok(subjectService.getSubjects(departmentId, includeArchived));
    }

    @PostMapping("/departments/{departmentId}/subjects")
    public ResponseEntity<Subject> createSubject(@PathVariable String departmentId,
                                                  @RequestBody SubjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(departmentId, request));
    }

    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<Subject> updateSubject(@PathVariable String subjectId,
                                                  @RequestBody SubjectUpdateRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(subjectId, request));
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String subjectId) {
        subjectService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }
}
