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
import com.example.studentattendance.data.local.entity.StudentEntity;
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
public final class StudentDao_Impl implements StudentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StudentEntity> __insertionAdapterOfStudentEntity;

  private final EntityInsertionAdapter<StudentEntity> __insertionAdapterOfStudentEntity_1;

  private final EntityDeletionOrUpdateAdapter<StudentEntity> __updateAdapterOfStudentEntity;

  private final SharedSQLiteStatement __preparedStmtOfReassignDepartment;

  private final SharedSQLiteStatement __preparedStmtOfReassignSemester;

  private final SharedSQLiteStatement __preparedStmtOfReassignSection;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public StudentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStudentEntity = new EntityInsertionAdapter<StudentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `students` (`id`,`ownerUserId`,`rollNumber`,`name`,`departmentId`,`semesterId`,`sectionId`,`email`,`phone`,`archived`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StudentEntity entity) {
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
        if (entity.rollNumber == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.rollNumber);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        if (entity.departmentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.departmentId);
        }
        if (entity.semesterId == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.sectionId);
        }
        if (entity.email == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.email);
        }
        if (entity.phone == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.phone);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(10, _tmp);
      }
    };
    this.__insertionAdapterOfStudentEntity_1 = new EntityInsertionAdapter<StudentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `students` (`id`,`ownerUserId`,`rollNumber`,`name`,`departmentId`,`semesterId`,`sectionId`,`email`,`phone`,`archived`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StudentEntity entity) {
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
        if (entity.rollNumber == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.rollNumber);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        if (entity.departmentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.departmentId);
        }
        if (entity.semesterId == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.sectionId);
        }
        if (entity.email == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.email);
        }
        if (entity.phone == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.phone);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(10, _tmp);
      }
    };
    this.__updateAdapterOfStudentEntity = new EntityDeletionOrUpdateAdapter<StudentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `students` SET `id` = ?,`ownerUserId` = ?,`rollNumber` = ?,`name` = ?,`departmentId` = ?,`semesterId` = ?,`sectionId` = ?,`email` = ?,`phone` = ?,`archived` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StudentEntity entity) {
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
        if (entity.rollNumber == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.rollNumber);
        }
        if (entity.name == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.name);
        }
        if (entity.departmentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.departmentId);
        }
        if (entity.semesterId == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.sectionId);
        }
        if (entity.email == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.email);
        }
        if (entity.phone == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.phone);
        }
        final int _tmp = entity.archived ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.id == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.id);
        }
      }
    };
    this.__preparedStmtOfReassignDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE students SET departmentId = ? WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSemester = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE students SET semesterId = ? WHERE semesterId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE students SET sectionId = ? WHERE sectionId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM students WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final StudentEntity student) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStudentEntity.insert(student);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upsert(final StudentEntity student) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStudentEntity_1.insert(student);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final StudentEntity student) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfStudentEntity.handle(student);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void reassignDepartment(final String oldId, final String newId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignDepartment.acquire();
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
      __preparedStmtOfReassignDepartment.release(_stmt);
    }
  }

  @Override
  public void reassignSemester(final String oldId, final String newId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignSemester.acquire();
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
      __preparedStmtOfReassignSemester.release(_stmt);
    }
  }

  @Override
  public void reassignSection(final String oldId, final String newId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignSection.acquire();
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
      __preparedStmtOfReassignSection.release(_stmt);
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
  public List<StudentEntity> listBySection(final String ownerId, final String sectionId) {
    final String _sql = "SELECT * FROM students WHERE ownerUserId = ? AND sectionId = ? AND archived = 0 ORDER BY rollNumber";
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
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<StudentEntity> _result = new ArrayList<StudentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StudentEntity _item;
        _item = new StudentEntity();
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
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
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
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _item.email = null;
        } else {
          _item.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _item.phone = null;
        } else {
          _item.phone = _cursor.getString(_cursorIndexOfPhone);
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
  public List<StudentEntity> listAllActive(final String ownerId) {
    final String _sql = "SELECT * FROM students WHERE ownerUserId = ? AND archived = 0 ORDER BY name";
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
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<StudentEntity> _result = new ArrayList<StudentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StudentEntity _item;
        _item = new StudentEntity();
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
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
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
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _item.email = null;
        } else {
          _item.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _item.phone = null;
        } else {
          _item.phone = _cursor.getString(_cursorIndexOfPhone);
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
  public List<StudentEntity> listAll(final String ownerId) {
    final String _sql = "SELECT * FROM students WHERE ownerUserId = ? ORDER BY name";
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
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final List<StudentEntity> _result = new ArrayList<StudentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StudentEntity _item;
        _item = new StudentEntity();
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
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
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
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _item.email = null;
        } else {
          _item.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _item.phone = null;
        } else {
          _item.phone = _cursor.getString(_cursorIndexOfPhone);
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
  public StudentEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM students WHERE id = ? AND ownerUserId = ? LIMIT 1";
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
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final StudentEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new StudentEntity();
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
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _result.rollNumber = null;
        } else {
          _result.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
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
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _result.email = null;
        } else {
          _result.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _result.phone = null;
        } else {
          _result.phone = _cursor.getString(_cursorIndexOfPhone);
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
  public StudentEntity findByRollInSection(final String ownerId, final String sectionId,
      final String rollNumber) {
    final String _sql = "SELECT * FROM students WHERE ownerUserId = ? AND sectionId = ? AND rollNumber = ? LIMIT 1";
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
    if (rollNumber == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, rollNumber);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
      final StudentEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new StudentEntity();
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
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _result.rollNumber = null;
        } else {
          _result.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
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
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _result.email = null;
        } else {
          _result.email = _cursor.getString(_cursorIndexOfEmail);
        }
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _result.phone = null;
        } else {
          _result.phone = _cursor.getString(_cursorIndexOfPhone);
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
  public int countInSection(final String ownerId, final String sectionId) {
    final String _sql = "SELECT COUNT(*) FROM students WHERE ownerUserId = ? AND sectionId = ? AND archived = 0";
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
  public int countInDepartment(final String ownerId, final String departmentId) {
    final String _sql = "SELECT COUNT(*) FROM students WHERE ownerUserId = ? AND departmentId = ? AND archived = 0";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
