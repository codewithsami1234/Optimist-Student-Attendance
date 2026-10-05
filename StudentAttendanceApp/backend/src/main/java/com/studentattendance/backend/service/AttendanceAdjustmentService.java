package com.studentattendance.backend.service;

import com.studentattendance.backend.dto.AttendanceAdjustmentRequest;
import com.studentattendance.backend.entity.AttendanceAdjustment;
import com.studentattendance.backend.entity.Student;
import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.repository.AttendanceAdjustmentRepository;
import com.studentattendance.backend.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Teacher-controlled attendance relief. A professor can credit or revoke a
 * bounded number of "present" days for a student to handle genuine cases
 * (a medical certificate, a marking mistake, an approved leave that
 * predates this system) - every such change requires a reason and is
 * recorded permanently here. It is intentionally impossible to edit or
 * delete an {@link com.studentattendance.backend.entity.AttendanceRecord}
 * through this path or any other: adjustments are additive, append-only
 * ledger entries that AnalyticsService folds into the *computed* rate,
 * so the original day-by-day attendance history is never altered.
 */
@Service
public class AttendanceAdjustmentService {

    /** A single relief request can move at most this many days, to keep adjustments a correction tool, not a rewrite tool. */
    private static final int MAX_ABS_DELTA = 5;
    private static final int MIN_REASON_LENGTH = 8;

    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final StudentService studentService;
    private final AuthContext authContext;
    private final NotificationService notificationService;

    public AttendanceAdjustmentService(AttendanceAdjustmentRepository adjustmentRepository,
                                        StudentService studentService,
                                        AuthContext authContext,
                                        NotificationService notificationService) {
        this.adjustmentRepository = adjustmentRepository;
        this.studentService = studentService;
        this.authContext = authContext;
        this.notificationService = notificationService;
    }

    @Transactional
    public AttendanceAdjustment createAdjustment(String studentId, AttendanceAdjustmentRequest request) {
        // Ownership check: throws 404/403 if this isn't the current professor's own student.
        Student student = studentService.getOwnedStudentOrThrow(studentId);

        if (request.getDelta() == 0) {
            throw new BadRequestException("Adjustment must increase or decrease attendance by at least 1 day.");
        }
        if (Math.abs(request.getDelta()) > MAX_ABS_DELTA) {
            throw new BadRequestException("A single adjustment can't exceed " + MAX_ABS_DELTA + " days. "
                    + "For larger corrections, please contact an administrator.");
        }
        if (request.getReason() == null || request.getReason().trim().length() < MIN_REASON_LENGTH) {
            throw new BadRequestException("Please provide a reason of at least " + MIN_REASON_LENGTH
                    + " characters explaining this adjustment.");
        }

        User teacher = authContext.currentUser();
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId("adj_" + UUID.randomUUID());
        adjustment.setStudentId(studentId);
        adjustment.setDepartmentId(student.getDepartmentId());
        adjustment.setDelta(request.getDelta());
        adjustment.setReason(request.getReason().trim());
        adjustment.setAdjustedByUserId(teacher.getId());
        adjustment.setAdjustedByName(teacher.getName());
        adjustment.setCreatedAtMillis(System.currentTimeMillis());
        AttendanceAdjustment saved = adjustmentRepository.save(adjustment);

        String direction = request.getDelta() > 0 ? "credited" : "deducted";
        notificationService.notify(teacher.getId(), "ATTENDANCE_ADJUSTED",
                String.format("You %s %d day(s) for %s (%s): %s",
                        direction, Math.abs(request.getDelta()), student.getName(), student.getRollNumber(),
                        adjustment.getReason()),
                studentId);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<AttendanceAdjustment> getHistory(String studentId) {
        studentService.getOwnedStudentOrThrow(studentId);
        return adjustmentRepository.findByStudentIdOrderByCreatedAtMillisDesc(studentId);
    }

    /** Net effect of all adjustments on a student's present-day count; 0 if none exist. */
    @Transactional(readOnly = true)
    public int getNetDelta(String studentId) {
        return adjustmentRepository.findByStudentIdOrderByCreatedAtMillisDesc(studentId).stream()
                .mapToInt(AttendanceAdjustment::getDelta)
                .sum();
    }
}
