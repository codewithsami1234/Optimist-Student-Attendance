package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    List<Department> findByOwnerIdOrderByNameAsc(String ownerId);

    boolean existsByOwnerIdAndCodeIgnoreCase(String ownerId, String code);

    boolean existsByOwnerIdAndCodeIgnoreCaseAndIdNot(String ownerId, String code, String id);

    @Query("select d.id from Department d where d.ownerId = :ownerId")
    List<String> findIdsByOwnerId(@Param("ownerId") String ownerId);
}
