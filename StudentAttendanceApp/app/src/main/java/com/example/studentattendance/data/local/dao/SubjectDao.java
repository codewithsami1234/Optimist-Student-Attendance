package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.SubjectEntity;

@Dao
public interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE ownerUserId = :ownerId AND departmentId = :departmentId AND archived = 0 ORDER BY name")
    List<SubjectEntity> listByDepartment(String ownerId, String departmentId);

    @Query("SELECT * FROM subjects WHERE ownerUserId = :ownerId AND archived = 0 ORDER BY name")
    List<SubjectEntity> listActive(String ownerId);

    @Query("SELECT * FROM subjects WHERE ownerUserId = :ownerId AND sectionId = :sectionId")
    List<SubjectEntity> listBySection(String ownerId, String sectionId);

    @Query("SELECT COUNT(*) FROM subjects WHERE ownerUserId = :ownerId AND sectionId = :sectionId")
    int countInSection(String ownerId, String sectionId);

    @Query("SELECT COUNT(*) FROM subjects WHERE ownerUserId = :ownerId AND semesterId = :semesterId")
    int countInSemester(String ownerId, String semesterId);

    @Query("SELECT COUNT(*) FROM subjects WHERE ownerUserId = :ownerId AND departmentId = :departmentId")
    int countInDepartment(String ownerId, String departmentId);

    @Query("SELECT * FROM subjects WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    SubjectEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM subjects WHERE ownerUserId = :ownerId AND departmentId = :departmentId AND code = :code LIMIT 1")
    SubjectEntity findByCode(String ownerId, String departmentId, String code);

    @Query("SELECT * FROM subjects WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND code = :code COLLATE NOCASE LIMIT 1")
    SubjectEntity findByCodeInSection(String ownerId, String sectionId, String code);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(SubjectEntity subject);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SubjectEntity subject);

    @Query("UPDATE subjects SET departmentId = :newDepartmentId WHERE departmentId = :oldDepartmentId AND ownerUserId = :ownerId")
    void reassignDepartment(String oldDepartmentId, String newDepartmentId, String ownerId);

    @Update
    void update(SubjectEntity subject);

    @Query("DELETE FROM subjects WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM subjects WHERE departmentId = :departmentId AND ownerUserId = :ownerId")
    void deleteByDepartment(String departmentId, String ownerId);

    @Query("DELETE FROM subjects WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
