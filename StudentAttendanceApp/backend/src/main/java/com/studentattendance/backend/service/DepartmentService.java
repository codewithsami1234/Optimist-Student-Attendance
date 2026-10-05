package com.studentattendance.backend.service;

import com.studentattendance.backend.dto.DepartmentCreateRequest;
import com.studentattendance.backend.dto.DepartmentUpdateRequest;
import com.studentattendance.backend.dto.SectionCreateRequest;
import com.studentattendance.backend.dto.SectionUpdateRequest;
import com.studentattendance.backend.dto.SemesterCreateRequest;
import com.studentattendance.backend.dto.SemesterUpdateRequest;
import com.studentattendance.backend.entity.Department;
import com.studentattendance.backend.entity.Section;
import com.studentattendance.backend.entity.Semester;
import com.studentattendance.backend.entity.Subject;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.exception.ForbiddenException;
import com.studentattendance.backend.exception.ResourceNotFoundException;
import com.studentattendance.backend.repository.DepartmentRepository;
import com.studentattendance.backend.repository.SectionRepository;
import com.studentattendance.backend.repository.SemesterRepository;
import com.studentattendance.backend.repository.StudentRepository;
import com.studentattendance.backend.repository.SubjectRepository;
import com.studentattendance.backend.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    public static final String GENERAL_SUBJECT_CODE = "GEN";
    public static final String GENERAL_SUBJECT_NAME = "General";

    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final AuthContext authContext;

    public DepartmentService(DepartmentRepository departmentRepository, SemesterRepository semesterRepository,
                              SectionRepository sectionRepository, SubjectRepository subjectRepository,
                              StudentRepository studentRepository, AuthContext authContext) {
        this.departmentRepository = departmentRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.authContext = authContext;
    }

    public List<Department> getDepartments() {
        return departmentRepository.findByOwnerIdOrderByNameAsc(authContext.currentUserId());
    }

    @Transactional
    public Department createDepartment(DepartmentCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Department name is required.");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BadRequestException("Department code is required.");
        }
        String ownerId = authContext.currentUserId();
        String code = request.getCode().trim().toUpperCase();
        if (departmentRepository.existsByOwnerIdAndCodeIgnoreCase(ownerId, code)) {
            throw new BadRequestException("You already have a department with this code.");
        }
        Department department = new Department("dept_" + UUID.randomUUID(), request.getName().trim(),
                code, 0, request.getHeadOfDepartment(), ownerId);
        Department saved = departmentRepository.save(department);
        ensureGeneralSubject(saved.getId());
        return saved;
    }

    @Transactional
    public Department updateDepartment(String departmentId, DepartmentUpdateRequest request) {
        Department department = requireOwnedDepartment(departmentId);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            department.setName(request.getName().trim());
        }
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String code = request.getCode().trim().toUpperCase();
            if (departmentRepository.existsByOwnerIdAndCodeIgnoreCaseAndIdNot(
                    authContext.currentUserId(), code, departmentId)) {
                throw new BadRequestException("You already have a department with this code.");
            }
            department.setCode(code);
        }
        if (request.getHeadOfDepartment() != null) {
            department.setHeadOfDepartment(request.getHeadOfDepartment().trim());
        }
        if (request.getArchived() != null) {
            department.setArchived(request.getArchived());
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(String departmentId) {
        Department department = requireOwnedDepartment(departmentId);
        long students = studentRepository.countByDepartmentIdAndArchivedFalse(departmentId);
        if (students > 0) {
            throw new BadRequestException("Archive or move students before deleting this department.");
        }
        departmentRepository.delete(department);
    }

    public List<Semester> getSemesters(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            throw new BadRequestException("Please select a department first.");
        }
        requireOwnedDepartment(departmentId);
        return semesterRepository.findByDepartmentIdOrderBySemesterNumberAsc(departmentId);
    }

    @Transactional
    public Semester createSemester(String departmentId, SemesterCreateRequest request) {
        requireOwnedDepartment(departmentId);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Semester name is required.");
        }
        if (request.getSemesterNumber() < 1) {
            throw new BadRequestException("Semester number must be 1 or greater.");
        }
        if (semesterRepository.existsByDepartmentIdAndSemesterNumber(departmentId, request.getSemesterNumber())) {
            throw new BadRequestException("This department already has that semester number.");
        }
        Semester semester = new Semester("sem_" + UUID.randomUUID(), departmentId, request.getName().trim(),
                request.getSemesterNumber());
        return semesterRepository.save(semester);
    }

    @Transactional
    public Semester updateSemester(String semesterId, SemesterUpdateRequest request) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found: " + semesterId));
        requireOwnedDepartment(semester.getDepartmentId());
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            semester.setName(request.getName().trim());
        }
        if (request.getSemesterNumber() != null) {
            if (request.getSemesterNumber() < 1) {
                throw new BadRequestException("Semester number must be 1 or greater.");
            }
            if (request.getSemesterNumber() != semester.getSemesterNumber()
                    && semesterRepository.existsByDepartmentIdAndSemesterNumber(
                    semester.getDepartmentId(), request.getSemesterNumber())) {
                throw new BadRequestException("This department already has that semester number.");
            }
            semester.setSemesterNumber(request.getSemesterNumber());
        }
        if (request.getArchived() != null) {
            semester.setArchived(request.getArchived());
        }
        return semesterRepository.save(semester);
    }

    @Transactional
    public void deleteSemester(String semesterId) {
        requireOwnedSemester(semesterId);
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found: " + semesterId));
        if (sectionRepository.countBySemesterId(semesterId) > 0) {
            throw new BadRequestException("Delete or archive sections in this semester first.");
        }
        semesterRepository.delete(semester);
    }

    public List<Section> getSections(String semesterId) {
        if (semesterId == null || semesterId.isBlank()) {
            throw new BadRequestException("Please select a semester first.");
        }
        requireOwnedSemester(semesterId);
        return sectionRepository.findBySemesterIdOrderByNameAsc(semesterId);
    }

    @Transactional
    public Section createSection(String semesterId, SectionCreateRequest request) {
        requireOwnedSemester(semesterId);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Section name is required.");
        }
        String name = request.getName().trim();
        if (sectionRepository.existsBySemesterIdAndNameIgnoreCase(semesterId, name)) {
            throw new BadRequestException("This semester already has a section with that name.");
        }
        Section section = new Section("sec_" + UUID.randomUUID(), semesterId, name, 0);
        return sectionRepository.save(section);
    }

    @Transactional
    public Section updateSection(String sectionId, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
        requireOwnedSemester(section.getSemesterId());
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = request.getName().trim();
            if (!name.equalsIgnoreCase(section.getName())
                    && sectionRepository.existsBySemesterIdAndNameIgnoreCase(section.getSemesterId(), name)) {
                throw new BadRequestException("This semester already has a section with that name.");
            }
            section.setName(name);
        }
        if (request.getArchived() != null) {
            section.setArchived(request.getArchived());
        }
        return sectionRepository.save(section);
    }

    @Transactional
    public void deleteSection(String sectionId) {
        requireOwnedSection(sectionId);
        if (studentRepository.countBySectionId(sectionId) > 0) {
            throw new BadRequestException("Archive or move students before deleting this section.");
        }
        sectionRepository.deleteById(sectionId);
    }

    @Transactional
    public Subject ensureGeneralSubject(String departmentId) {
        return subjectRepository.findByDepartmentIdAndCodeIgnoreCase(departmentId, GENERAL_SUBJECT_CODE)
                .orElseGet(() -> subjectRepository.save(new Subject(
                        "subj_gen_" + departmentId, departmentId, GENERAL_SUBJECT_NAME, GENERAL_SUBJECT_CODE)));
    }

    public Department requireOwnedDepartment(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
        if (!department.getOwnerId().equals(authContext.currentUserId())) {
            throw new ForbiddenException("You don't have access to this department.");
        }
        return department;
    }

    public Department requireOwnedSemester(String semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found: " + semesterId));
        return requireOwnedDepartment(semester.getDepartmentId());
    }

    public Department requireOwnedSection(String sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
        return requireOwnedSemester(section.getSemesterId());
    }

    public Department requireOwnedSubject(String subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        return requireOwnedDepartment(subject.getDepartmentId());
    }

    public List<String> currentOwnedDepartmentIds() {
        return departmentRepository.findIdsByOwnerId(authContext.currentUserId());
    }

    public Section getSectionOrThrow(String sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
    }

    public Semester getSemesterOrThrow(String semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found: " + semesterId));
    }

    @Transactional
    public void refreshCounts(String sectionId, String departmentId) {
        if (sectionId != null) {
            sectionRepository.findById(sectionId).ifPresent(section -> {
                section.setStudentCount((int) studentRepository.countBySectionIdAndArchivedFalse(sectionId));
                sectionRepository.save(section);
            });
        }
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(department -> {
                department.setTotalStudents((int) studentRepository.countByDepartmentIdAndArchivedFalse(departmentId));
                departmentRepository.save(department);
            });
        }
    }
}
