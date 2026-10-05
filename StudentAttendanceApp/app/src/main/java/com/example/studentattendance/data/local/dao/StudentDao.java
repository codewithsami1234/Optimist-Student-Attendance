package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.StudentEntity;

@Dao
public interface StudentDao {
    @Query("SELECT * FROM students WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND archived = 0 ORDER BY rollNumber")
    List<StudentEntity> listBySection(String ownerId, String sectionId);

    @Query("SELECT * FROM students WHERE ownerUserId = :ownerId AND sectionId = :sectionId")
    List<StudentEntity> listAllInSection(String ownerId, String sectionId);

    @Query("SELECT * FROM students WHERE ownerUserId = :ownerId AND archived = 0 ORDER BY name")
    List<StudentEntity> listAllActive(String ownerId);

    @Query("SELECT * FROM students WHERE ownerUserId = :ownerId ORDER BY name")
    List<StudentEntity> listAll(String ownerId);

    @Query("SELECT * FROM students WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    StudentEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM students WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND rollNumber = :rollNumber LIMIT 1")
    StudentEntity findByRollInSection(String ownerId, String sectionId, String rollNumber);

    @Query("SELECT COUNT(*) FROM students WHERE ownerUserId = :ownerId AND sectionId = :sectionId AND archived = 0")
    int countInSection(String ownerId, String sectionId);

    @Query("SELECT COUNT(*) FROM students WHERE ownerUserId = :ownerId AND departmentId = :departmentId AND archived = 0")
    int countInDepartment(String ownerId, String departmentId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(StudentEntity student);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(StudentEntity student);

    @Query("UPDATE students SET departmentId = :newId WHERE departmentId = :oldId AND ownerUserId = :ownerId")
    void reassignDepartment(String oldId, String newId, String ownerId);

    @Query("UPDATE students SET semesterId = :newId WHERE semesterId = :oldId AND ownerUserId = :ownerId")
    void reassignSemester(String oldId, String newId, String ownerId);

    @Query("UPDATE students SET sectionId = :newId WHERE sectionId = :oldId AND ownerUserId = :ownerId")
    void reassignSection(String oldId, String newId, String ownerId);

    @Update
    void update(StudentEntity student);

    @Query("DELETE FROM students WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM students WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
