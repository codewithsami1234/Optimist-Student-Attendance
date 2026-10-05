package com.example.studentattendance.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentattendance.data.local.entity.AccountEntity;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    AccountEntity findById(String id);

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    AccountEntity findByEmail(String email);

    @Query("SELECT * FROM accounts WHERE googleSubject = :googleSubject LIMIT 1")
    AccountEntity findByGoogleSubject(String googleSubject);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(AccountEntity account);

    @Update
    void update(AccountEntity account);
}
