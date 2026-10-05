package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.LectureEntity;

@Dao
public interface LectureDao {
    @Query("SELECT * FROM lectures WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND subjectId = :subjectId ORDER BY lectureNumber")
    List<LectureEntity> listBySectionAndSubject(String ownerId, String sectionId, String subjectId);

    @Query("SELECT * FROM lectures WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    LectureEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM lectures WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND subjectId = :subjectId AND lectureNumber = :lectureNumber LIMIT 1")
    LectureEntity findByNumber(String ownerId, String sectionId, String subjectId, int lectureNumber);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(LectureEntity lecture);

    @Update
    void update(LectureEntity lecture);

    @Query("DELETE FROM lectures WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM lectures WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
