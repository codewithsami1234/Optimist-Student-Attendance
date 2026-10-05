package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.AttendanceSession;

@Entity(tableName = "attendance_sessions",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SectionEntity.class, parentColumns = "id",
                        childColumns = "sectionId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SubjectEntity.class, parentColumns = "id",
                        childColumns = "subjectId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("sectionId"),
                @Index("subjectId"),
                @Index(value = {"ownerUserId", "sectionId", "subjectId", "date"}, unique = true)
        })
public class AttendanceSessionEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String departmentId;
    public String departmentName;
    public String semesterId;
    public String semesterName;
    public String sectionId;
    public String sectionName;
    public String subjectId;
    public String subjectName;
    public String date;
    public int lectureNumber = 1;
    public long createdAtMillis;

    public AttendanceSession toModel() {
        AttendanceSession session = new AttendanceSession();
        session.setId(id);
        session.setDepartmentId(departmentId);
        session.setDepartmentName(departmentName);
        session.setSemesterId(semesterId);
        session.setSemesterName(semesterName);
        session.setSectionId(sectionId);
        session.setSectionName(sectionName);
        session.setSubjectId(subjectId);
        session.setSubjectName(subjectName);
        session.setDate(date);
        session.setLectureNumber(lectureNumber);
        session.setCreatedAtMillis(createdAtMillis);
        return session;
    }
}
