package com.example.studentattendance.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.studentattendance.data.local.entity.SemesterEntity;
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
public final class SemesterDao_Impl implements SemesterDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SemesterEntity> __insertionAdapterOfSemesterEntity;

  private final EntityInsertionAdapter<SemesterEntity> __insertionAdapterOfSemesterEntity_1;

  private final EntityDeletionOrUpdateAdapter<SemesterEntity> __updateAdapterOfSemesterEntity;

  private final SharedSQLiteStatement __preparedStmtOfReassignDepartment;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByDepartment;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwner;

  public SemesterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSemesterEntity = new EntityInsertionAdapter<SemesterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `semesters` (`id`,`ownerUserId`,`departmentId`,`name`,`semesterNumber`,`archived`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SemesterEntity entity) {
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
        if (entity.departmentId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.departmentId);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        statement.bindLong(5, entity.semesterNumber);
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__insertionAdapterOfSemesterEntity_1 = new EntityInsertionAdapter<SemesterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `semesters` (`id`,`ownerUserId`,`departmentId`,`name`,`semesterNumber`,`archived`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SemesterEntity entity) {
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
        if (entity.departmentId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.departmentId);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        statement.bindLong(5, entity.semesterNumber);
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__updateAdapterOfSemesterEntity = new EntityDeletionOrUpdateAdapter<SemesterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `semesters` SET `id` = ?,`ownerUserId` = ?,`departmentId` = ?,`name` = ?,`semesterNumber` = ?,`archived` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SemesterEntity entity) {
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
        if (entity.departmentId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.departmentId);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        statement.bindLong(5, entity.semesterNumber);
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.id == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.id);
        }
      }
    };
    this.__preparedStmtOfReassignDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE semesters SET departmentId = ? WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM semesters WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM semesters WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByOwner = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM semesters WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final SemesterEntity semester) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSemesterEntity.insert(semester);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final SemesterEntity semester) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSemesterEntity_1.insert(semester);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final SemesterEntity semester) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSemesterEntity.handle(semester);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void reassignDepartment(final String oldDepartmentId, final String newDepartmentId,
      final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignDepartment.acquire();
    int _argIndex = 1;
    if (newDepartmentId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, newDepartmentId);
    }
    _argIndex = 2;
    if (oldDepartmentId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, oldDepartmentId);
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
      __preparedStmtOfReassignDepartment.release(_stmt);
    }
  }

  @Override
  public void delete(final String id, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
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
      __preparedStmtOfDelete.release(_stmt);
    }
  }

  @Override
  public void deleteByDepartment(final String departmentId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByDepartment.acquire();
    int _argIndex = 1;
    if (departmentId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, departmentId);
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
      __preparedStmtOfDeleteByDepartment.release(_stmt);
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
  public List<SemesterEntity> listByDepartment(final String ownerId, final String departmentId) {
    final String _sql = "SELECT * FROM semesters WHERE ownerUserId = ? AND departmentId = ? AND archived = 0 ORDER BY semesterNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (departmentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, departmentId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSemesterNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterNumber");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<SemesterEntity> _result = new ArrayList<SemesterEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SemesterEntity _item;
        _item = new SemesterEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _item.departmentId = null;
        } else {
          _item.departmentId = _cursor.getString(_cursorIndexOfDepartmentId);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        _item.semesterNumber = _cursor.getInt(_cursorIndexOfSemesterNumber);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfArchived);
        _item.archived = _tmp != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public SemesterEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM semesters WHERE id = ? AND ownerUserId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    _argIndex = 2;
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
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSemesterNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterNumber");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final SemesterEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new SemesterEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _result.departmentId = null;
        } else {
          _result.departmentId = _cursor.getString(_cursorIndexOfDepartmentId);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        _result.semesterNumber = _cursor.getInt(_cursorIndexOfSemesterNumber);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfArchived);
        _result.archived = _tmp != 0;
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
  public SemesterEntity findByNumber(final String ownerId, final String departmentId,
      final int number) {
    final String _sql = "SELECT * FROM semesters WHERE ownerUserId = ? AND departmentId = ? AND semesterNumber = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (departmentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, departmentId);
    }
    _argIndex = 3;
    _statement.bindLong(_argIndex, number);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfSemesterNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterNumber");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final SemesterEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new SemesterEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _result.departmentId = null;
        } else {
          _result.departmentId = _cursor.getString(_cursorIndexOfDepartmentId);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        _result.semesterNumber = _cursor.getInt(_cursorIndexOfSemesterNumber);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfArchived);
        _result.archived = _tmp != 0;
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
