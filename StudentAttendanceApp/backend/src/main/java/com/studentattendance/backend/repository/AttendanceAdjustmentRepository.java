package com.studentattendance.backend.repository;

import com.studentattendance.backend.entity.AttendanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, String> {
    List<AttendanceAdjustment> findByStudentIdOrderByCreatedAtMillisDesc(String studentId);
}
