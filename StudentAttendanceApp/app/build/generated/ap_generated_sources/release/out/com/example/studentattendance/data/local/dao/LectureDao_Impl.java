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
import com.example.studentattendance.data.local.entity.LectureEntity;
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
public final class LectureDao_Impl implements LectureDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LectureEntity> __insertionAdapterOfLectureEntity;

  private final EntityDeletionOrUpdateAdapter<LectureEntity> __updateAdapterOfLectureEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public LectureDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLectureEntity = new EntityInsertionAdapter<LectureEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `lectures` (`id`,`ownerUserId`,`sectionId`,`subjectId`,`lectureNumber`,`title`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LectureEntity entity) {
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
        if (entity.sectionId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.sectionId);
        }
        if (entity.subjectId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.subjectId);
        }
        statement.bindLong(5, entity.lectureNumber);
        if (entity.title == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.title);
        }
      }
    };
    this.__updateAdapterOfLectureEntity = new EntityDeletionOrUpdateAdapter<LectureEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `lectures` SET `id` = ?,`ownerUserId` = ?,`sectionId` = ?,`subjectId` = ?,`lectureNumber` = ?,`title` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LectureEntity entity) {
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
        if (entity.sectionId == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.sectionId);
        }
        if (entity.subjectId == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.subjectId);
        }
        statement.bindLong(5, entity.lectureNumber);
        if (entity.title == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.title);
        }
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
        final String _query = "DELETE FROM lectures WHERE id = ? AND ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final LectureEntity lecture) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfLectureEntity.insert(lecture);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final LectureEntity lecture) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfLectureEntity.handle(lecture);
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
  public List<LectureEntity> listBySectionAndSubject(final String ownerId, final String sectionId,
      final String subjectId) {
    final String _sql = "SELECT * FROM lectures WHERE ownerUserId = ? AND sectionId = ? AND subjectId = ? ORDER BY lectureNumber";
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
    if (subjectId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, subjectId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final List<LectureEntity> _result = new ArrayList<LectureEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LectureEntity _item;
        _item = new LectureEntity();
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
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _item.sectionId = null;
        } else {
          _item.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _item.subjectId = null;
        } else {
          _item.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        _item.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LectureEntity findOwned(final String id, final String ownerId) {
    final String _sql = "SELECT * FROM lectures WHERE id = ? AND ownerUserId = ? LIMIT 1";
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
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final LectureEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new LectureEntity();
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
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _result.subjectId = null;
        } else {
          _result.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        _result.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _result.title = null;
        } else {
          _result.title = _cursor.getString(_cursorIndexOfTitle);
        }
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
  public LectureEntity findByNumber(final String ownerId, final String sectionId,
      final String subjectId, final int lectureNumber) {
    final String _sql = "SELECT * FROM lectures WHERE ownerUserId = ? AND sectionId = ? AND subjectId = ? AND lectureNumber = ? LIMIT 1";
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
    _statement.bindLong(_argIndex, lectureNumber);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfSectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sectionId");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfLectureNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "lectureNumber");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final LectureEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new LectureEntity();
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
        if (_cursor.isNull(_cursorIndexOfSectionId)) {
          _result.sectionId = null;
        } else {
          _result.sectionId = _cursor.getString(_cursorIndexOfSectionId);
        }
        if (_cursor.isNull(_cursorIndexOfSubjectId)) {
          _result.subjectId = null;
        } else {
          _result.subjectId = _cursor.getString(_cursorIndexOfSubjectId);
        }
        _result.lectureNumber = _cursor.getInt(_cursorIndexOfLectureNumber);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _result.title = null;
        } else {
          _result.title = _cursor.getString(_cursorIndexOfTitle);
        }
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
