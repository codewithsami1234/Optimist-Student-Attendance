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
import com.example.studentattendance.data.local.entity.DepartmentEntity;
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
public final class DepartmentDao_Impl implements DepartmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DepartmentEntity> __insertionAdapterOfDepartmentEntity;

  private final EntityInsertionAdapter<DepartmentEntity> __insertionAdapterOfDepartmentEntity_1;

  private final EntityDeletionOrUpdateAdapter<DepartmentEntity> __updateAdapterOfDepartmentEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public DepartmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDepartmentEntity = new EntityInsertionAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `departments` (`id`,`ownerUserId`,`name`,`code`,`headOfDepartment`,`archived`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DepartmentEntity entity) {
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
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.code);
        }
        if (entity.headOfDepartment == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.headOfDepartment);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__insertionAdapterOfDepartmentEntity_1 = new EntityInsertionAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `departments` (`id`,`ownerUserId`,`name`,`code`,`headOfDepartment`,`archived`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DepartmentEntity entity) {
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
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.code);
        }
        if (entity.headOfDepartment == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.headOfDepartment);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__updateAdapterOfDepartmentEntity = new EntityDeletionOrUpdateAdapter<DepartmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `departments` SET `id` = ?,`ownerUserId` = ?,`name` = ?,`code` = ?,`headOfDepartment` = ?,`archived` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DepartmentEntity entity) {
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
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.code);
        }
        if (entity.headOfDepartment == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.headOfDepartment);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.id == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.id);
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM departments WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final DepartmentEntity department) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDepartmentEntity.insert(department);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final DepartmentEntity department) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDepartmentEntity_1.insert(department);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final DepartmentEntity department) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfDepartmentEntity.handle(department);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
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
  public List<DepartmentEntity> listActive(final String ownerId) {
    final String _sql = "SELECT * FROM departments WHERE ownerUserId = ? AND archived = 0 ORDER BY name";
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
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfHeadOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "headOfDepartment");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<DepartmentEntity> _result = new ArrayList<DepartmentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DepartmentEntity _item;
        _item = new DepartmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _item.code = null;
        } else {
          _item.code = _cursor.getString(_cursorIndexOfCode);
        }
        if (_cursor.isNull(_cursorIndexOfHeadOfDepartment)) {
          _item.headOfDepartment = null;
        } else {
          _item.headOfDepartment = _cursor.getString(_cursorIndexOfHeadOfDepartment);
        }
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
  public List<DepartmentEntity> listAll(final String ownerId) {
    final String _sql = "SELECT * FROM departments WHERE ownerUserId = ?";
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
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfHeadOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "headOfDepartment");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<DepartmentEntity> _result = new ArrayList<DepartmentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DepartmentEntity _item;
        _item = new DepartmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _item.code = null;
        } else {
          _item.code = _cursor.getString(_cursorIndexOfCode);
        }
        if (_cursor.isNull(_cursorIndexOfHeadOfDepartment)) {
          _item.headOfDepartment = null;
        } else {
          _item.headOfDepartment = _cursor.getString(_cursorIndexOfHeadOfDepartment);
        }
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
  public DepartmentEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM departments WHERE id = ? AND ownerUserId = ? LIMIT 1";
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
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfHeadOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "headOfDepartment");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final DepartmentEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new DepartmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _result.code = null;
        } else {
          _result.code = _cursor.getString(_cursorIndexOfCode);
        }
        if (_cursor.isNull(_cursorIndexOfHeadOfDepartment)) {
          _result.headOfDepartment = null;
        } else {
          _result.headOfDepartment = _cursor.getString(_cursorIndexOfHeadOfDepartment);
        }
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
  public DepartmentEntity findByCode(final String ownerId, final String code) {
    final String _sql = "SELECT * FROM departments WHERE ownerUserId = ? AND code = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (code == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, code);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfHeadOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "headOfDepartment");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final DepartmentEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new DepartmentEntity();
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
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _result.code = null;
        } else {
          _result.code = _cursor.getString(_cursorIndexOfCode);
        }
        if (_cursor.isNull(_cursorIndexOfHeadOfDepartment)) {
          _result.headOfDepartment = null;
        } else {
          _result.headOfDepartment = _cursor.getString(_cursorIndexOfHeadOfDepartment);
        }
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
  public int countForOwner(final String ownerId) {
    final String _sql = "SELECT COUNT(*) FROM departments WHERE ownerUserId = ?";
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
