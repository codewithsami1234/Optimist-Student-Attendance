package com.example.studentattendance.data.local;

import org.junit.Test;

public class AcademicSeederTest {

    @Test
    public void seedIfEmptyDoesNotThrow() {
        AcademicSeeder.seedIfEmpty(null, null);
        AcademicSeeder.seedIfEmpty(null, "user_1");
    }
}
