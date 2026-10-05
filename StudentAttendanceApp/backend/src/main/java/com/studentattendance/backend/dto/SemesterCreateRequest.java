package com.studentattendance.backend.dto;

public class SemesterCreateRequest {
    private String name;
    private int semesterNumber;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }
}
