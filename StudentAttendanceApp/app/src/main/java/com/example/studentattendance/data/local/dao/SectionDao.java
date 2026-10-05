package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.SectionEntity;

@Dao
public interface SectionDao {
    @Query("SELECT * FROM sections WHERE ownerUserId = :ownerId AND semesterId = :semesterId AND archived = 0 ORDER BY name")
    List<SectionEntity> listBySemester(String ownerId, String semesterId);

    @Query("SELECT * FROM sections WHERE ownerUserId = :ownerId AND archived = 0 ORDER BY name")
    List<SectionEntity> listAllActive(String ownerId);

    @Query("SELECT * FROM sections WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    SectionEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM sections WHERE ownerUserId = :ownerId AND semesterId = :semesterId AND name = :name LIMIT 1")
    SectionEntity findByName(String ownerId, String semesterId, String name);

    @Query("SELECT * FROM sections WHERE ownerUserId = :ownerId AND semesterId = :semesterId AND name = :name COLLATE NOCASE LIMIT 1")
    SectionEntity findByNameIgnoreCase(String ownerId, String semesterId, String name);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(SectionEntity section);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SectionEntity section);

    @Query("UPDATE sections SET semesterId = :newSemesterId WHERE semesterId = :oldSemesterId AND ownerUserId = :ownerId")
    void reassignSemester(String oldSemesterId, String newSemesterId, String ownerId);

    @Update
    void update(SectionEntity section);

    @Query("DELETE FROM sections WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM sections WHERE semesterId = :semesterId AND ownerUserId = :ownerId")
    void deleteBySemester(String semesterId, String ownerId);

    @Query("DELETE FROM sections WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
