package com.studentattendance.backend.dto;

import java.util.ArrayList;
import java.util.List;

/** Result of POST /api/students/import - never silently drops rows. */
public class ImportResultDto {
    private int imported;
    private int duplicates;
    private int invalid;
    private List<ImportRowErrorDto> errors = new ArrayList<>();

    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public int getDuplicates() { return duplicates; }
    public void setDuplicates(int duplicates) { this.duplicates = duplicates; }
    public int getInvalid() { return invalid; }
    public void setInvalid(int invalid) { this.invalid = invalid; }
    public List<ImportRowErrorDto> getErrors() { return errors; }
    public void setErrors(List<ImportRowErrorDto> errors) { this.errors = errors; }

    public void addError(int row, String rollNumber, String message) {
        errors.add(new ImportRowErrorDto(row, rollNumber, message));
    }
}
