package com.example.studentattendance.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LectureTest {

    @Test
    public void displayLabelUsesNumberWhenTitleMissing() {
        Lecture lecture = new Lecture("lec_1", "sec_1", "subj_1", 3, "");
        assertEquals("Lecture 3", lecture.displayLabel());
    }

    @Test
    public void displayLabelIncludesOptionalTitle() {
        Lecture lecture = new Lecture("lec_1", "sec_1", "subj_1", 2, "Intro to OOP");
        assertEquals("Lecture 2 · Intro to OOP", lecture.displayLabel());
    }

    @Test
    public void lectureNumberDefaultsToOne() {
        Lecture lecture = new Lecture();
        lecture.setLectureNumber(0);
        assertEquals(1, lecture.getLectureNumber());
    }
}
