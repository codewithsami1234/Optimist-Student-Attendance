package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Subject;

@Entity(tableName = "subjects",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = DepartmentEntity.class, parentColumns = "id",
                        childColumns = "departmentId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("departmentId"),
                @Index("semesterId"),
                @Index("sectionId"),
                @Index(value = {"ownerUserId", "sectionId", "code"}, unique = true)
        })
public class SubjectEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String departmentId;
    public String semesterId;
    public String sectionId;
    public String name;
    public String code;
    public boolean archived;

    public Subject toModel() {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setDepartmentId(departmentId);
        subject.setSemesterId(semesterId);
        subject.setSectionId(sectionId);
        subject.setName(name);
        subject.setCode(code);
        subject.setArchived(archived);
        return subject;
    }
}
