package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Lecture;

@Entity(tableName = "lectures",
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
                @Index(value = {"ownerUserId", "sectionId", "subjectId", "lectureNumber"}, unique = true)
        })
public class LectureEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String sectionId;
    public String subjectId;
    public int lectureNumber = 1;
    public String title;

    public Lecture toModel() {
        return new Lecture(id, sectionId, subjectId, lectureNumber, title);
    }
}
