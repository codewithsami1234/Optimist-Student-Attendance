package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtMillisDesc(String userId);
    Page<Notification> findByUserIdOrderByCreatedAtMillisDesc(String userId, Pageable pageable);
    long countByUserIdAndReadFalse(String userId);
    boolean existsByUserIdAndRelatedStudentIdAndTypeAndCreatedAtMillisGreaterThan(
            String userId, String relatedStudentId, String type, long sinceMillis);

    @Modifying
    @Query("update Notification n set n.read = true where n.userId = :userId and n.read = false")
    int markAllReadForUser(@Param("userId") String userId);
}
