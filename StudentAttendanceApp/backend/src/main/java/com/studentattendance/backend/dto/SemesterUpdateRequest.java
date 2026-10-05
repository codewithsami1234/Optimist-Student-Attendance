package com.studentattendance.backend.dto;

public class SemesterUpdateRequest {
    private String name;
    private Integer semesterNumber;
    private Boolean archived;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(Integer semesterNumber) { this.semesterNumber = semesterNumber; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}
