package com.example.studentattendance.data.local.auth;

public interface AccountStore {
    AccountRecord findById(String id);

    AccountRecord findByEmail(String email);

    AccountRecord findByGoogleSubject(String googleSubject);

    void insert(AccountRecord account);

    void update(AccountRecord account);
}
