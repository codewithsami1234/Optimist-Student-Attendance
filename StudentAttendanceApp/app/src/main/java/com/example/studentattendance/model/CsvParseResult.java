package com.example.studentattendance.model;

import java.util.ArrayList;
import java.util.List;

/** Client-side CSV parse result shown in the import preview dialog. */
public class CsvParseResult {
    public final List<Student> validStudents = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();
    public int duplicateCount;
    public int invalidCount;
    public String headerLine;
}
