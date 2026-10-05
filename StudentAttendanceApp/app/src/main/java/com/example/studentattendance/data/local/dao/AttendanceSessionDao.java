package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;

@Dao
public interface AttendanceSessionDao {
    @Query("SELECT * FROM attendance_sessions WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    AttendanceSessionEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM attendance_sessions WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND subjectId = :subjectId AND date = :date ORDER BY lectureNumber ASC")
    List<AttendanceSessionEntity> listByCourseDate(String ownerId, String sectionId, String subjectId, String date);

    @Query("SELECT * FROM attendance_sessions WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND subjectId = :subjectId AND date = :date LIMIT 1")
    AttendanceSessionEntity findByCourseDate(String ownerId, String sectionId, String subjectId, String date);

    @Query("SELECT * FROM attendance_sessions WHERE ownerUserId = :ownerId ORDER BY date DESC, lectureNumber ASC, createdAtMillis DESC")
    List<AttendanceSessionEntity> listAll(String ownerId);

    @Query("SELECT * FROM attendance_sessions WHERE ownerUserId = :ownerId AND subjectId = :subjectId")
    List<AttendanceSessionEntity> listBySubject(String ownerId, String subjectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AttendanceSessionEntity session);

    @Query("UPDATE attendance_sessions SET departmentId = :newId WHERE departmentId = :oldId AND ownerUserId = :ownerId")
    void reassignDepartment(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_sessions SET semesterId = :newId WHERE semesterId = :oldId AND ownerUserId = :ownerId")
    void reassignSemester(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_sessions SET sectionId = :newId WHERE sectionId = :oldId AND ownerUserId = :ownerId")
    void reassignSection(String oldId, String newId, String ownerId);

    @Query("UPDATE attendance_sessions SET subjectId = :newId WHERE subjectId = :oldId AND ownerUserId = :ownerId")
    void reassignSubject(String oldId, String newId, String ownerId);

    @Query("DELETE FROM attendance_sessions WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM attendance_sessions WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
