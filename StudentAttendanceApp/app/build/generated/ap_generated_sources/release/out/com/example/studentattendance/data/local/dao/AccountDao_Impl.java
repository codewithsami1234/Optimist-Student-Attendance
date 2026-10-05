package com.example.studentattendance.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.studentattendance.data.local.entity.AccountEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AccountDao_Impl implements AccountDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AccountEntity> __insertionAdapterOfAccountEntity;

  private final EntityDeletionOrUpdateAdapter<AccountEntity> __updateAdapterOfAccountEntity;

  public AccountDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAccountEntity = new EntityInsertionAdapter<AccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `accounts` (`id`,`email`,`name`,`role`,`designation`,`passwordHash`,`googleSubject`,`lowAttendanceThreshold`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AccountEntity entity) {
        if (entity.id == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.id);
        }
        if (entity.email == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.email);
        }
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.role == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.role);
        }
        if (entity.designation == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.designation);
        }
        if (entity.passwordHash == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.passwordHash);
        }
        if (entity.googleSubject == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.googleSubject);
        }
        statement.bindDouble(8, entity.lowAttendanceThreshold);
        statement.bindLong(9, entity.createdAtMillis);
      }
    };
    this.__updateAdapterOfAccountEntity = new EntityDeletionOrUpdateAdapter<AccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `accounts` SET `id` = ?,`email` = ?,`name` = ?,`role` = ?,`designation` = ?,`passwordHash` = ?,`googleSubject` = ?,`lowAttendanceThreshold` = ?,`createdAtMillis` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AccountEntity entity) {
        if (entity.id == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.id);
        }
        if (entity.email == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.email);
        }
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.role == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.role);
        }
        if (entity.designation == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.designation);
        }
        if (entity.passwordHash == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.passwordHash);
        }
        if (entity.googleSubject == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.googleSubject);
        }
        statement.bindDouble(8, entity.lowAttendanceThreshold);
        statement.bindLong(9, entity.createdAtMillis);
        if (entity.id == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.id);
        }
      }
    };
  }

  @Override
  public void insert(final AccountEntity account) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAccountEntity.insert(account);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final AccountEntity account) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfAccountEntity.handle(account);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public AccountEntity findById(final String id) {
    final String _sql = "SELECT * FROM accounts WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
      final int _cursorIndexOfPasswordHash = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordHash");
      final int _cursorIndexOfGoogleSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "googleSubject");
      final int _cursorIndexOfLowAttendanceThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowAttendanceThreshold");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AccountEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AccountEntity();
        if (_cursor.isNull(_cursorIndexOfId)) {
          _result.id = null;
        } else {
          _result.id = _cursor.getString(_cursorIndexOfId);
        }
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _result.email = null;
        } else {
          _result.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _result.role = null;
        } else {
          _result.role = _cursor.getString(_cursorIndexOfRole);
        }
        if (_cursor.isNull(_cursorIndexOfDesignation)) {
          _result.designation = null;
        } else {
          _result.designation = _cursor.getString(_cursorIndexOfDesignation);
        }
        if (_cursor.isNull(_cursorIndexOfPasswordHash)) {
          _result.passwordHash = null;
        } else {
          _result.passwordHash = _cursor.getString(_cursorIndexOfPasswordHash);
        }
        if (_cursor.isNull(_cursorIndexOfGoogleSubject)) {
          _result.googleSubject = null;
        } else {
          _result.googleSubject = _cursor.getString(_cursorIndexOfGoogleSubject);
        }
        _result.lowAttendanceThreshold = _cursor.getDouble(_cursorIndexOfLowAttendanceThreshold);
        _result.createdAtMillis = _cursor.getLong(_cursorIndexOfCreatedAtMillis);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public AccountEntity findByEmail(final String email) {
    final String _sql = "SELECT * FROM accounts WHERE email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (email == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, email);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
      final int _cursorIndexOfPasswordHash = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordHash");
      final int _cursorIndexOfGoogleSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "googleSubject");
      final int _cursorIndexOfLowAttendanceThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowAttendanceThreshold");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AccountEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AccountEntity();
        if (_cursor.isNull(_cursorIndexOfId)) {
          _result.id = null;
        } else {
          _result.id = _cursor.getString(_cursorIndexOfId);
        }
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _result.email = null;
        } else {
          _result.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _result.role = null;
        } else {
          _result.role = _cursor.getString(_cursorIndexOfRole);
        }
        if (_cursor.isNull(_cursorIndexOfDesignation)) {
          _result.designation = null;
        } else {
          _result.designation = _cursor.getString(_cursorIndexOfDesignation);
        }
        if (_cursor.isNull(_cursorIndexOfPasswordHash)) {
          _result.passwordHash = null;
        } else {
          _result.passwordHash = _cursor.getString(_cursorIndexOfPasswordHash);
        }
        if (_cursor.isNull(_cursorIndexOfGoogleSubject)) {
          _result.googleSubject = null;
        } else {
          _result.googleSubject = _cursor.getString(_cursorIndexOfGoogleSubject);
        }
        _result.lowAttendanceThreshold = _cursor.getDouble(_cursorIndexOfLowAttendanceThreshold);
        _result.createdAtMillis = _cursor.getLong(_cursorIndexOfCreatedAtMillis);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public AccountEntity findByGoogleSubject(final String googleSubject) {
    final String _sql = "SELECT * FROM accounts WHERE googleSubject = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (googleSubject == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, googleSubject);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfDesignation = CursorUtil.getColumnIndexOrThrow(_cursor, "designation");
      final int _cursorIndexOfPasswordHash = CursorUtil.getColumnIndexOrThrow(_cursor, "passwordHash");
      final int _cursorIndexOfGoogleSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "googleSubject");
      final int _cursorIndexOfLowAttendanceThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowAttendanceThreshold");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AccountEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AccountEntity();
        if (_cursor.isNull(_cursorIndexOfId)) {
          _result.id = null;
        } else {
          _result.id = _cursor.getString(_cursorIndexOfId);
        }
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _result.email = null;
        } else {
          _result.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _result.role = null;
        } else {
          _result.role = _cursor.getString(_cursorIndexOfRole);
        }
        if (_cursor.isNull(_cursorIndexOfDesignation)) {
          _result.designation = null;
        } else {
          _result.designation = _cursor.getString(_cursorIndexOfDesignation);
        }
        if (_cursor.isNull(_cursorIndexOfPasswordHash)) {
          _result.passwordHash = null;
        } else {
          _result.passwordHash = _cursor.getString(_cursorIndexOfPasswordHash);
        }
        if (_cursor.isNull(_cursorIndexOfGoogleSubject)) {
          _result.googleSubject = null;
        } else {
          _result.googleSubject = _cursor.getString(_cursorIndexOfGoogleSubject);
        }
        _result.lowAttendanceThreshold = _cursor.getDouble(_cursorIndexOfLowAttendanceThreshold);
        _result.createdAtMillis = _cursor.getLong(_cursorIndexOfCreatedAtMillis);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
