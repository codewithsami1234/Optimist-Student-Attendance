package com.studentattendance.backend.dto;

public class AttendanceAdjustmentRequest {
    private int delta;
    private String reason;

    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
