package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.DepartmentShift;

@Entity(tableName = "departments",
        foreignKeys = @ForeignKey(
                entity = AccountEntity.class,
                parentColumns = "id",
                childColumns = "ownerUserId",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index("ownerUserId"),
                @Index(value = {"ownerUserId", "code"}, unique = true),
                @Index(value = {"ownerUserId", "name", "shift"})
        })
public class DepartmentEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String name;
    public String code;
    public String headOfDepartment;
    @NonNull
    public String shift = DepartmentShift.MORNING;
    public boolean archived;

    public Department toModel(int totalStudents) {
        return new Department(id, name, code, totalStudents, headOfDepartment, shift);
    }
}
