package com.studentattendance.backend.service;

import com.studentattendance.backend.dto.SubjectCreateRequest;
import com.studentattendance.backend.dto.SubjectUpdateRequest;
import com.studentattendance.backend.entity.Subject;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.exception.ResourceNotFoundException;
import com.studentattendance.backend.repository.AttendanceSessionRepository;
import com.studentattendance.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentService departmentService;
    private final AttendanceSessionRepository sessionRepository;

    public SubjectService(SubjectRepository subjectRepository, DepartmentService departmentService,
                           AttendanceSessionRepository sessionRepository) {
        this.subjectRepository = subjectRepository;
        this.departmentService = departmentService;
        this.sessionRepository = sessionRepository;
    }

    public List<Subject> getSubjects(String departmentId, boolean includeArchived) {
        departmentService.requireOwnedDepartment(departmentId);
        departmentService.ensureGeneralSubject(departmentId);
        if (includeArchived) {
            return subjectRepository.findByDepartmentIdOrderByNameAsc(departmentId);
        }
        return subjectRepository.findByDepartmentIdAndArchivedFalseOrderByNameAsc(departmentId);
    }

    @Transactional
    public Subject createSubject(String departmentId, SubjectCreateRequest request) {
        departmentService.requireOwnedDepartment(departmentId);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Subject name is required.");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BadRequestException("Subject code is required.");
        }
        String code = request.getCode().trim().toUpperCase();
        if (subjectRepository.existsByDepartmentIdAndCodeIgnoreCase(departmentId, code)) {
            throw new BadRequestException("This department already has a subject with that code.");
        }
        Subject subject = new Subject("subj_" + UUID.randomUUID(), departmentId, request.getName().trim(), code);
        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject updateSubject(String subjectId, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        departmentService.requireOwnedDepartment(subject.getDepartmentId());
        if (DepartmentService.GENERAL_SUBJECT_CODE.equalsIgnoreCase(subject.getCode())
                && Boolean.TRUE.equals(request.getArchived())) {
            throw new BadRequestException("The General subject cannot be archived.");
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            subject.setName(request.getName().trim());
        }
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String code = request.getCode().trim().toUpperCase();
            if (!code.equalsIgnoreCase(subject.getCode())
                    && subjectRepository.existsByDepartmentIdAndCodeIgnoreCase(subject.getDepartmentId(), code)) {
                throw new BadRequestException("This department already has a subject with that code.");
            }
            if (DepartmentService.GENERAL_SUBJECT_CODE.equalsIgnoreCase(subject.getCode())
                    && !DepartmentService.GENERAL_SUBJECT_CODE.equalsIgnoreCase(code)) {
                throw new BadRequestException("The General subject code cannot be changed.");
            }
            subject.setCode(code);
        }
        if (request.getArchived() != null) {
            subject.setArchived(request.getArchived());
        }
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(String subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        departmentService.requireOwnedDepartment(subject.getDepartmentId());
        if (DepartmentService.GENERAL_SUBJECT_CODE.equalsIgnoreCase(subject.getCode())) {
            throw new BadRequestException("The General subject cannot be deleted.");
        }
        if (sessionRepository.existsBySubjectId(subjectId)) {
            throw new BadRequestException("This subject has attendance sessions. Archive it instead of deleting.");
        }
        subjectRepository.delete(subject);
    }

    public Subject requireOwned(String subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        departmentService.requireOwnedDepartment(subject.getDepartmentId());
        return subject;
    }
}
