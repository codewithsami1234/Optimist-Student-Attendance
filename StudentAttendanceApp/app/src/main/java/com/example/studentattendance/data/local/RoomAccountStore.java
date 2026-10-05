package com.example.studentattendance.data.local;

import com.example.studentattendance.data.local.auth.AccountRecord;
import com.example.studentattendance.data.local.auth.AccountStore;
import com.example.studentattendance.data.local.dao.AccountDao;
import com.example.studentattendance.data.local.entity.AccountEntity;

public final class RoomAccountStore implements AccountStore {

    private final AccountDao dao;

    public RoomAccountStore(AccountDao dao) {
        this.dao = dao;
    }

    @Override
    public AccountRecord findById(String id) {
        AccountEntity entity = dao.findById(id);
        return entity == null ? null : entity.toRecord();
    }

    @Override
    public AccountRecord findByEmail(String email) {
        AccountEntity entity = dao.findByEmail(email);
        return entity == null ? null : entity.toRecord();
    }

    @Override
    public AccountRecord findByGoogleSubject(String googleSubject) {
        AccountEntity entity = dao.findByGoogleSubject(googleSubject);
        return entity == null ? null : entity.toRecord();
    }

    @Override
    public void insert(AccountRecord account) {
        dao.insert(AccountEntity.from(account));
    }

    @Override
    public void update(AccountRecord account) {
        dao.update(AccountEntity.from(account));
    }
}
