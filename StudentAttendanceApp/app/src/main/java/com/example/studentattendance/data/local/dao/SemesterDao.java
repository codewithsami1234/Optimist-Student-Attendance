package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.SemesterEntity;

@Dao
public interface SemesterDao {
    @Query("SELECT * FROM semesters WHERE ownerUserId = :ownerId AND departmentId = :departmentId AND archived = 0 ORDER BY semesterNumber")
    List<SemesterEntity> listByDepartment(String ownerId, String departmentId);

    @Query("SELECT * FROM semesters WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    SemesterEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM semesters WHERE ownerUserId = :ownerId AND departmentId = :departmentId AND semesterNumber = :number LIMIT 1")
    SemesterEntity findByNumber(String ownerId, String departmentId, int number);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(SemesterEntity semester);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SemesterEntity semester);

    @Query("UPDATE semesters SET departmentId = :newDepartmentId WHERE departmentId = :oldDepartmentId AND ownerUserId = :ownerId")
    void reassignDepartment(String oldDepartmentId, String newDepartmentId, String ownerId);

    @Update
    void update(SemesterEntity semester);

    @Query("DELETE FROM semesters WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM semesters WHERE departmentId = :departmentId AND ownerUserId = :ownerId")
    void deleteByDepartment(String departmentId, String ownerId);

    @Query("DELETE FROM semesters WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
