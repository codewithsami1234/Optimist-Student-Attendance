package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {

    List<AttendanceRecord> findByStudentIdOrderByDateDesc(String studentId);

    boolean existsByStudentId(String studentId);

    @Query("select r from AttendanceRecord r where r.departmentId in :ownedDepartmentIds and "
            + "(:departmentId is null or r.departmentId = :departmentId) and "
            + "(:semesterId is null or r.semesterId = :semesterId) and "
            + "(:sectionId is null or r.sectionId = :sectionId) and "
            + "(:subjectId is null or r.subjectId = :subjectId)")
    List<AttendanceRecord> search(@Param("ownedDepartmentIds") List<String> ownedDepartmentIds,
                                   @Param("departmentId") String departmentId,
                                   @Param("semesterId") String semesterId,
                                   @Param("sectionId") String sectionId,
                                   @Param("subjectId") String subjectId);

    @Query("select r from AttendanceRecord r where r.departmentId in :ownedDepartmentIds and r.date >= :fromDate and "
            + "(:departmentId is null or r.departmentId = :departmentId) and "
            + "(:subjectId is null or r.subjectId = :subjectId)")
    List<AttendanceRecord> findSince(@Param("ownedDepartmentIds") List<String> ownedDepartmentIds,
                                      @Param("fromDate") String fromDate,
                                      @Param("departmentId") String departmentId,
                                      @Param("subjectId") String subjectId);

    List<AttendanceRecord> findByDepartmentIdIn(List<String> ownedDepartmentIds);
}
