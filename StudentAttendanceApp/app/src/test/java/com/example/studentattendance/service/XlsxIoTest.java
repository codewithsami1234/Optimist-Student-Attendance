package com.example.studentattendance.service;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XlsxIoTest {

    @Test
    public void roundTripPreservesCells() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"RollNumber", "Name"});
        rows.add(new String[]{"CS-001", "Ayesha Khan"});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XlsxIo.write(out, java.util.Collections.singletonList(new XlsxIo.Sheet("Students", rows)));
        byte[] bytes = out.toByteArray();
        assertTrue(XlsxIo.looksLikeZip(bytes));
        List<List<String>> parsed = XlsxIo.readFirstSheet(bytes);
        assertEquals(2, parsed.size());
        assertEquals("RollNumber", parsed.get(0).get(0));
        assertEquals("Ayesha Khan", parsed.get(1).get(1));
    }

    @Test
    public void inlineTextRollsKeepLeadingZeros() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Roll No", "Student Name"});
        rows.add(new String[]{"001", "Ali"});
        rows.add(new String[]{"00027", "Ahmed"});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XlsxIo.write(out, java.util.Collections.singletonList(new XlsxIo.Sheet("Students", rows)));
        List<List<String>> parsed = XlsxIo.readFirstSheet(out.toByteArray());
        assertEquals("001", parsed.get(1).get(0));
        assertEquals("00027", parsed.get(2).get(0));
    }

    @Test
    public void zeroPaddedNumericFormatKeepsLeadingZeros() throws Exception {
        String sheet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>"
                + "<row r=\"1\">"
                + "<c r=\"A1\" t=\"inlineStr\"><is><t>Roll No</t></is></c>"
                + "</row>"
                + "<row r=\"2\">"
                + "<c r=\"A2\" s=\"1\"><v>1</v></c>"
                + "</row>"
                + "<row r=\"3\">"
                + "<c r=\"A3\" s=\"2\"><v>27</v></c>"
                + "</row>"
                + "</sheetData></worksheet>";
        String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"2\">"
                + "<numFmt numFmtId=\"164\" formatCode=\"000\"/>"
                + "<numFmt numFmtId=\"165\" formatCode=\"00000\"/>"
                + "</numFmts>"
                + "<cellXfs count=\"3\">"
                + "<xf numFmtId=\"0\"/>"
                + "<xf numFmtId=\"164\"/>"
                + "<xf numFmtId=\"165\"/>"
                + "</cellXfs>"
                + "</styleSheet>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            put(zip, "xl/styles.xml", styles);
            put(zip, "xl/worksheets/sheet1.xml", sheet);
        }
        List<List<String>> parsed = XlsxIo.readFirstSheet(out.toByteArray());
        assertEquals("001", parsed.get(1).get(0));
        assertEquals("00027", parsed.get(2).get(0));
    }

    @Test
    public void leadingZeroFormatHelper() {
        assertEquals("001", XlsxIo.applyLeadingZeroFormat("1", "000"));
        assertEquals("00027", XlsxIo.applyLeadingZeroFormat("27.0", "00000"));
        assertEquals("001", XlsxIo.applyLeadingZeroFormat("001", null));
        assertEquals("123", XlsxIo.applyLeadingZeroFormat("123.0", "0"));
    }

    @Test
    public void richTextSharedStringsStayOneValueAndRelsAreNotTheSheet() throws Exception {
        String shared = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<si><r><t>Department</t></r><r><t xml:space=\"preserve\"> Name</t></r></si>"
                + "<si><t>001</t></si>"
                + "</sst>";
        String relsNoise = "<?xml version=\"1.0\"?><Relationships>"
                + "<Relationship Target=\"../printerSettings/printerSettings1.bin\"/>"
                + "</Relationships>";
        String sheet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>"
                + "<row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c><c r=\"C1\"/></row>"
                + "<row r=\"2\"><c r=\"A2\" t=\"s\"><v>1</v></c></row>"
                + "</sheetData></worksheet>";
        String workbook = "<?xml version=\"1.0\"?>"
                + "<workbook xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Students\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
        String workbookRels = "<?xml version=\"1.0\"?>"
                + "<Relationships>"
                + "<Relationship Id=\"rId1\" Type=\"worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            put(zip, "xl/worksheets/_rels/sheet1.xml.rels", relsNoise);
            put(zip, "xl/sharedStrings.xml", shared);
            put(zip, "xl/workbook.xml", workbook);
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels);
            put(zip, "xl/worksheets/sheet1.xml", sheet);
        }
        List<List<String>> parsed = XlsxIo.readFirstSheet(out.toByteArray());
        assertEquals("Department Name", parsed.get(0).get(0));
        assertEquals("", parsed.get(0).get(1));
        assertEquals("001", parsed.get(1).get(0));
    }

    private static void put(ZipOutputStream zip, String name, String xml) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(xml.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    @Test
    public void columnNames() {
        assertEquals("A", XlsxIo.colName(0));
        assertEquals("Z", XlsxIo.colName(25));
        assertEquals("AA", XlsxIo.colName(26));
    }
}
