package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents a section within a semester (e.g. "Section A").
 */
public class Section implements Serializable {

    private String id;
    private String semesterId;
    private String name; // e.g. "Section A"
    private int studentCount;

    public Section() {
    }

    public Section(String id, String semesterId, String name, int studentCount) {
        this.id = id;
        this.semesterId = semesterId;
        this.name = name;
        this.studentCount = studentCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
