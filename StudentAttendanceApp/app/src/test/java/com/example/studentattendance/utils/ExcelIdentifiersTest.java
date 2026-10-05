package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExcelIdentifiersTest {

    @Test
    public void stripsExcelWholeNumberDecimalSuffix() {
        assertEquals("123", ExcelIdentifiers.normalizeRollNumber("123.0"));
        assertEquals("123", ExcelIdentifiers.normalizeRollNumber(" 123.00 "));
        assertEquals("-5", ExcelIdentifiers.normalizeRollNumber("-5.0"));
    }

    @Test
    public void preservesIdentifierTextAndLeadingZeros() {
        assertEquals("00123", ExcelIdentifiers.normalizeRollNumber("00123"));
        assertEquals("BSIT-001", ExcelIdentifiers.normalizeRollNumber("BSIT-001"));
        assertEquals("23-IT-045", ExcelIdentifiers.normalizeRollNumber("23-IT-045"));
        assertEquals("123", ExcelIdentifiers.normalizeRollNumber("123"));
        assertEquals("123.5", ExcelIdentifiers.normalizeRollNumber("123.5"));
        assertEquals("", ExcelIdentifiers.normalizeRollNumber(null));
        assertEquals("", ExcelIdentifiers.normalizeRollNumber("   "));
    }
}
