package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.AttendanceAdjustment;

@Entity(tableName = "attendance_adjustments",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = StudentEntity.class, parentColumns = "id",
                        childColumns = "studentId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("studentId")
        })
public class AdjustmentEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String studentId;
    public String departmentId;
    public int delta;
    public String reason;
    public String adjustedByUserId;
    public String adjustedByName;
    public long createdAtMillis;

    public AttendanceAdjustment toModel() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(id);
        adjustment.setStudentId(studentId);
        adjustment.setDepartmentId(departmentId);
        adjustment.setDelta(delta);
        adjustment.setReason(reason);
        adjustment.setAdjustedByUserId(adjustedByUserId);
        adjustment.setAdjustedByName(adjustedByName);
        adjustment.setCreatedAtMillis(createdAtMillis);
        return adjustment;
    }
}
