package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.AttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, String> {

    Optional<AttendanceSession> findBySectionIdAndSubjectIdAndDate(
            String sectionId, String subjectId, String date);

    Optional<AttendanceSession> findByDepartmentIdAndSemesterIdAndSectionIdAndDate(
            String departmentId, String semesterId, String sectionId, String date);

    boolean existsBySectionId(String sectionId);
    boolean existsBySubjectId(String subjectId);

    @Query("select s from AttendanceSession s where s.departmentId in :ownedDepartmentIds and "
            + "(:departmentId is null or s.departmentId = :departmentId) and "
            + "(:semesterId is null or s.semesterId = :semesterId) and "
            + "(:sectionId is null or s.sectionId = :sectionId) and "
            + "(:subjectId is null or s.subjectId = :subjectId) and "
            + "(:fromDate is null or s.date >= :fromDate) and "
            + "(:toDate is null or s.date <= :toDate) "
            + "order by s.date desc, s.createdAtMillis desc")
    List<AttendanceSession> search(@Param("ownedDepartmentIds") List<String> ownedDepartmentIds,
                                    @Param("departmentId") String departmentId,
                                    @Param("semesterId") String semesterId,
                                    @Param("sectionId") String sectionId,
                                    @Param("subjectId") String subjectId,
                                    @Param("fromDate") String fromDate,
                                    @Param("toDate") String toDate);

    @Query("select s from AttendanceSession s where s.departmentId in :ownedDepartmentIds and "
            + "(:departmentId is null or s.departmentId = :departmentId) and "
            + "(:semesterId is null or s.semesterId = :semesterId) and "
            + "(:sectionId is null or s.sectionId = :sectionId) and "
            + "(:subjectId is null or s.subjectId = :subjectId) and "
            + "(:fromDate is null or s.date >= :fromDate) and "
            + "(:toDate is null or s.date <= :toDate)")
    Page<AttendanceSession> searchPage(@Param("ownedDepartmentIds") List<String> ownedDepartmentIds,
                                        @Param("departmentId") String departmentId,
                                        @Param("semesterId") String semesterId,
                                        @Param("sectionId") String sectionId,
                                        @Param("subjectId") String subjectId,
                                        @Param("fromDate") String fromDate,
                                        @Param("toDate") String toDate,
                                        Pageable pageable);

    List<AttendanceSession> findByDepartmentIdInOrderByDateDescCreatedAtMillisDesc(List<String> departmentIds);
}
