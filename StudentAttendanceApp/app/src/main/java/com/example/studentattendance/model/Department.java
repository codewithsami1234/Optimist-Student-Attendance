package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents an academic department (e.g. Computer Science).
 */
public class Department implements Serializable {

    private String id;
    private String name;
    private String code;
    private int totalStudents;
    private String headOfDepartment;
    /** {@link DepartmentShift#MORNING} or {@link DepartmentShift#EVENING}. */
    private String shift = DepartmentShift.MORNING;

    public Department() {
    }

    public Department(String id, String name, String code, int totalStudents, String headOfDepartment) {
        this(id, name, code, totalStudents, headOfDepartment, DepartmentShift.MORNING);
    }

    public Department(String id, String name, String code, int totalStudents, String headOfDepartment,
                      String shift) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalStudents = totalStudents;
        this.headOfDepartment = headOfDepartment;
        this.shift = DepartmentShift.normalize(shift);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public String getHeadOfDepartment() {
        return headOfDepartment;
    }

    public void setHeadOfDepartment(String headOfDepartment) {
        this.headOfDepartment = headOfDepartment;
    }

    public String getShift() {
        return DepartmentShift.normalize(shift);
    }

    public void setShift(String shift) {
        this.shift = DepartmentShift.normalize(shift);
    }

    /** Display label that distinguishes Morning vs Evening departments. */
    public String displayLabel() {
        String base = name == null || name.trim().isEmpty() ? "Department" : name.trim();
        return base + " — " + DepartmentShift.label(shift);
    }

    @Override
    public String toString() {
        return displayLabel();
    }
}
