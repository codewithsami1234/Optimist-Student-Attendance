package com.example.studentattendance.data.local;

/**
 * New accounts start with an empty catalog so professors build their own
 * departments, classes, and rosters. Existing Room data is never modified here.
 */
public final class AcademicSeeder {

    private AcademicSeeder() {
    }

    public static void seedIfEmpty(AppDatabase db, String ownerUserId) {
        if (db == null || ownerUserId == null || ownerUserId.isEmpty()) {
            return;
        }
    }
}
