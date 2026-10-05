package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents a semester that belongs to a department.
 */
public class Semester implements Serializable {

    private String id;
    private String departmentId;
    private String name; // e.g. "Semester 1"
    private int semesterNumber;

    public Semester() {
    }

    public Semester(String id, String departmentId, String name, int semesterNumber) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.semesterNumber = semesterNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    @Override
    public String toString() {
        return name;
    }
}
