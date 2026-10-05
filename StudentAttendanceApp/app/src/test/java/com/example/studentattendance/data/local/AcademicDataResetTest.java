package com.example.studentattendance.data.local;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AcademicDataResetTest {

    @Test
    public void resetRemovesAcademicRecordsAndKeepsAccount() {
        AcademicDataReset.MemoryGraph graph = new AcademicDataReset.MemoryGraph();
        graph.seedOwner("ownerA");
        graph.seedOwner("ownerB");

        AcademicDataReset.delete(graph, "ownerA");

        assertFalse(graph.hasOrphans("ownerA"));
        assertTrue(graph.accounts.contains("ownerA:account"));
        assertTrue(graph.accounts.contains("ownerB:account"));
        assertTrue(graph.departments.contains("ownerB:d1"));
        assertTrue(graph.students.contains("ownerB:st1"));
        assertTrue(graph.sessions.contains("ownerB:s1"));
        assertTrue(graph.records.contains("ownerB:r1"));
        assertEquals(2, graph.accounts.size());
    }

    @Test
    public void resetDoesNotTouchOtherOwner() {
        AcademicDataReset.MemoryGraph graph = new AcademicDataReset.MemoryGraph();
        graph.seedOwner("ownerA");
        graph.seedOwner("ownerB");
        AcademicDataReset.delete(graph, "ownerA");
        assertFalse(graph.hasOrphans("ownerA"));
        assertTrue(graph.hasOrphans("ownerB"));
        assertTrue(graph.notifications.contains("ownerB:n1"));
        assertTrue(graph.lectures.contains("ownerB:l1"));
        assertTrue(graph.adjustments.contains("ownerB:a1"));
        assertTrue(graph.subjects.contains("ownerB:sub1"));
        assertTrue(graph.sections.contains("ownerB:sec1"));
        assertTrue(graph.semesters.contains("ownerB:sem1"));
    }

    @Test
    public void missingOwnerFailsWithoutDeleting() {
        AcademicDataReset.MemoryGraph graph = new AcademicDataReset.MemoryGraph();
        graph.seedOwner("ownerA");
        try {
            AcademicDataReset.delete(graph, "");
            fail("expected failure");
        } catch (LocalDataException expected) {
            assertTrue(graph.departments.contains("ownerA:d1"));
            assertTrue(graph.accounts.contains("ownerA:account"));
        }
    }
}
