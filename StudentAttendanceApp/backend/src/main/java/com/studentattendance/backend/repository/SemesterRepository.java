package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, String> {
    List<Semester> findByDepartmentIdOrderBySemesterNumberAsc(String departmentId);
    boolean existsByDepartmentIdAndSemesterNumber(String departmentId, int semesterNumber);
    long countByDepartmentId(String departmentId);
}
