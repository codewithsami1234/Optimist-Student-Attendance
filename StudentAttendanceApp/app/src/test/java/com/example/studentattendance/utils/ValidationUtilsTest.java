package com.example.studentattendance.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationUtilsTest {

    @Test
    public void validEmailRequiresDomainAndTld() {
        assertTrue(ValidationUtils.isValidEmail("professor@university.edu"));
        assertTrue(ValidationUtils.isValidEmail("first.last+tag@dept.ac.uk"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("not-an-email"));
        assertFalse(ValidationUtils.isValidEmail("missing-domain@"));
        assertFalse(ValidationUtils.isValidEmail("a@b"));
        assertFalse(ValidationUtils.isValidEmail("bad..dots@school.edu"));
    }

    @Test
    public void newPasswordRequiresLetterAndNumber() {
        assertTrue(ValidationUtils.isValidNewPassword("password123"));
        assertFalse(ValidationUtils.isValidNewPassword("short1"));
        assertFalse(ValidationUtils.isValidNewPassword("lettersonly"));
        assertFalse(ValidationUtils.isValidNewPassword("12345678"));
    }
}
