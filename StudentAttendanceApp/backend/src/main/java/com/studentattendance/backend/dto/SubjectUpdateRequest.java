package com.studentattendance.backend.dto;

public class SubjectUpdateRequest {
    private String name;
    private String code;
    private Boolean archived;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}
