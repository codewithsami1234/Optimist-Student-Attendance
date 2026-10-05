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
import com.example.studentattendance.data.local.entity.AdjustmentEntity;
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
public final class AdjustmentDao_Impl implements AdjustmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AdjustmentEntity> __insertionAdapterOfAdjustmentEntity;

  private final EntityInsertionAdapter<AdjustmentEntity> __insertionAdapterOfAdjustmentEntity_1;

  private final SharedSQLiteStatement __preparedStmtOfReassignStudent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwner;

  public AdjustmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAdjustmentEntity = new EntityInsertionAdapter<AdjustmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `attendance_adjustments` (`id`,`ownerUserId`,`studentId`,`departmentId`,`delta`,`reason`,`adjustedByUserId`,`adjustedByName`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AdjustmentEntity entity) {
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
        if (entity.studentId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.studentId);
        }
        if (entity.departmentId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.departmentId);
        }
        statement.bindLong(5, entity.delta);
        if (entity.reason == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.reason);
        }
        if (entity.adjustedByUserId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.adjustedByUserId);
        }
        if (entity.adjustedByName == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.adjustedByName);
        }
        statement.bindLong(9, entity.createdAtMillis);
      }
    };
    this.__insertionAdapterOfAdjustmentEntity_1 = new EntityInsertionAdapter<AdjustmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_adjustments` (`id`,`ownerUserId`,`studentId`,`departmentId`,`delta`,`reason`,`adjustedByUserId`,`adjustedByName`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AdjustmentEntity entity) {
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
        if (entity.studentId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.studentId);
        }
        if (entity.departmentId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.departmentId);
        }
        statement.bindLong(5, entity.delta);
        if (entity.reason == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.reason);
        }
        if (entity.adjustedByUserId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.adjustedByUserId);
        }
        if (entity.adjustedByName == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.adjustedByName);
        }
        statement.bindLong(9, entity.createdAtMillis);
      }
    };
    this.__preparedStmtOfReassignStudent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_adjustments SET studentId = ? WHERE studentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByOwner = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attendance_adjustments WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final AdjustmentEntity adjustment) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAdjustmentEntity.insert(adjustment);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final AdjustmentEntity adjustment) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAdjustmentEntity_1.insert(adjustment);
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
  public List<AdjustmentEntity> listAll(final String ownerId) {
    final String _sql = "SELECT * FROM attendance_adjustments WHERE ownerUserId = ?";
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
      final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "delta");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfAdjustedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedByUserId");
      final int _cursorIndexOfAdjustedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedByName");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final List<AdjustmentEntity> _result = new ArrayList<AdjustmentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdjustmentEntity _item;
        _item = new AdjustmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfStudentId)) {
          _item.studentId = null;
        } else {
          _item.studentId = _cursor.getString(_cursorIndexOfStudentId);
        }
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _item.departmentId = null;
        } else {
          _item.departmentId = _cursor.getString(_cursorIndexOfDepartmentId);
        }
        _item.delta = _cursor.getInt(_cursorIndexOfDelta);
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _item.reason = null;
        } else {
          _item.reason = _cursor.getString(_cursorIndexOfReason);
        }
        if (_cursor.isNull(_cursorIndexOfAdjustedByUserId)) {
          _item.adjustedByUserId = null;
        } else {
          _item.adjustedByUserId = _cursor.getString(_cursorIndexOfAdjustedByUserId);
        }
        if (_cursor.isNull(_cursorIndexOfAdjustedByName)) {
          _item.adjustedByName = null;
        } else {
          _item.adjustedByName = _cursor.getString(_cursorIndexOfAdjustedByName);
        }
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
  public List<AdjustmentEntity> listByStudent(final String ownerId, final String studentId) {
    final String _sql = "SELECT * FROM attendance_adjustments WHERE ownerUserId = ? AND studentId = ? ORDER BY createdAtMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (studentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, studentId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "delta");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfAdjustedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedByUserId");
      final int _cursorIndexOfAdjustedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedByName");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final List<AdjustmentEntity> _result = new ArrayList<AdjustmentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdjustmentEntity _item;
        _item = new AdjustmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfStudentId)) {
          _item.studentId = null;
        } else {
          _item.studentId = _cursor.getString(_cursorIndexOfStudentId);
        }
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _item.departmentId = null;
        } else {
          _item.departmentId = _cursor.getString(_cursorIndexOfDepartmentId);
        }
        _item.delta = _cursor.getInt(_cursorIndexOfDelta);
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _item.reason = null;
        } else {
          _item.reason = _cursor.getString(_cursorIndexOfReason);
        }
        if (_cursor.isNull(_cursorIndexOfAdjustedByUserId)) {
          _item.adjustedByUserId = null;
        } else {
          _item.adjustedByUserId = _cursor.getString(_cursorIndexOfAdjustedByUserId);
        }
        if (_cursor.isNull(_cursorIndexOfAdjustedByName)) {
          _item.adjustedByName = null;
        } else {
          _item.adjustedByName = _cursor.getString(_cursorIndexOfAdjustedByName);
        }
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
  public int netDelta(final String ownerId, final String studentId) {
    final String _sql = "SELECT COALESCE(SUM(delta), 0) FROM attendance_adjustments WHERE ownerUserId = ? AND studentId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (studentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, studentId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
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
