package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;

@Dao
public interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND ownerUserId = :ownerId ORDER BY rollNumber")
    List<AttendanceRecordEntity> listBySession(String sessionId, String ownerId);

    @Query("SELECT * FROM attendance_records WHERE ownerUserId = :ownerId")
    List<AttendanceRecordEntity> listAll(String ownerId);

    @Query("SELECT * FROM attendance_records WHERE ownerUserId = :ownerId AND studentId = :studentId ORDER BY date DESC")
    List<AttendanceRecordEntity> listByStudent(String ownerId, String studentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AttendanceRecordEntity> records);

    @Query("UPDATE attendance_records SET departmentId = :newId WHERE departmentId = :oldId AND ownerUserId = :ownerId")
    void reassignDepartment(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_records SET semesterId = :newId WHERE semesterId = :oldId AND ownerUserId = :ownerId")
    void reassignSemester(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_records SET sectionId = :newId WHERE sectionId = :oldId AND ownerUserId = :ownerId")
    void reassignSection(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_records SET studentId = :newId WHERE studentId = :oldId AND ownerUserId = :ownerId")
    void reassignStudent(String oldId, String newId, String ownerId);

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId AND ownerUserId = :ownerId")
    void deleteBySession(String sessionId, String ownerId);

    @Query("DELETE FROM attendance_records WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
