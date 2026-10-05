package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentattendance.data.local.entity.AdjustmentEntity;

@Dao
public interface AdjustmentDao {
    @Query("SELECT * FROM attendance_adjustments WHERE ownerUserId = :ownerId")
    List<AdjustmentEntity> listAll(String ownerId);

    @Query("SELECT * FROM attendance_adjustments WHERE ownerUserId = :ownerId AND studentId = :studentId ORDER BY createdAtMillis DESC")
    List<AdjustmentEntity> listByStudent(String ownerId, String studentId);

    @Query("SELECT COALESCE(SUM(delta), 0) FROM attendance_adjustments WHERE ownerUserId = :ownerId AND studentId = :studentId")
    int netDelta(String ownerId, String studentId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(AdjustmentEntity adjustment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AdjustmentEntity adjustment);

    @Query("UPDATE attendance_adjustments SET studentId = :newId WHERE studentId = :oldId AND ownerUserId = :ownerId")
    void reassignStudent(String oldId, String newId, String ownerId);

    @Query("DELETE FROM attendance_adjustments WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
