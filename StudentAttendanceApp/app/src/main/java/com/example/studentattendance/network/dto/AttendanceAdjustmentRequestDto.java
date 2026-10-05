package com.example.studentattendance.network.dto;

public class AttendanceAdjustmentRequestDto {
    private final int delta;
    private final String reason;

    public AttendanceAdjustmentRequestDto(int delta, String reason) {
        this.delta = delta;
        this.reason = reason;
    }
}
