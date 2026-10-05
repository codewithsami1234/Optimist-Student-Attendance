package com.example.studentattendance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImportResult implements Serializable {
    private int imported;
    private int duplicates;
    private int invalid;
    private int totalRows;
    private int validRows;
    private int reused;
    private int departmentsCreated;
    private int departmentsReused;
    private int semestersCreated;
    private int semestersReused;
    private int sectionsCreated;
    private int sectionsReused;
    private int coursesCreated;
    private int coursesReused;
    private List<ImportRowError> errors = new ArrayList<>();

    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public int getDuplicates() { return duplicates; }
    public void setDuplicates(int duplicates) { this.duplicates = duplicates; }
    public int getInvalid() { return invalid; }
    public void setInvalid(int invalid) { this.invalid = invalid; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getValidRows() { return validRows; }
    public void setValidRows(int validRows) { this.validRows = validRows; }
    public int getReused() { return reused; }
    public void setReused(int reused) { this.reused = reused; }
    public int getDepartmentsCreated() { return departmentsCreated; }
    public void setDepartmentsCreated(int departmentsCreated) { this.departmentsCreated = departmentsCreated; }
    public int getDepartmentsReused() { return departmentsReused; }
    public void setDepartmentsReused(int departmentsReused) { this.departmentsReused = departmentsReused; }
    public int getSemestersCreated() { return semestersCreated; }
    public void setSemestersCreated(int semestersCreated) { this.semestersCreated = semestersCreated; }
    public int getSemestersReused() { return semestersReused; }
    public void setSemestersReused(int semestersReused) { this.semestersReused = semestersReused; }
    public int getSectionsCreated() { return sectionsCreated; }
    public void setSectionsCreated(int sectionsCreated) { this.sectionsCreated = sectionsCreated; }
    public int getSectionsReused() { return sectionsReused; }
    public void setSectionsReused(int sectionsReused) { this.sectionsReused = sectionsReused; }
    public int getCoursesCreated() { return coursesCreated; }
    public void setCoursesCreated(int coursesCreated) { this.coursesCreated = coursesCreated; }
    public int getCoursesReused() { return coursesReused; }
    public void setCoursesReused(int coursesReused) { this.coursesReused = coursesReused; }
    public List<ImportRowError> getErrors() { return errors; }
    public void setErrors(List<ImportRowError> errors) { this.errors = errors; }

    public int getTotalProcessed() {
        return imported + duplicates + invalid;
    }
}
