package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findBySectionIdAndArchivedFalseOrderByRollNumberAsc(String sectionId);
    List<Student> findBySectionIdOrderByRollNumberAsc(String sectionId);
    Optional<Student> findBySectionIdAndRollNumberIgnoreCase(String sectionId, String rollNumber);
    boolean existsBySectionIdAndRollNumberIgnoreCase(String sectionId, String rollNumber);
    long countBySectionIdAndArchivedFalse(String sectionId);
    long countByDepartmentIdAndArchivedFalse(String departmentId);
    long countBySectionId(String sectionId);

    @Query("select s from Student s where s.departmentId in :departmentIds and s.archived = false order by s.rollNumber asc")
    List<Student> findActiveByDepartmentIdIn(@Param("departmentIds") List<String> departmentIds);

    @Query("select s from Student s where s.departmentId in :departmentIds order by s.rollNumber asc")
    List<Student> findByDepartmentIdIn(@Param("departmentIds") List<String> departmentIds);
}
