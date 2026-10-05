package com.example.studentattendance.data.local.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.DepartmentEntity;

@Dao
public interface DepartmentDao {
    @Query("SELECT * FROM departments WHERE ownerUserId = :ownerId AND archived = 0 ORDER BY name")
    List<DepartmentEntity> listActive(String ownerId);

    @Query("SELECT * FROM departments WHERE ownerUserId = :ownerId")
    List<DepartmentEntity> listAll(String ownerId);

    @Query("SELECT * FROM departments WHERE id = :id AND ownerUserId = :ownerId LIMIT 1")
    DepartmentEntity findOwned(String id, String ownerId);

    @Query("SELECT * FROM departments WHERE ownerUserId = :ownerId AND code = :code LIMIT 1")
    DepartmentEntity findByCode(String ownerId, String code);

    @Query("SELECT * FROM departments WHERE ownerUserId = :ownerId AND name = :name COLLATE NOCASE LIMIT 1")
    DepartmentEntity findByName(String ownerId, String name);

    @Query("SELECT * FROM departments WHERE ownerUserId = :ownerId AND name = :name COLLATE NOCASE AND shift = :shift LIMIT 1")
    DepartmentEntity findByNameAndShift(String ownerId, String name, String shift);

    @Query("SELECT COUNT(*) FROM departments WHERE ownerUserId = :ownerId")
    int countForOwner(String ownerId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(DepartmentEntity department);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DepartmentEntity department);

    @Update
    void update(DepartmentEntity department);

    @Query("DELETE FROM departments WHERE id = :id AND ownerUserId = :ownerId")
    void delete(String id, String ownerId);

    @Query("DELETE FROM departments WHERE ownerUserId = :ownerId")
    void deleteByOwner(String ownerId);
}
