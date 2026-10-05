package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.studentattendance.model.DepartmentShift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AcademicExcelParserTest {

    @Test
    public void repeatedStructureStaysOneSetOfStudentsWithTextRolls() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Class list"));
        rows.add(Arrays.asList(
                "Department Name", "Shift", "Semester", "Section",
                "Course/Subject Name", "Course Code", "Student Name", "Roll No"));
        rows.add(Arrays.asList(
                "BS Information Technology", "Morning", "6", "A",
                "Database Systems", "IT-301", "Ali Raza", "001"));
        rows.add(Arrays.asList(
                "BS Information Technology", "Morning", "6", "A",
                "Database Systems", "IT-301", "Ahmed", "002"));
        rows.add(Arrays.asList(
                "BS Information Technology", "Morning", "Semester 6", "A",
                "Database Systems", "it-301", "Sara", "00027"));
        rows.add(Arrays.asList("", "", "", "", "", "", "", ""));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(3, result.totalRows);
        assertEquals(3, result.validRows);
        assertEquals(0, result.invalidRows);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("002", result.rows.get(1).rollNumber);
        assertEquals("00027", result.rows.get(2).rollNumber);
        assertEquals("Ali Raza", result.rows.get(0).studentName);
        assertEquals(DepartmentShift.MORNING, result.rows.get(0).shift);
        assertEquals(6, result.rows.get(0).semesterNumber);
        assertEquals(6, result.rows.get(2).semesterNumber);
        assertEquals("IT-301", result.rows.get(2).courseCode);
        assertEquals("Database Systems", result.rows.get(0).courseName);
        assertEquals("A", result.rows.get(0).sectionName);
    }

    @Test
    public void missingColumnIsAFileError() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Department Name", "Shift", "Semester", "Section", "Student Name", "Roll No"));
        rows.add(Arrays.asList("BS IT", "Morning", "6", "A", "Ali", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNotNull(result.fatalError);
        assertTrue(result.fatalError.contains("Missing required columns:"));
        assertTrue(result.fatalError.contains("Course/Subject Name"));
        assertTrue(result.fatalError.contains("Course Code"));
        assertTrue(result.rows.isEmpty());
    }

    @Test
    public void invalidRowsDoNotRejectTheRest() {
        List<List<String>> rows = header();
        rows.add(Arrays.asList("BS IT", "Afternoon", "6", "A", "Database Systems", "IT-301", "Ali", "001"));
        rows.add(Arrays.asList("BS IT", "Evening", "9", "A", "Database Systems", "IT-301", "Ahmed", "002"));
        rows.add(Arrays.asList("BS IT", "Evening", "6", "B", "Networks", "IT-302", "Sara", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(3, result.totalRows);
        assertEquals(2, result.validRows);
        assertEquals(1, result.invalidRows);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals(DepartmentShift.MORNING, result.rows.get(0).shift);
        assertEquals("001", result.rows.get(1).rollNumber);
        assertEquals("B", result.rows.get(1).sectionName);
        assertEquals(DepartmentShift.EVENING, result.rows.get(1).shift);
        assertTrue(result.errors.get(0).toString().contains("1 to 8"));
    }

    @Test
    public void shortHeadersAndAMissingRollStaySpecific() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                " Department ", "Shift", "Semester", "Section", "Course", "Code", "Student", "Roll No"));
        rows.add(Arrays.asList(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT-301", "Ali Raza", "001"));
        rows.add(Arrays.asList(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT-301", "Ahmed Khan", "002"));
        rows.add(Arrays.asList(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT-301", "", "00027"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(3, result.totalRows);
        assertEquals(2, result.validRows);
        assertEquals(1, result.invalidRows);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("Database Systems", result.rows.get(0).courseName);
        assertEquals("IT-301", result.rows.get(0).courseCode);
        assertTrue(result.errors.get(0).toString().contains("Student Name is missing."));
    }

    @Test
    public void extraColumnsAreIgnoredAndRequiredColumnsCanMove() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Student ID", "Department", "Phone", "Shift", "Semester", "Section",
                "Course", "Code", "Student", "Roll No", "Email", "Father Name",
                "Address", "Gender", "CNIC"));
        rows.add(Arrays.asList(
                "1001", "BS IT", "03001234567", "Morning", "6", "A",
                "Database", "IT301", "Ali", "001", "ali@example.com", "Khan",
                "Street 1", "Male", "12345"));
        rows.add(Arrays.asList(
                "1002", "BS IT", "03007654321", "Morning", "6", "A",
                "Database", "IT301", "Ahmed", "00027", "ahmed@example.com", "Khan",
                "Street 2", "Male", "67890"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals(0, result.invalidRows);
        assertEquals(0, result.errors.size());
        AcademicExcelParser.Row ali = result.rows.get(0);
        assertEquals("BS IT", ali.departmentName);
        assertEquals(DepartmentShift.MORNING, ali.shift);
        assertEquals(6, ali.semesterNumber);
        assertEquals("A", ali.sectionName);
        assertEquals("Database", ali.courseName);
        assertEquals("IT301", ali.courseCode);
        assertEquals("Ali", ali.studentName);
        assertEquals("001", ali.rollNumber);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals("Ahmed", result.rows.get(1).studentName);
    }

    @Test
    public void extraColumnsBeforeRequiredColumnsStillMapByHeader() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Father's Name", "Mobile", "Date of Birth",
                "Roll Number", "Student Name", "Course Code", "Course/Subject Name",
                "Section", "Semester", "Shift", "Department Name"));
        rows.add(Arrays.asList(
                "Khan", "0300", "2004-01-01",
                "001", "Ali Raza", "IT-301", "Database Systems",
                "A", "6", "Morning", "BS IT"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(1, result.validRows);
        AcademicExcelParser.Row row = result.rows.get(0);
        assertEquals("BS IT", row.departmentName);
        assertEquals("Database Systems", row.courseName);
        assertEquals("IT-301", row.courseCode);
        assertEquals("Ali Raza", row.studentName);
        assertEquals("001", row.rollNumber);
    }

    @Test
    public void missingCourseCodeStopsTheImport() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Department Name", "Shift", "Semester", "Section",
                "Course/Subject Name", "Student Name", "Roll No", "Phone"));
        rows.add(Arrays.asList("BS IT", "Morning", "6", "A", "Database Systems", "Ali", "001", "0300"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNotNull(result.fatalError);
        assertEquals("Missing required column: Course Code", result.fatalError);
        assertTrue(result.rows.isEmpty());
    }

    @Test
    public void headerOnlyFileIsRejected() {
        AcademicExcelParser.Result result = AcademicExcelParser.parse(header());
        assertNotNull(result.fatalError);
        assertTrue(result.fatalError.contains("no student rows"));
        assertTrue(result.rows.isEmpty());
    }

    @Test
    public void numericExcelRollArtifactKeepsLeadingZerosWhenAlreadyText() {
        List<List<String>> rows = header();
        rows.add(Arrays.asList("BS IT", "Morning", "6.0", "A", "Database Systems", "IT-301", "Ali", "001.0"));
        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);
        assertEquals(1, result.validRows);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals(6, result.rows.get(0).semesterNumber);
    }

    @Test
    public void optionalShiftAndSectionMayBeAbsentOrBlank() {
        List<List<String>> absent = new ArrayList<>();
        absent.add(Arrays.asList(
                "Department Name", "Semester", "Course/Subject Name", "Course Code",
                "Student Name", "Roll No"));
        absent.add(Arrays.asList("BS IT", "6", "Database Systems", "IT-301", "Ali", "001"));

        AcademicExcelParser.Result withoutColumns = AcademicExcelParser.parse(absent);
        assertNull(withoutColumns.fatalError);
        assertEquals(1, withoutColumns.validRows);
        assertEquals(DepartmentShift.MORNING, withoutColumns.rows.get(0).shift);
        assertEquals("", withoutColumns.rows.get(0).sectionName);
        assertEquals("001", withoutColumns.rows.get(0).rollNumber);

        List<List<String>> blank = header();
        blank.add(Arrays.asList(
                "BS IT", "", "6", "", "Database Systems", "IT-301", "Ahmed", "00027"));
        AcademicExcelParser.Result blankCells = AcademicExcelParser.parse(blank);
        assertNull(blankCells.fatalError);
        assertEquals(1, blankCells.validRows);
        assertEquals(DepartmentShift.MORNING, blankCells.rows.get(0).shift);
        assertEquals("", blankCells.rows.get(0).sectionName);
        assertEquals("00027", blankCells.rows.get(0).rollNumber);
    }

    @Test
    public void blankRequiredDepartmentIsARowError() {
        List<List<String>> rows = header();
        rows.add(Arrays.asList("", "Morning", "6", "A", "Database Systems", "IT-301", "Ali", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(0, result.validRows);
        assertEquals(1, result.invalidRows);
        assertTrue(result.errors.get(0).toString().contains("Department Name is missing."));
    }

    @Test
    public void missingDepartmentHeaderStopsTheImport() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Shift", "Semester", "Section", "Course/Subject Name", "Course Code",
                "Student Name", "Roll No"));
        rows.add(Arrays.asList("Morning", "6", "A", "Database Systems", "IT-301", "Ali", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertEquals("Missing required column: Department Name", result.fatalError);
        assertTrue(result.rows.isEmpty());
    }

    @Test
    public void rowWiseOptionalShiftAndSectionUseTheSameDefaults() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Class list"));
        rows.add(Arrays.asList("Department Name", "BS IT"));
        rows.add(Arrays.asList("Semester", "6"));
        rows.add(Arrays.asList("Course/Subject Name", "Database Systems"));
        rows.add(Arrays.asList("Course Code", "IT301"));
        rows.add(Arrays.asList("Student Name", "Ali"));
        rows.add(Arrays.asList("Roll No", "001"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Student Name", "Ahmed"));
        rows.add(Arrays.asList("Roll No", "00027"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals(DepartmentShift.MORNING, result.rows.get(0).shift);
        assertEquals("", result.rows.get(0).sectionName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals(result.rows.get(0).courseCode, result.rows.get(1).courseCode);
        assertEquals("IT301", result.rows.get(1).courseCode);
    }

    @Test
    public void rowWiseSingleStudentKeepsTextRoll() {
        AcademicExcelParser.Result result = AcademicExcelParser.parse(rowWiseBlock(
                "BS Information Technology", "Morning", "6", "A",
                "Database Systems", "IT301", "Ali", "001"));

        assertNull(result.fatalError);
        assertEquals(1, result.validRows);
        assertEquals("BS Information Technology", result.rows.get(0).departmentName);
        assertEquals(DepartmentShift.MORNING, result.rows.get(0).shift);
        assertEquals(6, result.rows.get(0).semesterNumber);
        assertEquals("A", result.rows.get(0).sectionName);
        assertEquals("Database Systems", result.rows.get(0).courseName);
        assertEquals("IT301", result.rows.get(0).courseCode);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
    }

    @Test
    public void rowWiseRepeatedStudentsShareOneAcademicStructure() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Attendance Report"));
        rows.add(Arrays.asList(""));
        rows.addAll(rowWiseBlock(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301", "Ali", "001"));
        rows.add(Arrays.asList(""));
        rows.addAll(rowWiseBlock(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301", "Ahmed", "00027"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals(0, result.invalidRows);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("Ahmed", result.rows.get(1).studentName);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals(result.rows.get(0).departmentName, result.rows.get(1).departmentName);
        assertEquals(result.rows.get(0).shift, result.rows.get(1).shift);
        assertEquals(result.rows.get(0).semesterNumber, result.rows.get(1).semesterNumber);
        assertEquals(result.rows.get(0).sectionName, result.rows.get(1).sectionName);
        assertEquals(result.rows.get(0).courseName, result.rows.get(1).courseName);
        assertEquals(result.rows.get(0).courseCode, result.rows.get(1).courseCode);
    }

    @Test
    public void rowWiseExtraFieldsAreIgnored() {
        List<List<String>> rows = rowWiseBlock(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301", "Ali", "001");
        rows.add(2, Arrays.asList("Father Name", "Khan"));
        rows.add(3, Arrays.asList("Phone", "03001234567"));
        rows.add(4, Arrays.asList("Email", "ali@example.com"));
        rows.add(Arrays.asList("Gender", "Male"));
        rows.add(Arrays.asList("CNIC", "12345"));
        rows.add(Arrays.asList("Batch", "2026"));
        rows.add(Arrays.asList("Remarks", "none"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(1, result.validRows);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("IT301", result.rows.get(0).courseCode);
    }

    @Test
    public void rowWiseMissingCourseCodeIsAFileError() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Department Name", "BS IT"));
        rows.add(Arrays.asList("Shift", "Morning"));
        rows.add(Arrays.asList("Semester", "6"));
        rows.add(Arrays.asList("Section", "A"));
        rows.add(Arrays.asList("Course/Subject Name", "Database Systems"));
        rows.add(Arrays.asList("Student Name", "Ali"));
        rows.add(Arrays.asList("Roll No", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertEquals("Missing required field: Course Code", result.fatalError);
        assertTrue(result.rows.isEmpty());
    }

    @Test
    public void parsingTheSameSheetTwiceProducesTheSameRoster() {
        List<List<String>> rows = rowWiseBlock(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT-301", "Sara", "00027");
        AcademicExcelParser.Result first = AcademicExcelParser.parse(rows);
        AcademicExcelParser.Result second = AcademicExcelParser.parse(rows);

        assertNull(first.fatalError);
        assertNull(second.fatalError);
        assertEquals(first.rows.get(0).departmentName, second.rows.get(0).departmentName);
        assertEquals(first.rows.get(0).courseCode, second.rows.get(0).courseCode);
        assertEquals("00027", first.rows.get(0).rollNumber);
        assertEquals(first.rows.get(0).rollNumber, second.rows.get(0).rollNumber);
        assertEquals(first.rows.get(0).sectionName, second.rows.get(0).sectionName);
    }

    @Test
    public void hybridMetadataThenStudentTableImportsOneClass() {
        List<List<String>> rows = hybridClass(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301");
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Ali", "001"));
        rows.add(Arrays.asList("Ahmed", "00027"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals(0, result.invalidRows);
        assertEquals("BS IT", result.rows.get(0).departmentName);
        assertEquals(DepartmentShift.MORNING, result.rows.get(0).shift);
        assertEquals(6, result.rows.get(0).semesterNumber);
        assertEquals("A", result.rows.get(0).sectionName);
        assertEquals("Database Systems", result.rows.get(0).courseName);
        assertEquals("IT301", result.rows.get(0).courseCode);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("Ahmed", result.rows.get(1).studentName);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals(result.rows.get(0).courseCode, result.rows.get(1).courseCode);
        assertEquals(result.rows.get(0).departmentName, result.rows.get(1).departmentName);
    }

    @Test
    public void hybridStudentTableIgnoresExtraColumnsWhereverTheySit() {
        List<List<String>> rows = hybridClass(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301");
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList(
                "Phone", "Roll Number", "Father Name", "Name", "Email", "Address"));
        rows.add(Arrays.asList(
                "03001234567", "001", "Ahmed Khan", "Ali", "ali@example.com", "Street 1"));
        rows.add(Arrays.asList(
                "03007654321", "00027", "Imran Khan", "Ahmed", "ahmed@example.com", "Street 2"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals(0, result.errors.size());
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("Ahmed", result.rows.get(1).studentName);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals("IT301", result.rows.get(0).courseCode);
        assertEquals("IT301", result.rows.get(1).courseCode);
    }

    @Test
    public void hybridBlankRowsAndTitlesAreIgnored() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Class Attendance"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Department Name", "BS IT"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Shift", "Morning"));
        rows.add(Arrays.asList("Semester", "6"));
        rows.add(Arrays.asList("Section", "A"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Course/Subject Name", "Database Systems"));
        rows.add(Arrays.asList("Course Code", "IT301"));
        rows.add(Arrays.asList("Student Roster"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("", "Student Name", "Roll No"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("", "Ali", "001"));
        rows.add(Arrays.asList("", "Ahmed", "00027"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(2, result.validRows);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals("Database Systems", result.rows.get(1).courseName);
    }

    @Test
    public void hybridMetadataAfterATitleStillImports() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("BS Information Technology — Spring"));
        rows.add(Arrays.asList(""));
        rows.addAll(hybridClass(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301"));
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Usman", "00031"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(1, result.validRows);
        assertEquals("Usman", result.rows.get(0).studentName);
        assertEquals("00031", result.rows.get(0).rollNumber);
        assertEquals(6, result.rows.get(0).semesterNumber);
        assertEquals("A", result.rows.get(0).sectionName);
    }

    @Test
    public void hybridTwoCourseBlocksKeepTheirOwnStudents() {
        List<List<String>> rows = hybridClass(
                "BS IT", "Morning", "6", "A", "Database Systems", "IT301");
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Ali", "001"));
        rows.add(Arrays.asList("Ahmed", "00027"));
        rows.add(Arrays.asList(""));
        rows.add(Arrays.asList("Course/Subject Name", "Networks"));
        rows.add(Arrays.asList("Course Code", "IT302"));
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Usman", "00031"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(3, result.validRows);
        assertEquals("Ali", result.rows.get(0).studentName);
        assertEquals("IT301", result.rows.get(0).courseCode);
        assertEquals("Database Systems", result.rows.get(0).courseName);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("Ahmed", result.rows.get(1).studentName);
        assertEquals("IT301", result.rows.get(1).courseCode);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals("Usman", result.rows.get(2).studentName);
        assertEquals("Networks", result.rows.get(2).courseName);
        assertEquals("IT302", result.rows.get(2).courseCode);
        assertEquals("00031", result.rows.get(2).rollNumber);
        assertEquals("BS IT", result.rows.get(2).departmentName);
        assertEquals("A", result.rows.get(2).sectionName);
    }

    @Test
    public void hybridMissingDepartmentIsAFieldError() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Shift", "Morning"));
        rows.add(Arrays.asList("Semester", "6"));
        rows.add(Arrays.asList("Section", "A"));
        rows.add(Arrays.asList("Course/Subject Name", "Database Systems"));
        rows.add(Arrays.asList("Course Code", "IT301"));
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Ali", "001"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertEquals("Missing required field: Department Name", result.fatalError);
        assertTrue(result.rows.isEmpty());
        assertTrue(!result.fatalError.contains("column"));
    }

    @Test
    public void hybridMetadataPairsOnOneRowFeedTheStudentTable() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Department Name", "BS IT", "Shift", "Morning", "Semester", "6",
                "Section", "A", "Course/Subject Name", "Database Systems", "Course Code", "IT301"));
        rows.add(Arrays.asList("Student Name", "Roll No"));
        rows.add(Arrays.asList("Ali", "001"));
        rows.add(Arrays.asList("Ahmed", "00027"));
        rows.add(Arrays.asList("Usman", "00031"));

        AcademicExcelParser.Result result = AcademicExcelParser.parse(rows);

        assertNull(result.fatalError);
        assertEquals(3, result.validRows);
        assertEquals("BS IT", result.rows.get(0).departmentName);
        assertEquals(DepartmentShift.MORNING, result.rows.get(2).shift);
        assertEquals(6, result.rows.get(2).semesterNumber);
        assertEquals("A", result.rows.get(1).sectionName);
        assertEquals("Database Systems", result.rows.get(2).courseName);
        assertEquals("IT301", result.rows.get(0).courseCode);
        assertEquals("001", result.rows.get(0).rollNumber);
        assertEquals("00027", result.rows.get(1).rollNumber);
        assertEquals("00031", result.rows.get(2).rollNumber);
    }

    private static List<List<String>> hybridClass(String department, String shift, String semester,
                                                   String section, String course, String code) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Department Name", department));
        rows.add(Arrays.asList("Shift", shift));
        rows.add(Arrays.asList("Semester", semester));
        rows.add(Arrays.asList("Section", section));
        rows.add(Arrays.asList("Course/Subject Name", course));
        rows.add(Arrays.asList("Course Code", code));
        return rows;
    }

    private static List<List<String>> rowWiseBlock(String department, String shift, String semester,
                                                    String section, String course, String code,
                                                    String student, String roll) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Department Name", department));
        rows.add(Arrays.asList("Shift", shift));
        rows.add(Arrays.asList("Semester", semester));
        rows.add(Arrays.asList("Section", section));
        rows.add(Arrays.asList("Course/Subject Name", course));
        rows.add(Arrays.asList("Course Code", code));
        rows.add(Arrays.asList("Student Name", student));
        rows.add(Arrays.asList("Roll No", roll));
        return rows;
    }

    private static List<List<String>> header() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "Department Name", "Shift", "Semester", "Section",
                "Course/Subject Name", "Course Code", "Student Name", "Roll No"));
        return rows;
    }
}
