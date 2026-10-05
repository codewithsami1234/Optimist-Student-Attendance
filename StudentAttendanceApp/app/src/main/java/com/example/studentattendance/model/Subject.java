package com.example.studentattendance.model;

import java.io.Serializable;

public class Subject implements Serializable {
    private String id;
    private String departmentId;
    private String semesterId;
    private String sectionId;
    private String name;
    private String code;
    private boolean archived;

    public Subject() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getSemesterId() { return semesterId; }
    public void setSemesterId(String semesterId) { this.semesterId = semesterId; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    @Override
    public String toString() {
        if (code == null || code.isEmpty()) {
            return name;
        }
        return name + " (" + code + ")";
    }
}
