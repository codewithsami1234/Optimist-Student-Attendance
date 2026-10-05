package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceStatus;

@Entity(tableName = "attendance_records",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = AttendanceSessionEntity.class, parentColumns = "id",
                        childColumns = "sessionId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = StudentEntity.class, parentColumns = "id",
                        childColumns = "studentId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("sessionId"),
                @Index("studentId"),
                @Index(value = {"ownerUserId", "sessionId", "studentId"}, unique = true)
        })
public class AttendanceRecordEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String sessionId;
    public String studentId;
    public String studentName;
    public String rollNumber;
    public String departmentId;
    public String semesterId;
    public String sectionId;
    public String date;
    public AttendanceStatus status;
    public String markedBy;
    public long markedAtMillis;

    public AttendanceRecord toModel() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(id);
        record.setStudentId(studentId);
        record.setStudentName(studentName);
        record.setRollNumber(rollNumber);
        record.setDepartmentId(departmentId);
        record.setSemesterId(semesterId);
        record.setSectionId(sectionId);
        record.setDate(date);
        record.setStatus(status);
        record.setMarkedBy(markedBy);
        record.setMarkedAtMillis(markedAtMillis);
        return record;
    }
}
