package com.studentattendance.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentTransferRequest {
    @NotBlank(message = "Please select a target section.")
    private String sectionId;

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
}
