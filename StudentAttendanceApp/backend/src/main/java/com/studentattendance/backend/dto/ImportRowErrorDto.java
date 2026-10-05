package com.studentattendance.backend.dto;

public class ImportRowErrorDto {
    private int row;
    private String rollNumber;
    private String message;

    public ImportRowErrorDto() {
    }

    public ImportRowErrorDto(int row, String rollNumber, String message) {
        this.row = row;
        this.rollNumber = rollNumber;
        this.message = message;
    }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
