package com.example.studentattendance.service;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.studentattendance.model.CsvParseResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvExcelServiceTest {

    @Test
    public void csvEscapesCommasAndQuotes() {
        assertEquals("RollNumber,Name", CsvExcelService.csvLine("RollNumber", "Name"));
        assertEquals("\"Khan, Ayesha\"", CsvExcelService.csvEscape("Khan, Ayesha"));
        assertEquals("\"He said \"\"hello\"\"\"", CsvExcelService.csvEscape("He said \"hello\""));
    }

    @Test
    public void csvIncludesNameAndRoll() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Student Name", "Roll Number"});
        rows.add(new String[]{"Ayesha Khan", "CS-001"});
        String csv = CsvExcelService.toCsv(rows);
        assertTrue(csv.contains("Student Name,Roll Number"));
        assertTrue(csv.contains("Ayesha Khan,CS-001"));
        assertTrue(csv.startsWith("\uFEFF"));
    }

    @Test
    public void parseNameThenRollHeader() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Student Name", "Roll Number"));
        rows.add(Arrays.asList("Ayesha Khan", "CS-001"));
        rows.add(Arrays.asList("", ""));
        rows.add(Arrays.asList("Ali Raza", "CS-001"));
        rows.add(Arrays.asList("No Roll", ""));
        CsvParseResult result = CsvExcelService.parseStudentRows(rows, "d", "sem", "sec");
        assertEquals(1, result.validStudents.size());
        assertEquals("Ayesha Khan", result.validStudents.get(0).getName());
        assertEquals("CS-001", result.validStudents.get(0).getRollNumber());
        assertEquals(1, result.duplicateCount);
        assertEquals(1, result.invalidCount);
    }

    @Test
    public void parseRollThenNameHeader() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("RollNumber", "Name"));
        rows.add(Arrays.asList("CS-002", "Sara Ahmed"));
        CsvParseResult result = CsvExcelService.parseStudentRows(rows, "d", "sem", "sec");
        assertEquals(1, result.validStudents.size());
        assertEquals("Sara Ahmed", result.validStudents.get(0).getName());
        assertEquals("CS-002", result.validStudents.get(0).getRollNumber());
    }

    @Test
    public void parsePreservesRollNumbersWithoutDecimalSuffix() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Student Roster"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Student Name", "Roll Number"));
        rows.add(Arrays.asList("Ali", "00123"));
        rows.add(Arrays.asList("Ahmed", "BSIT-001"));
        rows.add(Arrays.asList("Sara", "23-IT-045"));
        rows.add(Arrays.asList("Usman", "123.0"));
        rows.add(Arrays.asList("Zain", "456"));
        CsvParseResult result = CsvExcelService.parseStudentRows(rows, "d", "sem", "sec");
        assertEquals(5, result.validStudents.size());
        assertEquals("00123", result.validStudents.get(0).getRollNumber());
        assertEquals("BSIT-001", result.validStudents.get(1).getRollNumber());
        assertEquals("23-IT-045", result.validStudents.get(2).getRollNumber());
        assertEquals("123", result.validStudents.get(3).getRollNumber());
        assertEquals("456", result.validStudents.get(4).getRollNumber());
    }
}
