package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Semester;

@Entity(tableName = "semesters",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = DepartmentEntity.class, parentColumns = "id",
                        childColumns = "departmentId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("departmentId"),
                @Index(value = {"ownerUserId", "departmentId", "semesterNumber"}, unique = true)
        })
public class SemesterEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String departmentId;
    public String name;
    public int semesterNumber;
    public boolean archived;

    public Semester toModel() {
        return new Semester(id, departmentId, name, semesterNumber);
    }
}
