package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Student;

@Entity(tableName = "students",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SectionEntity.class, parentColumns = "id",
                        childColumns = "sectionId", onDelete = ForeignKey.NO_ACTION)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("sectionId"),
                @Index(value = {"ownerUserId", "sectionId", "rollNumber"}, unique = true)
        })
public class StudentEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String rollNumber;
    public String name;
    public String departmentId;
    public String semesterId;
    public String sectionId;
    public String email;
    public String phone;
    public boolean archived;

    public Student toModel() {
        Student student = new Student(id, rollNumber, name, departmentId, semesterId, sectionId, email, phone);
        student.setArchived(archived);
        return student;
    }
}
