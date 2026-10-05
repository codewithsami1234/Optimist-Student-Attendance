package com.example.studentattendance.model;

import java.io.Serializable;

public class ImportRowError implements Serializable {
    private int row;
    private String rollNumber;
    private String message;

    public ImportRowError() {
    }

    public ImportRowError(int row, String message) {
        this.row = row;
        this.message = message;
    }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    @Override
    public String toString() {
        String roll = rollNumber == null || rollNumber.isEmpty() ? "" : " (" + rollNumber + ")";
        return "Row " + row + roll + ": " + message;
    }
}
