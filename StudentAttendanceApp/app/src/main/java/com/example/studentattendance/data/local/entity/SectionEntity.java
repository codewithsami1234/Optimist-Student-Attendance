package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Section;

@Entity(tableName = "sections",
        foreignKeys = {
                @ForeignKey(entity = AccountEntity.class, parentColumns = "id",
                        childColumns = "ownerUserId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SemesterEntity.class, parentColumns = "id",
                        childColumns = "semesterId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("ownerUserId"),
                @Index("semesterId"),
                @Index(value = {"ownerUserId", "semesterId", "name"}, unique = true)
        })
public class SectionEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String semesterId;
    public String name;
    public boolean archived;

    public Section toModel(int studentCount) {
        return new Section(id, semesterId, name, studentCount);
    }
}
