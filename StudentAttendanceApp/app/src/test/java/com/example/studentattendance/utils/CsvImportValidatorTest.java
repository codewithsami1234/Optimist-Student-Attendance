package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CsvImportValidatorTest {

    @Test
    public void headerMustIncludeRollAndName() {
        assertNull(CsvImportValidator.validateHeader("Student Name,Roll Number"));
        assertNull(CsvImportValidator.validateHeader("RollNumber,Name,Email,Phone"));
        assertNotNull(CsvImportValidator.validateHeader("Email,Phone"));
        assertNotNull(CsvImportValidator.validateHeader(""));
    }

    @Test
    public void rowsRequireRollAndName() {
        assertNull(CsvImportValidator.validateRow(2, "CS-001", "Ahmed"));
        assertNotNull(CsvImportValidator.validateRow(3, "", "Ahmed"));
        assertNotNull(CsvImportValidator.validateRow(4, "CS-002", "  "));
    }
}
