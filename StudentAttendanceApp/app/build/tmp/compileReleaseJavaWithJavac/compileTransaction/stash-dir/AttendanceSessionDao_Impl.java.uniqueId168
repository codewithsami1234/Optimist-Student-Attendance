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
import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
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
public final class AttendanceSessionDao_Impl implements AttendanceSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttendanceSessionEntity> __insertionAdapterOfAttendanceSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfReassignDepartment;

  private final SharedSQLiteStatement __preparedStmtOfReassignSemester;

  private final SharedSQLiteStatement __preparedStmtOfReassignSection;

  private final SharedSQLiteStatement __preparedStmtOfReassignSubject;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public AttendanceSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttendanceSessionEntity = new EntityInsertionAdapter<AttendanceSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_sessions` (`id`,`ownerUserId`,`departmentId`,`departmentName`,`semesterId`,`semesterName`,`sectionId`,`sectionName`,`subjectId`,`subjectName`,`date`,`lectureNumber`,`createdAtMillis`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AttendanceSessionEntity entity) {
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
        if (entity.departmentName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.departmentName);
        }
        if (entity.semesterId == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.semesterId);
        }
        if (entity.semesterName == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.semesterName);
        }
        if (entity.sectionId == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.sectionId);
        }
        if (entity.sectionName == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.sectionName);
        }
        if (entity.subjectId == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.subjectId);
        }
        if (entity.subjectName == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.subjectName);
        }
        if (entity.date == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.date);
        }
        statement.bindLong(12, entity.lectureNumber);
        statement.bindLong(13, entity.createdAtMillis);
      }
    };
    this.__preparedStmtOfReassignDepartment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_sessions SET departmentId = ? WHERE departmentId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSemester = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_sessions SET semesterId = ? WHERE semesterId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_sessions SET sectionId = ? WHERE sectionId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReassignSubject = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attendance_sessions SET subjectId = ? WHERE subjectId = ? AND ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attendance_sessions WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final AttendanceSessionEntity session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAttendanceSessionEntity.insert(session);
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
  public void reassignSubject(final String oldId, final String newId, final String ownerId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfReassignSubject.acquire();
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
      __preparedStmtOfReassignSubject.release(_stmt);
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
  public AttendanceSessionEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE id = ? AND ownerUserId = ? LIMIT 1";
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
      final int _cursorIndexOfDepartmentName = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentName");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSemesterName = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterName");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSectionName = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionName");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AttendanceSessionEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AttendanceSessionEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentName)) {
          _result.departmentName = null;
        } else {
          _result.departmentName = _cursor.getString(_cursorIndexOfDepartmentName);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterName)) {
          _result.semesterName = null;
        } else {
          _result.semesterName = _cursor.getString(_cursorIndexOfSemesterName);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionName)) {
          _result.sectionName = null;
        } else {
          _result.sectionName = _cursor.getString(_cursorIndexOfSectionName);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _result.subjectId = null;
        } else {
          _result.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _result.subjectName = null;
        } else {
          _result.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _result.date = null;
        } else {
          _result.date = _cursor.getString(_cursorIndexOfDate);
        }
        _result.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
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
  public AttendanceSessionEntity findByLecture(final String ownerId, final String sectionId,
      final String subjectId, final String date, final int lectureNumber) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE ownerUserId = ? AND sectionId = ? AND subjectId = ? AND date = ? AND lectureNumber = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
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
    if (subjectId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, subjectId);
    }
    _argIndex = 4;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    _argIndex = 5;
    _statement.bindLong(_argIndex, lectureNumber);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfDepartmentName = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentName");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSemesterName = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterName");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSectionName = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionName");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AttendanceSessionEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AttendanceSessionEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentName)) {
          _result.departmentName = null;
        } else {
          _result.departmentName = _cursor.getString(_cursorIndexOfDepartmentName);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterName)) {
          _result.semesterName = null;
        } else {
          _result.semesterName = _cursor.getString(_cursorIndexOfSemesterName);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionName)) {
          _result.sectionName = null;
        } else {
          _result.sectionName = _cursor.getString(_cursorIndexOfSectionName);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _result.subjectId = null;
        } else {
          _result.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _result.subjectName = null;
        } else {
          _result.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _result.date = null;
        } else {
          _result.date = _cursor.getString(_cursorIndexOfDate);
        }
        _result.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
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
  public AttendanceSessionEntity findByTriple(final String ownerId, final String sectionId,
      final String subjectId, final String date) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE ownerUserId = ? AND sectionId = ? AND subjectId = ? AND date = ? AND lectureNumber = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
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
    if (subjectId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, subjectId);
    }
    _argIndex = 4;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfDepartmentName = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentName");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSemesterName = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterName");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSectionName = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionName");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final AttendanceSessionEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AttendanceSessionEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentName)) {
          _result.departmentName = null;
        } else {
          _result.departmentName = _cursor.getString(_cursorIndexOfDepartmentName);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _result.semesterId = null;
        } else {
          _result.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterName)) {
          _result.semesterName = null;
        } else {
          _result.semesterName = _cursor.getString(_cursorIndexOfSemesterName);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionName)) {
          _result.sectionName = null;
        } else {
          _result.sectionName = _cursor.getString(_cursorIndexOfSectionName);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _result.subjectId = null;
        } else {
          _result.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _result.subjectName = null;
        } else {
          _result.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _result.date = null;
        } else {
          _result.date = _cursor.getString(_cursorIndexOfDate);
        }
        _result.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
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
  public List<AttendanceSessionEntity> listAll(final String ownerId) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE ownerUserId = ? ORDER BY date DESC, lectureNumber ASC, createdAtMillis DESC";
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
      final int _cursorIndexOfDepartmentName = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentName");
      final int _cursorIndexOfSemesterId = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterId");
      final int _cursorIndexOfSemesterName = CursorUtil.getColumnIndexOrThrow(_cursor, "semesterName");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSectionName = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionName");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfCreatedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMillis");
      final List<AttendanceSessionEntity> _result = new ArrayList<AttendanceSessionEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AttendanceSessionEntity _item;
        _item = new AttendanceSessionEntity();
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
        if (_cursor.isNull(_cursorIndexOfDepartmentName)) {
          _item.departmentName = null;
        } else {
          _item.departmentName = _cursor.getString(_cursorIndexOfDepartmentName);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterId)) {
          _item.semesterId = null;
        } else {
          _item.semesterId = _cursor.getString(_cursorIndexOfSemesterId);
        }
        if (_cursor.isNull(_cursorIndexOfSemesterName)) {
          _item.semesterName = null;
        } else {
          _item.semesterName = _cursor.getString(_cursorIndexOfSemesterName);
        }
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _item.sectionId = null;
        } else {
          _item.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSectionName)) {
          _item.sectionName = null;
        } else {
          _item.sectionName = _cursor.getString(_cursorIndexOfSectionName);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _item.subjectId = null;
        } else {
          _item.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _item.date = null;
        } else {
          _item.date = _cursor.getString(_cursorIndexOfDate);
        }
        _item.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
        _item.createdAtMillis = _cursor.getLong(_cursorIndexOfCreatedAtMillis);
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
