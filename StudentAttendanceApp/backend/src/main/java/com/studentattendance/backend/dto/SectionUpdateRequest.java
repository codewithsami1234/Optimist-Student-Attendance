package com.studentattendance.backend.dto;

public class SectionUpdateRequest {
    private String name;
    private Boolean archived;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}
