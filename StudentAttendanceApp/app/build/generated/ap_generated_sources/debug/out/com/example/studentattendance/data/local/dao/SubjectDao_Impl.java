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
import com.example.studentattendance.data.local.entity.SubjectEntity;
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
public final class SubjectDao_Impl implements SubjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SubjectEntity> __insertionAdapterOfSubjectEntity;

  private final EntityInsertionAdapter<SubjectEntity> __insertionAdapterOfSubjectEntity_1;

  private final EntityDeletionOrUpdateAdapter<SubjectEntity> __updateAdapterOfSubjectEntity;

  private final SharedSQLiteStatement __preparedStmtOfReassignDepartment;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByDepartment;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwner;

  public SubjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSubjectEntity = new EntityInsertionAdapter<SubjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `subjects` (`id`,`ownerUserId`,`departmentId`,`semesterId`,`sectionId`,`name`,`code`,`archived`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SubjectEntity entity) {
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
        if (entity.semesterId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.sectionId);
        }
        if (entity.name == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.code);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__insertionAdapterOfSubjectEntity_1 = new EntityInsertionAdapter<SubjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `subjects` (`id`,`ownerUserId`,`departmentId`,`semesterId`,`sectionId`,`name`,`code`,`archived`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SubjectEntity entity) {
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
        if (entity.semesterId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.sectionId);
        }
        if (entity.name == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.code);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__updateAdapterOfSubjectEntity = new EntityDeletionOrUpdateAdapter<SubjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `subjects` SET `id` = ?,`ownerUserId` = ?,`departmentId` = ?,`semesterId` = ?,`sectionId` = ?,`name` = ?,`code` = ?,`archived` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SubjectEntity entity) {
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
        if (entity.semesterId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.sectionId);
        }
        if (entity.name == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.code);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.id == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.id);
        }
      }
    };
    this.__preparedStmtOfReassignDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE subjects SET departmentId = ? WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM subjects WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM subjects WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByOwner = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM subjects WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final SubjectEntity subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSubjectEntity.insert(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final SubjectEntity subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSubjectEntity_1.insert(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final SubjectEntity subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSubjectEntity.handle(subject);
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
  public List<SubjectEntity> listByDepartment(final String ownerId, final String departmentId) {
    final String _sql = "SELECT * FROM subjects WHERE ownerUserId = ? AND departmentId = ? AND archived = 0 ORDER BY name";
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
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<SubjectEntity> _result = new ArrayList<SubjectEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SubjectEntity _item;
        _item = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _item.semesterId = null;
        } else {
          _item.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _item.sectionId = null;
        } else {
          _item.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
  public List<SubjectEntity> listActive(final String ownerId) {
    final String _sql = "SELECT * FROM subjects WHERE ownerUserId = ? AND archived = 0 ORDER BY name";
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
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<SubjectEntity> _result = new ArrayList<SubjectEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SubjectEntity _item;
        _item = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _item.semesterId = null;
        } else {
          _item.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _item.sectionId = null;
        } else {
          _item.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
  public List<SubjectEntity> listBySection(final String ownerId, final String sectionId) {
    final String _sql = "SELECT * FROM subjects WHERE ownerUserId = ? AND sectionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (sectionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sectionId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<SubjectEntity> _result = new ArrayList<SubjectEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SubjectEntity _item;
        _item = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _item.semesterId = null;
        } else {
          _item.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _item.sectionId = null;
        } else {
          _item.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
  public int countInSection(final String ownerId, final String sectionId) {
    final String _sql = "SELECT COUNT(*) FROM subjects WHERE ownerUserId = ? AND sectionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (sectionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sectionId);
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

  @Override
  public int countInSemester(final String ownerId, final String semesterId) {
    final String _sql = "SELECT COUNT(*) FROM subjects WHERE ownerUserId = ? AND semesterId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (semesterId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, semesterId);
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

  @Override
  public int countInDepartment(final String ownerId, final String departmentId) {
    final String _sql = "SELECT COUNT(*) FROM subjects WHERE ownerUserId = ? AND departmentId = ?";
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

  @Override
  public SubjectEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM subjects WHERE id = ? AND ownerUserId = ? LIMIT 1";
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
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final SubjectEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
  public SubjectEntity findByCode(final String ownerId, final String departmentId,
      final String code) {
    final String _sql = "SELECT * FROM subjects WHERE ownerUserId = ? AND departmentId = ? AND code = ? LIMIT 1";
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
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final SubjectEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
  public SubjectEntity findByCodeInSection(final String ownerId, final String sectionId,
      final String code) {
    final String _sql = "SELECT * FROM subjects WHERE ownerUserId = ? AND sectionId = ? AND code = ? COLLATE NOCASE LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    _argIndex = 2;
    if (sectionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sectionId);
    }
    _argIndex = 3;
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
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final SubjectEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new SubjectEntity();
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
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
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
