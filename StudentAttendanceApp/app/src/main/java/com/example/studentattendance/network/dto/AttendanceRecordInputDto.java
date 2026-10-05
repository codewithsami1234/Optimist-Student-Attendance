package com.example.studentattendance.network.dto;

import com.example.studentattendance.model.AttendanceStatus;

/** One row inside an AttendanceSaveRequestDto. */
public class AttendanceRecordInputDto {
    private final String studentId;
    private final String studentName;
    private final String rollNumber;
    private final AttendanceStatus status;

    public AttendanceRecordInputDto(String studentId, String studentName, String rollNumber, AttendanceStatus status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.status = status;
    }
}
