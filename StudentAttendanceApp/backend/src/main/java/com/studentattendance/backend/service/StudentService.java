package com.studentattendance.backend.service;

import com.studentattendance.backend.dto.ImportResultDto;
import com.studentattendance.backend.dto.StudentTransferRequest;
import com.studentattendance.backend.dto.StudentUpdateRequest;
import com.studentattendance.backend.entity.Section;
import com.studentattendance.backend.entity.Semester;
import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.exception.ForbiddenException;
import com.studentattendance.backend.exception.ResourceNotFoundException;
import com.studentattendance.backend.repository.AttendanceRecordRepository;
import com.studentattendance.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final AttendanceRecordRepository recordRepository;

    public StudentService(StudentRepository studentRepository, DepartmentService departmentService,
                           AttendanceRecordRepository recordRepository) {
        this.studentRepository = studentRepository;
        this.departmentService = departmentService;
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getStudents(String departmentId, String semesterId, String sectionId, boolean includeArchived) {
        if (sectionId == null || sectionId.isBlank()) {
            throw new BadRequestException("Please select a section first.");
        }
        departmentService.requireOwnedSection(sectionId);
        if (includeArchived) {
            return studentRepository.findBySectionIdOrderByRollNumberAsc(sectionId);
        }
        return studentRepository.findBySectionIdAndArchivedFalseOrderByRollNumberAsc(sectionId);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        return studentRepository.findActiveByDepartmentIdIn(ownedDepartmentIds);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudentsIncludingArchived() {
        List<String> ownedDepartmentIds = departmentService.currentOwnedDepartmentIds();
        if (ownedDepartmentIds.isEmpty()) {
            return List.of();
        }
        return studentRepository.findByDepartmentIdIn(ownedDepartmentIds);
    }

    @Transactional
    public Student addStudent(Student student) {
        if (student == null || student.getSectionId() == null || student.getSectionId().isBlank()) {
            throw new BadRequestException("Please select a section first.");
        }
        departmentService.requireOwnedSection(student.getSectionId());
        fillHierarchyFromSection(student);
        validateNewStudent(student);
        if (student.getId() == null || student.getId().isBlank()) {
            student.setId("st_" + System.currentTimeMillis());
        }
        student.setArchived(false);
        Student saved = studentRepository.save(student);
        departmentService.refreshCounts(saved.getSectionId(), saved.getDepartmentId());
        return saved;
    }

    @Transactional
    public Student updateStudent(String studentId, StudentUpdateRequest request) {
        Student student = getOwnedStudentOrThrow(studentId);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            student.setName(request.getName().trim());
        }
        if (request.getRollNumber() != null && !request.getRollNumber().trim().isEmpty()) {
            String roll = request.getRollNumber().trim();
            studentRepository.findBySectionIdAndRollNumberIgnoreCase(student.getSectionId(), roll)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(studentId)) {
                            throw new BadRequestException("A student with this roll number already exists in this section.");
                        }
                    });
            student.setRollNumber(roll);
        }
        if (request.getEmail() != null) {
            student.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone().trim());
        }
        boolean archiveChanged = false;
        if (request.getArchived() != null && request.getArchived() != student.isArchived()) {
            student.setArchived(request.getArchived());
            archiveChanged = true;
        }
        Student saved = studentRepository.save(student);
        if (archiveChanged) {
            departmentService.refreshCounts(saved.getSectionId(), saved.getDepartmentId());
        }
        return saved;
    }

    @Transactional
    public Student transferStudent(String studentId, StudentTransferRequest request) {
        Student student = getOwnedStudentOrThrow(studentId);
        String targetSectionId = request.getSectionId();
        departmentService.requireOwnedSection(targetSectionId);
        if (targetSectionId.equals(student.getSectionId())) {
            return student;
        }
        if (studentRepository.existsBySectionIdAndRollNumberIgnoreCase(targetSectionId, student.getRollNumber())) {
            throw new BadRequestException("A student with this roll number already exists in the target section.");
        }
        String oldSectionId = student.getSectionId();
        String oldDepartmentId = student.getDepartmentId();
        student.setSectionId(targetSectionId);
        fillHierarchyFromSection(student);
        Student saved = studentRepository.save(student);
        departmentService.refreshCounts(oldSectionId, oldDepartmentId);
        departmentService.refreshCounts(saved.getSectionId(), saved.getDepartmentId());
        return saved;
    }

    @Transactional
    public void deleteStudent(String studentId) {
        Student student = getOwnedStudentOrThrow(studentId);
        if (recordRepository.existsByStudentId(studentId)) {
            throw new BadRequestException("This student has attendance history. Archive them instead of deleting.");
        }
        String sectionId = student.getSectionId();
        String departmentId = student.getDepartmentId();
        studentRepository.delete(student);
        departmentService.refreshCounts(sectionId, departmentId);
    }

    @Transactional
    public ImportResultDto importStudents(List<Student> students) {
        ImportResultDto result = new ImportResultDto();
        if (students == null || students.isEmpty()) {
            throw new BadRequestException("No valid student rows were found in the selected file.");
        }

        int counter = 0;
        int row = 1;
        for (Student student : students) {
            row++;
            try {
                if (student.getSectionId() == null || student.getSectionId().isBlank()) {
                    result.setInvalid(result.getInvalid() + 1);
                    result.addError(row, student.getRollNumber(), "Section is required.");
                    continue;
                }
                departmentService.requireOwnedSection(student.getSectionId());
                fillHierarchyFromSection(student);
                if (student.getName() == null || student.getName().trim().isEmpty()) {
                    result.setInvalid(result.getInvalid() + 1);
                    result.addError(row, student.getRollNumber(), "Student name is required.");
                    continue;
                }
                if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
                    result.setInvalid(result.getInvalid() + 1);
                    result.addError(row, student.getName(), "Roll number is required.");
                    continue;
                }
                student.setName(student.getName().trim());
                student.setRollNumber(student.getRollNumber().trim());
                if (studentRepository.existsBySectionIdAndRollNumberIgnoreCase(
                        student.getSectionId(), student.getRollNumber())) {
                    result.setDuplicates(result.getDuplicates() + 1);
                    result.addError(row, student.getRollNumber(),
                            "Duplicate roll number in this section.");
                    continue;
                }
                if (student.getId() == null || student.getId().isBlank()) {
                    student.setId("st_" + System.currentTimeMillis() + "_" + (counter++));
                }
                student.setArchived(false);
                studentRepository.save(student);
                departmentService.refreshCounts(student.getSectionId(), student.getDepartmentId());
                result.setImported(result.getImported() + 1);
            } catch (Exception ex) {
                result.setInvalid(result.getInvalid() + 1);
                result.addError(row, student.getRollNumber(), ex.getMessage());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Student getOwnedStudentOrThrow(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        try {
            departmentService.requireOwnedDepartment(student.getDepartmentId());
        } catch (ForbiddenException e) {
            throw new ForbiddenException("You don't have access to this student.");
        }
        return student;
    }

    private void fillHierarchyFromSection(Student student) {
        Section section = departmentService.getSectionOrThrow(student.getSectionId());
        Semester semester = departmentService.getSemesterOrThrow(section.getSemesterId());
        student.setSemesterId(semester.getId());
        student.setDepartmentId(semester.getDepartmentId());
    }

    private void validateNewStudent(Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new BadRequestException("Student name is required.");
        }
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            throw new BadRequestException("Roll number is required.");
        }
        student.setName(student.getName().trim());
        student.setRollNumber(student.getRollNumber().trim());
        if (studentRepository.existsBySectionIdAndRollNumberIgnoreCase(student.getSectionId(), student.getRollNumber())) {
            throw new BadRequestException("A student with this roll number already exists in this section.");
        }
    }
}
