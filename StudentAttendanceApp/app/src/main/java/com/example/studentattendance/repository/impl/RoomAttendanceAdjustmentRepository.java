package com.example.studentattendance.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.data.local.LocalExec;
import com.example.studentattendance.data.local.LocalOwner;
import com.example.studentattendance.data.local.entity.AdjustmentEntity;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.utils.PreferenceManager;

public class RoomAttendanceAdjustmentRepository implements AttendanceAdjustmentRepository {

    private static final int MAX_ABS_DELTA = 5;
    private static final int MIN_REASON_LENGTH = 8;

    private final PreferenceManager prefs;
    private final AppDatabase db;

    public RoomAttendanceAdjustmentRepository(PreferenceManager prefs, AppDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    @Override
    public void createAdjustment(String studentId, int delta, String reason,
                                  ApiCallback<AttendanceAdjustment> callback) {
        LocalExec.io(() -> {
            if (delta == 0) {
                throw new LocalDataException("Adjustment must increase or decrease attendance by at least 1 day.");
            }
            if (Math.abs(delta) > MAX_ABS_DELTA) {
                throw new LocalDataException("A single adjustment can't exceed " + MAX_ABS_DELTA + " days.");
            }
            if (reason == null || reason.trim().length() < MIN_REASON_LENGTH) {
                throw new LocalDataException("Please provide a reason of at least " + MIN_REASON_LENGTH
                        + " characters explaining this adjustment.");
            }
            String ownerId = LocalOwner.requireId(prefs);
            User teacher = LocalOwner.requireUser(prefs);
            StudentEntity student = db.studentDao().findOwned(studentId, ownerId);
            if (student == null) {
                throw new LocalDataException("Student not found.");
            }
            AdjustmentEntity entity = new AdjustmentEntity();
            entity.id = "adj_" + UUID.randomUUID();
            entity.ownerUserId = ownerId;
            entity.studentId = studentId;
            entity.departmentId = student.departmentId;
            entity.delta = delta;
            entity.reason = reason.trim();
            entity.adjustedByUserId = teacher.getId();
            entity.adjustedByName = teacher.getName();
            entity.createdAtMillis = System.currentTimeMillis();
            db.adjustmentDao().insert(entity);

            NotificationEntity notification = new NotificationEntity();
            notification.id = "n_" + UUID.randomUUID();
            notification.ownerUserId = ownerId;
            notification.type = "ATTENDANCE_ADJUSTED";
            String direction = delta > 0 ? "credited" : "deducted";
            notification.message = String.format("You %s %d day(s) for %s (%s): %s",
                    direction, Math.abs(delta), student.name, student.rollNumber, reason.trim());
            notification.relatedStudentId = studentId;
            notification.createdAtMillis = System.currentTimeMillis();
            db.notificationDao().insert(notification);
            com.example.studentattendance.sync.LowAttendanceEvaluator.notifyIfBelow(
                    db, ownerId, teacher, java.util.Collections.singletonList(studentId));
            return entity.toModel();
        }, callback);
    }

    @Override
    public void getHistory(String studentId, ApiCallback<List<AttendanceAdjustment>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<AttendanceAdjustment> result = new ArrayList<>();
            for (AdjustmentEntity entity : db.adjustmentDao().listByStudent(ownerId, studentId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }

    @Override
    public void getAll(ApiCallback<List<AttendanceAdjustment>> callback) {
        LocalExec.io(() -> {
            String ownerId = LocalOwner.requireId(prefs);
            List<AttendanceAdjustment> result = new ArrayList<>();
            for (AdjustmentEntity entity : db.adjustmentDao().listAll(ownerId)) {
                result.add(entity.toModel());
            }
            return result;
        }, callback);
    }
}
