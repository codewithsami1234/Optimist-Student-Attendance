package com.example.studentattendance.repository;

import java.util.List;

import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.network.ApiCallback;

/**
 * Phase 4: teacher-controlled attendance relief. Every create is a new,
 * permanent audit-trail entry - never an edit to an original attendance
 * record.
 */
public interface AttendanceAdjustmentRepository {
    void createAdjustment(String studentId, int delta, String reason, ApiCallback<AttendanceAdjustment> callback);
    void getHistory(String studentId, ApiCallback<List<AttendanceAdjustment>> callback);

    default void getAll(ApiCallback<List<AttendanceAdjustment>> callback) {
        callback.onSuccess(java.util.Collections.emptyList());
    }
}
