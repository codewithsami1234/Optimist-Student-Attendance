package com.example.studentattendance.data.local;

import androidx.room.TypeConverter;

import com.example.studentattendance.model.AttendanceStatus;

public final class Converters {

    @TypeConverter
    public static String fromStatus(AttendanceStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static AttendanceStatus toStatus(String value) {
        if (value == null || value.isEmpty()) {
            return AttendanceStatus.NOT_MARKED;
        }
        try {
            return AttendanceStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return AttendanceStatus.NOT_MARKED;
        }
    }
}
