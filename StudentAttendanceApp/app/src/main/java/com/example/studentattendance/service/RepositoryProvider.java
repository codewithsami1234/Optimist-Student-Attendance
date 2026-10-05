package com.example.studentattendance.service;

import android.content.Context;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.repository.AnalyticsRepository;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.repository.NotificationRepository;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.repository.impl.RoomAnalyticsRepository;
import com.example.studentattendance.repository.impl.RoomAttendanceAdjustmentRepository;
import com.example.studentattendance.repository.impl.RoomAttendanceRepository;
import com.example.studentattendance.repository.impl.RoomAuthRepository;
import com.example.studentattendance.repository.impl.RoomDepartmentRepository;
import com.example.studentattendance.repository.impl.RoomNotificationRepository;
import com.example.studentattendance.repository.impl.RoomStudentRepository;
import com.example.studentattendance.sync.PendingSyncStore;
import com.example.studentattendance.sync.PrefsKvStore;
import com.example.studentattendance.utils.PreferenceManager;

/**
 * Process-wide repository graph. The live app uses Room/SQLite as the source
 * of truth so register, login, and academic CRUD work without a network.
 */
public final class RepositoryProvider {

    private static volatile RepositoryProvider instance;

    private final AuthRepository authRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AttendanceAdjustmentRepository attendanceAdjustmentRepository;
    private final NotificationRepository notificationRepository;
    private final ExcelService excelService;
    private final PendingSyncStore pendingSyncStore;

    private RepositoryProvider(Context appContext) {
        PreferenceManager preferenceManager = new PreferenceManager(appContext);
        AppDatabase database = AppDatabase.getInstance(appContext);
        this.pendingSyncStore = new PendingSyncStore(new PrefsKvStore(appContext));
        this.authRepository = new RoomAuthRepository(appContext, preferenceManager, database);
        this.departmentRepository = new RoomDepartmentRepository(preferenceManager, database);
        this.studentRepository = new RoomStudentRepository(preferenceManager, database);
        this.attendanceRepository = new RoomAttendanceRepository(preferenceManager, database);
        this.analyticsRepository = new RoomAnalyticsRepository(preferenceManager, database);
        this.attendanceAdjustmentRepository = new RoomAttendanceAdjustmentRepository(preferenceManager, database);
        this.notificationRepository = new RoomNotificationRepository(preferenceManager, database);
        this.excelService = new CsvExcelService(appContext);
    }

    public static RepositoryProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (RepositoryProvider.class) {
                if (instance == null) {
                    instance = new RepositoryProvider(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public AuthRepository getAuthRepository() { return authRepository; }
    public DepartmentRepository getDepartmentRepository() { return departmentRepository; }
    public StudentRepository getStudentRepository() { return studentRepository; }
    public AttendanceRepository getAttendanceRepository() { return attendanceRepository; }
    public AnalyticsRepository getAnalyticsRepository() { return analyticsRepository; }
    public AttendanceAdjustmentRepository getAttendanceAdjustmentRepository() { return attendanceAdjustmentRepository; }
    public NotificationRepository getNotificationRepository() { return notificationRepository; }
    public ExcelService getExcelService() { return excelService; }
    public PendingSyncStore getPendingSyncStore() { return pendingSyncStore; }
}
