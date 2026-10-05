package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * A teacher-created lecture for a section and course. Attendance sessions
 * still store {@code lectureNumber}; this catalog is what the teacher adds,
 * selects, and deletes before marking.
 */
public class Lecture implements Serializable {

    private String id;
    private String sectionId;
    private String subjectId;
    private int lectureNumber;
    private String title;

    public Lecture() {
    }

    public Lecture(String id, String sectionId, String subjectId, int lectureNumber, String title) {
        this.id = id;
        this.sectionId = sectionId;
        this.subjectId = subjectId;
        this.lectureNumber = lectureNumber;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public int getLectureNumber() {
        return lectureNumber < 1 ? 1 : lectureNumber;
    }

    public void setLectureNumber(int lectureNumber) {
        this.lectureNumber = lectureNumber < 1 ? 1 : lectureNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String displayLabel() {
        if (title != null && !title.trim().isEmpty()) {
            return "Lecture " + getLectureNumber() + " · " + title.trim();
        }
        return "Lecture " + getLectureNumber();
    }

    @Override
    public String toString() {
        return displayLabel();
    }
}
