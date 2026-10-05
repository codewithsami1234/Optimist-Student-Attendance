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
import com.example.studentattendance.data.local.Converters;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
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
public final class AttendanceRecordDao_Impl implements AttendanceRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttendanceRecordEntity> __insertionAdapterOfAttendanceRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfReassignDepartment;

  private final SharedSQLiteStatement __preparedStmtOfReassignSemester;

  private final SharedSQLiteStatement __preparedStmtOfReassignSection;

  private final SharedSQLiteStatement __preparedStmtOfReassignStudent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwner;

  public AttendanceRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttendanceRecordEntity = new EntityInsertionAdapter<AttendanceRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_records` (`id`,`ownerUserId`,`sessionId`,`studentId`,`studentName`,`rollNumber`,`departmentId`,`semesterId`,`sectionId`,`date`,`status`,`markedBy`,`markedAtMillis`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AttendanceRecordEntity entity) {
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
        if (entity.sessionId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.sessionId);
        }
        if (entity.studentId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.studentId);
        }
        if (entity.studentName == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.studentName);
        }
        if (entity.rollNumber == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.rollNumber);
        }
        if (entity.departmentId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.departmentId);
        }
        if (entity.semesterId == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.semesterId);
        }
        if (entity.sectionId == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.sectionId);
        }
        if (entity.date == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.date);
        }
        final String _tmp = Converters.fromStatus(entity.status);
        if (_tmp == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp);
        }
        if (entity.markedBy == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.markedBy);
        }
        statement.bindLong(13, entity.markedAtMillis);
      }
    };
    this.__preparedStmtOfReassignDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_records SET departmentId = ? WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSemester = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_records SET semesterId = ? WHERE semesterId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_records SET sectionId = ? WHERE sectionId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignStudent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_records SET studentId = ? WHERE studentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBySession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attendance_records WHERE sessionId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByOwner = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attendance_records WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<AttendanceRecordEntity> records) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAttendanceRecordEntity.insert(records);
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
  public void deleteBySession(final String sessionId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBySession.acquire();
    int _argIndex = 1;
    if (sessionId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, sessionId);
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
      __preparedStmtOfDeleteBySession.release(_stmt);
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
  public List<AttendanceRecordEntity> listBySession(final String sessionId, final String ownerId) {
    final String _sql = "SELECT * FROM attendance_records WHERE sessionId = ? AND ownerUserId = ? ORDER BY rollNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (sessionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sessionId);
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
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
      final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
      final int _cursorIndexOfMarkedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAtMillis");
      final List<AttendanceRecordEntity> _result = new ArrayList<AttendanceRecordEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AttendanceRecordEntity _item;
        _item = new AttendanceRecordEntity();
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
        if (_cursor.isNull(_cursorIndexOfSessionId)) {
          _item.sessionId = null;
        } else {
          _item.sessionId = _cursor.getString(_cursorIndexOfSessionId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentId)) {
          _item.studentId = null;
        } else {
          _item.studentId = _cursor.getString(_cursorIndexOfStudentId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentName)) {
          _item.studentName = null;
        } else {
          _item.studentName = _cursor.getString(_cursorIndexOfStudentName);
        }
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
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
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _item.date = null;
        } else {
          _item.date = _cursor.getString(_cursorIndexOfDate);
        }
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toStatus(_tmp);
        if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
          _item.markedBy = null;
        } else {
          _item.markedBy = _cursor.getString(_cursorIndexOfMarkedBy);
        }
        _item.markedAtMillis = _cursor.getLong(_cursorIndexOfMarkedAtMillis);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AttendanceRecordEntity> listAll(final String ownerId) {
    final String _sql = "SELECT * FROM attendance_records WHERE ownerUserId = ?";
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
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
      final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
      final int _cursorIndexOfMarkedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAtMillis");
      final List<AttendanceRecordEntity> _result = new ArrayList<AttendanceRecordEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AttendanceRecordEntity _item;
        _item = new AttendanceRecordEntity();
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
        if (_cursor.isNull(_cursorIndexOfSessionId)) {
          _item.sessionId = null;
        } else {
          _item.sessionId = _cursor.getString(_cursorIndexOfSessionId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentId)) {
          _item.studentId = null;
        } else {
          _item.studentId = _cursor.getString(_cursorIndexOfStudentId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentName)) {
          _item.studentName = null;
        } else {
          _item.studentName = _cursor.getString(_cursorIndexOfStudentName);
        }
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
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
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _item.date = null;
        } else {
          _item.date = _cursor.getString(_cursorIndexOfDate);
        }
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toStatus(_tmp);
        if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
          _item.markedBy = null;
        } else {
          _item.markedBy = _cursor.getString(_cursorIndexOfMarkedBy);
        }
        _item.markedAtMillis = _cursor.getLong(_cursorIndexOfMarkedAtMillis);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AttendanceRecordEntity> listByStudent(final String ownerId, final String studentId) {
    final String _sql = "SELECT * FROM attendance_records WHERE ownerUserId = ? AND studentId = ? ORDER BY date DESC";
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
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
      final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
      final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfMarkedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "markedBy");
      final int _cursorIndexOfMarkedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "markedAtMillis");
      final List<AttendanceRecordEntity> _result = new ArrayList<AttendanceRecordEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AttendanceRecordEntity _item;
        _item = new AttendanceRecordEntity();
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
        if (_cursor.isNull(_cursorIndexOfSessionId)) {
          _item.sessionId = null;
        } else {
          _item.sessionId = _cursor.getString(_cursorIndexOfSessionId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentId)) {
          _item.studentId = null;
        } else {
          _item.studentId = _cursor.getString(_cursorIndexOfStudentId);
        }
        if (_cursor.isNull(_cursorIndexOfStudentName)) {
          _item.studentName = null;
        } else {
          _item.studentName = _cursor.getString(_cursorIndexOfStudentName);
        }
        if (_cursor.isNull(_cursorIndexOfRollNumber)) {
          _item.rollNumber = null;
        } else {
          _item.rollNumber = _cursor.getString(_cursorIndexOfRollNumber);
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
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _item.date = null;
        } else {
          _item.date = _cursor.getString(_cursorIndexOfDate);
        }
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toStatus(_tmp);
        if (_cursor.isNull(_cursorIndexOfMarkedBy)) {
          _item.markedBy = null;
        } else {
          _item.markedBy = _cursor.getString(_cursorIndexOfMarkedBy);
        }
        _item.markedAtMillis = _cursor.getLong(_cursorIndexOfMarkedAtMillis);
        _result.add(_item);
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
