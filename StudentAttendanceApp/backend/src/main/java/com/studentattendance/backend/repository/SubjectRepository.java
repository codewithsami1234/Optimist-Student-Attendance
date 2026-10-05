package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    List<Subject> findByDepartmentIdAndArchivedFalseOrderByNameAsc(String departmentId);
    List<Subject> findByDepartmentIdOrderByNameAsc(String departmentId);
    Optional<Subject> findByDepartmentIdAndCodeIgnoreCase(String departmentId, String code);
    boolean existsByDepartmentIdAndCodeIgnoreCase(String departmentId, String code);
    long countByDepartmentId(String departmentId);
}
