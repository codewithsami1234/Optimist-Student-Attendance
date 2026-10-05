package com.example.studentattendance.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.studentattendance.data.local.dao.AccountDao;
import com.example.studentattendance.data.local.dao.AdjustmentDao;
import com.example.studentattendance.data.local.dao.AttendanceRecordDao;
import com.example.studentattendance.data.local.dao.AttendanceSessionDao;
import com.example.studentattendance.data.local.dao.DepartmentDao;
import com.example.studentattendance.data.local.dao.LectureDao;
import com.example.studentattendance.data.local.dao.NotificationDao;
import com.example.studentattendance.data.local.dao.SectionDao;
import com.example.studentattendance.data.local.dao.SemesterDao;
import com.example.studentattendance.data.local.dao.StudentDao;
import com.example.studentattendance.data.local.dao.SubjectDao;
import com.example.studentattendance.data.local.entity.AccountEntity;
import com.example.studentattendance.data.local.entity.AdjustmentEntity;
import com.example.studentattendance.data.local.entity.AttendanceRecordEntity;
import com.example.studentattendance.data.local.entity.AttendanceSessionEntity;
import com.example.studentattendance.data.local.entity.DepartmentEntity;
import com.example.studentattendance.data.local.entity.LectureEntity;
import com.example.studentattendance.data.local.entity.NotificationEntity;
import com.example.studentattendance.data.local.entity.SectionEntity;
import com.example.studentattendance.data.local.entity.SemesterEntity;
import com.example.studentattendance.data.local.entity.StudentEntity;
import com.example.studentattendance.data.local.entity.SubjectEntity;

@Database(
        entities = {
                AccountEntity.class,
                DepartmentEntity.class,
                SemesterEntity.class,
                SectionEntity.class,
                SubjectEntity.class,
                LectureEntity.class,
                StudentEntity.class,
                AttendanceSessionEntity.class,
                AttendanceRecordEntity.class,
                AdjustmentEntity.class,
                NotificationEntity.class
        },
        version = 8,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract AccountDao accountDao();
    public abstract DepartmentDao departmentDao();
    public abstract SemesterDao semesterDao();
    public abstract SectionDao sectionDao();
    public abstract SubjectDao subjectDao();
    public abstract LectureDao lectureDao();
    public abstract StudentDao studentDao();
    public abstract AttendanceSessionDao attendanceSessionDao();
    public abstract AttendanceRecordDao attendanceRecordDao();
    public abstract AdjustmentDao adjustmentDao();
    public abstract NotificationDao notificationDao();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE attendance_sessions ADD COLUMN lectureNumber INTEGER NOT NULL DEFAULT 1");
            db.execSQL("DROP INDEX IF EXISTS `index_attendance_sessions_ownerUserId_sectionId_subjectId_date`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_sessions_ownerUserId_sectionId_subjectId_date_lectureNumber` ON `attendance_sessions` (`ownerUserId`, `sectionId`, `subjectId`, `date`, `lectureNumber`)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `lectures` ("
                    + "`id` TEXT NOT NULL, "
                    + "`ownerUserId` TEXT NOT NULL, "
                    + "`sectionId` TEXT, "
                    + "`subjectId` TEXT, "
                    + "`lectureNumber` INTEGER NOT NULL, "
                    + "`title` TEXT, "
                    + "PRIMARY KEY(`id`), "
                    + "FOREIGN KEY(`ownerUserId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, "
                    + "FOREIGN KEY(`sectionId`) REFERENCES `sections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, "
                    + "FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lectures_ownerUserId` ON `lectures` (`ownerUserId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lectures_sectionId` ON `lectures` (`sectionId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lectures_subjectId` ON `lectures` (`subjectId`)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_lectures_ownerUserId_sectionId_subjectId_lectureNumber` ON `lectures` (`ownerUserId`, `sectionId`, `subjectId`, `lectureNumber`)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DELETE FROM attendance_records WHERE rowid NOT IN ("
                    + "SELECT MIN(rowid) FROM attendance_records "
                    + "GROUP BY ownerUserId, sessionId, studentId)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS "
                    + "`index_attendance_records_ownerUserId_sessionId_studentId` "
                    + "ON `attendance_records` (`ownerUserId`, `sessionId`, `studentId`)");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DELETE FROM attendance_records WHERE sessionId IN ("
                    + "SELECT id FROM attendance_sessions WHERE rowid NOT IN ("
                    + "SELECT MIN(rowid) FROM attendance_sessions "
                    + "GROUP BY ownerUserId, sectionId, subjectId, date))");
            db.execSQL("DELETE FROM attendance_sessions WHERE rowid NOT IN ("
                    + "SELECT MIN(rowid) FROM attendance_sessions "
                    + "GROUP BY ownerUserId, sectionId, subjectId, date)");
            db.execSQL("DROP INDEX IF EXISTS "
                    + "`index_attendance_sessions_ownerUserId_sectionId_subjectId_date_lectureNumber`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS "
                    + "`index_attendance_sessions_ownerUserId_sectionId_subjectId_date` "
                    + "ON `attendance_sessions` (`ownerUserId`, `sectionId`, `subjectId`, `date`)");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE subjects ADD COLUMN semesterId TEXT");
            db.execSQL("ALTER TABLE subjects ADD COLUMN sectionId TEXT");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_semesterId` ON `subjects` (`semesterId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_sectionId` ON `subjects` (`sectionId`)");
            db.execSQL("DROP INDEX IF EXISTS `index_subjects_ownerUserId_departmentId_code`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS "
                    + "`index_subjects_ownerUserId_sectionId_code` "
                    + "ON `subjects` (`ownerUserId`, `sectionId`, `code`)");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE departments ADD COLUMN shift TEXT NOT NULL DEFAULT 'MORNING'");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_departments_ownerUserId_name_shift` "
                    + "ON `departments` (`ownerUserId`, `name`, `shift`)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "student_attendance.db")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                                    MIGRATION_6_7, MIGRATION_7_8)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
