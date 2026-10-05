package com.example.studentattendance.utils;

/**
 * App-wide constant keys (Intent extras, request codes, etc.) kept in one
 * place to avoid typo-prone magic strings scattered across screens.
 */
public final class Constants {

    private Constants() {
    }

    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_SEMESTER = "extra_semester";
    public static final String EXTRA_SECTION = "extra_section";
    public static final String EXTRA_SUBJECT = "extra_subject";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_LECTURE_NUMBER = "extra_lecture_number";
    public static final String EXTRA_LECTURE = "extra_lecture";
    public static final String EXTRA_SESSION = "extra_session";
    public static final String EXTRA_STUDENT_ID = "extra_student_id";
    public static final String EXTRA_OPEN_TAB = "extra_open_tab";
    public static final String EXTRA_MANAGE_STUDENTS = "extra_manage_students";
    public static final String EXTRA_ROSTER_MODE = "extra_roster_mode";

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_DISPLAY_PATTERN = "EEE, dd MMM yyyy";
}
