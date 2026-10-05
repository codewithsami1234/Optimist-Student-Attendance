package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.studentattendance.model.AttendanceStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttendanceImportParserTest {

    @Test
    public void validWorkbookRowsAreAccepted() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Student Name", "Roll Number", "Date", "Status", "Course Code"));
        rows.add(Arrays.asList("Ayesha Khan", "CS-001", "2026-01-10", "Present", "CS-401"));
        AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
        assertEquals(1, result.validRows.size());
        assertEquals("CS-001", result.validRows.get(0).rollNumber);
        assertEquals(AttendanceStatus.PRESENT, result.validRows.get(0).status);
        assertEquals("CS-401", result.validRows.get(0).courseCode);
    }

    @Test
    public void missingRequiredColumnsAreRejected() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Student Name", "Email"));
        rows.add(Arrays.asList("Ayesha", "a@x.com"));
        AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
        assertTrue(result.validRows.isEmpty());
        assertTrue(result.invalidCount >= 1);
        assertTrue(result.errors.get(0).toLowerCase().contains("missing required columns"));
    }

    @Test
    public void emptyRowsAreSkipped() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Roll Number", "Date", "Status"));
        rows.add(Arrays.asList("", "", ""));
        rows.add(Arrays.asList("CS-001", "2026-01-10", "Absent"));
        AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
        assertEquals(1, result.emptyCount);
        assertEquals(1, result.validRows.size());
    }

    @Test
    public void duplicateRollAndDateAreSkipped() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Roll Number", "Date", "Status"));
        rows.add(Arrays.asList("CS-001", "2026-01-10", "Present"));
        rows.add(Arrays.asList("CS-001", "2026-01-10", "Absent"));
        AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
        assertEquals(1, result.validRows.size());
        assertEquals(1, result.duplicateCount);
    }

    @Test
    public void invalidStatusAndDateAreRejected() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Roll Number", "Date", "Status"));
        rows.add(Arrays.asList("CS-001", "10/01/2026", "Present"));
        rows.add(Arrays.asList("CS-002", "2026-01-10", "Maybe"));
        AttendanceImportParser.Result result = AttendanceImportParser.parse(rows);
        assertTrue(result.validRows.isEmpty());
        assertEquals(2, result.invalidCount);
    }

    @Test
    public void emptyWorkbookIsInvalid() {
        AttendanceImportParser.Result result = AttendanceImportParser.parse(new ArrayList<>());
        assertTrue(result.validRows.isEmpty());
        assertTrue(result.invalidCount >= 1);
    }
}
