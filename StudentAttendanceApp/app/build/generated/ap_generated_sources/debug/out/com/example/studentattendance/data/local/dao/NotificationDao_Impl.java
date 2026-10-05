package com.example.studentattendance.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NotificationDao_Impl implements NotificationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NotificationEntity> __insertionAdapterOfNotificationEntity;

  private final EntityInsertionAdapter<NotificationEntity> __insertionAdapterOfNotificationEntity_1;

  private final SharedSQLiteStatement __preparedStmtOfReassignStudent;

  private final SharedSQLiteStatement __preparedStmtOfMarkRead;

  private final SharedSQLiteStatement __preparedStmtOfMarkAllRead;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByRelatedStudent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwner;

  public NotificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotificationEntity = new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `notifications` (`id`,`ownerUserId`,`type`,`message`,`relatedStudentId`,`read`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final NotificationEntity entity) {
        if (entity.id == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.id);
        }
        if (entity.ownerUserId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.ownerUserId);
        }
        if (entity.type == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.type);
        }
        if (entity.message == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.message);
        }
        if (entity.relatedStudentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.relatedStudentId);
        }
        final int _tmp = entity.read ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.createdAtMillis);
      }
    };
    this.__insertionAdapterOfNotificationEntity_1 = new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notifications` (`id`,`ownerUserId`,`type`,`message`,`relatedStudentId`,`read`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final NotificationEntity entity) {
        if (entity.id == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.id);
        }
        if (entity.ownerUserId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.ownerUserId);
        }
        if (entity.type == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.type);
        }
        if (entity.message == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.message);
        }
        if (entity.relatedStudentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.relatedStudentId);
        }
        final int _tmp = entity.read ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.createdAtMillis);
      }
    };
    this.__preparedStmtOfReassignStudent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET relatedStudentId = ? WHERE relatedStudentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET read = 1 WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAllRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET read = 1 WHERE ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByRelatedStudent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE ownerUserId = ? AND relatedStudentId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByOwner = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final NotificationEntity notification) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfNotificationEntity.insert(notification);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final NotificationEntity notification) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfNotificationEntity_1.insert(notification);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void reassignStudent(final String oldId, final String newId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignStudent.acquire();
    int _argIndex = 1;
    if (newId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, newId);
    }
    _argIndex = 2;
    if (oldId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, oldId);
    }
    _argIndex = 3;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfReassignStudent.release(_stmt);
    }
  }

  @Override
  public void markRead(final String id, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkRead.acquire();
    int _argIndex = 1;
    if (id == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, id);
    }
    _argIndex = 2;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfMarkRead.release(_stmt);
    }
  }

  @Override
  public void markAllRead(final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAllRead.acquire();
    int _argIndex = 1;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfMarkAllRead.release(_stmt);
    }
  }

  @Override
  public void deleteByRelatedStudent(final String ownerId, final String studentId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByRelatedStudent.acquire();
    int _argIndex = 1;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (studentId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, studentId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByRelatedStudent.release(_stmt);
    }
  }

  @Override
  public void deleteByOwner(final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByOwner.acquire();
    int _argIndex = 1;
    if (ownerId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, ownerId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByOwner.release(_stmt);
    }
  }

  @Override
  public List<NotificationEntity> list(final String ownerId) {
    final String _sql = "SELECT * FROM notifications WHERE ownerUserId = ? ORDER BY createdAtMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfRelatedStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedStudentId");
      final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final NotificationEntity _item;
        _item = new NotificationEntity();
        if (_cursor.isNull(_cursorIndexOfId)) {
          _item.id = null;
        } else {
          _item.id = _cursor.getString(_cursorIndexOfId);
        }
        if (_cursor.isNull(_cursorIndexOfOwnerUserId)) {
          _item.ownerUserId = null;
        } else {
          _item.ownerUserId = _cursor.getString(_cursorIndexOfOwnerUserId);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _item.message = null;
        } else {
          _item.message = _cursor.getString(_cursorIndexOfMessage);
        }
        if (_cursor.isNull(_cursorIndexOfRelatedStudentId)) {
          _item.relatedStudentId = null;
        } else {
          _item.relatedStudentId = _cursor.getString(_cursorIndexOfRelatedStudentId);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRead);
        _item.read = _tmp != 0;
        _item.createdAtMillis = _cursor.getLong(_cursorIndexOfCreatedAtMillis);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public long unreadCount(final String ownerId) {
    final String _sql = "SELECT COUNT(*) FROM notifications WHERE ownerUserId = ? AND read = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final long _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getLong(0);
      } else {
        _result = 0L;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public NotificationEntity findRecent(final String ownerId, final String type,
      final String studentId, final long sinceMillis) {
    final String _sql = "SELECT * FROM notifications WHERE ownerUserId = ? AND type = ? AND relatedStudentId = ? AND createdAtMillis >= ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 3;
    if (studentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, studentId);
    }
    _argIndex = 4;
    _statement.bindLong(_argIndex, sinceMillis);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfRelatedStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedStudentId");
      final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final NotificationEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new NotificationEntity();
        if (_cursor.isNull(_cursorIndexOfId)) {
          _result.id = null;
        } else {
          _result.id = _cursor.getString(_cursorIndexOfId);
        }
        if (_cursor.isNull(_cursorIndexOfOwnerUserId)) {
          _result.ownerUserId = null;
        } else {
          _result.ownerUserId = _cursor.getString(_cursorIndexOfOwnerUserId);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _result.type = null;
        } else {
          _result.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _result.message = null;
        } else {
          _result.message = _cursor.getString(_cursorIndexOfMessage);
        }
        if (_cursor.isNull(_cursorIndexOfRelatedStudentId)) {
          _result.relatedStudentId = null;
        } else {
          _result.relatedStudentId = _cursor.getString(_cursorIndexOfRelatedStudentId);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRead);
        _result.read = _tmp != 0;
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
