package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentattendance.data.local.entity.NotificationEntity;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE ownerUserId = :ownerId ORDER BY createdAtMillis DESC")
    List<NotificationEntity> list(String ownerId);

    @Query("SELECT COUNT(*) FROM notifications WHERE ownerUserId = :ownerId AND read = 0")
    long unreadCount(String ownerId);

    @Query("SELECT * FROM notifications WHERE ownerUserId = :ownerId AND type = :type AND relatedStudentId = :studentId AND createdAtMillis >= :sinceMillis LIMIT 1")
    NotificationEntity findRecent(String ownerId, String type, String studentId, long sinceMillis);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(NotificationEntity notification);

    @Query("UPDATE notifications SET relatedStudentId = :newId WHERE relatedStudentId = :oldId AND ownerUserId = :ownerId")
    void reassignStudent(String oldId, String newId, String ownerId);

    @Query("UPDATE notifications SET read = 1 WHERE id = :id AND ownerUserId = :ownerId")
    void markRead(String id, String ownerId);

    @Query("UPDATE notifications SET read = 1 WHERE ownerUserId = :ownerId")
    void markAllRead(String ownerId);

    @Query("DELETE FROM notifications WHERE ownerUserId = :ownerId AND relatedStudentId = :studentId")
    void deleteByRelatedStudent(String ownerId, String studentId);

    @Query("DELETE FROM notifications WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
