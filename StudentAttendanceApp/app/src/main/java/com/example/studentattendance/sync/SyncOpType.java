package com.example.studentattendance.sync;

public final class SyncOpType {

    public static final String CREATE_DEPARTMENT = "CREATE_DEPARTMENT";
    public static final String UPDATE_DEPARTMENT = "UPDATE_DEPARTMENT";
    public static final String DELETE_DEPARTMENT = "DELETE_DEPARTMENT";
    public static final String CREATE_SEMESTER = "CREATE_SEMESTER";
    public static final String UPDATE_SEMESTER = "UPDATE_SEMESTER";
    public static final String DELETE_SEMESTER = "DELETE_SEMESTER";
    public static final String CREATE_SECTION = "CREATE_SECTION";
    public static final String UPDATE_SECTION = "UPDATE_SECTION";
    public static final String DELETE_SECTION = "DELETE_SECTION";
    public static final String CREATE_SUBJECT = "CREATE_SUBJECT";
    public static final String UPDATE_SUBJECT = "UPDATE_SUBJECT";
    public static final String DELETE_SUBJECT = "DELETE_SUBJECT";
    public static final String ADD_STUDENT = "ADD_STUDENT";
    public static final String UPDATE_STUDENT = "UPDATE_STUDENT";
    public static final String TRANSFER_STUDENT = "TRANSFER_STUDENT";
    public static final String DELETE_STUDENT = "DELETE_STUDENT";
    public static final String IMPORT_STUDENTS = "IMPORT_STUDENTS";
    public static final String SAVE_ATTENDANCE = "SAVE_ATTENDANCE";
    public static final String CREATE_ADJUSTMENT = "CREATE_ADJUSTMENT";
    public static final String MARK_NOTIFICATION_READ = "MARK_NOTIFICATION_READ";
    public static final String MARK_ALL_NOTIFICATIONS_READ = "MARK_ALL_NOTIFICATIONS_READ";

    private SyncOpType() {
    }
}
