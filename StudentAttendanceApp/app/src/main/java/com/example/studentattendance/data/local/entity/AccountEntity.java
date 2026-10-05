package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.data.local.auth.AccountRecord;

@Entity(tableName = "accounts", indices = {
        @Index(value = "email", unique = true),
        @Index(value = "googleSubject", unique = true)
})
public class AccountEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String email = "";
    public String name;
    public String role;
    public String designation;
    public String passwordHash;
    public String googleSubject;
    public double lowAttendanceThreshold = 75.0;
    public long createdAtMillis;

    public AccountRecord toRecord() {
        AccountRecord record = new AccountRecord();
        record.id = id;
        record.email = email;
        record.name = name;
        record.role = role;
        record.designation = designation;
        record.passwordHash = passwordHash;
        record.googleSubject = googleSubject;
        record.lowAttendanceThreshold = lowAttendanceThreshold;
        record.createdAtMillis = createdAtMillis;
        return record;
    }

    public static AccountEntity from(AccountRecord record) {
        AccountEntity entity = new AccountEntity();
        entity.id = record.id;
        entity.email = record.email;
        entity.name = record.name;
        entity.role = record.role;
        entity.designation = record.designation;
        entity.passwordHash = record.passwordHash;
        entity.googleSubject = record.googleSubject;
        entity.lowAttendanceThreshold = record.lowAttendanceThreshold;
        entity.createdAtMillis = record.createdAtMillis;
        return entity;
    }
}
