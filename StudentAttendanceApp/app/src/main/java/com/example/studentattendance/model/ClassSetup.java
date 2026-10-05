package com.example.studentattendance.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * One saved academic class/course card: department + semester + optional
 * section + course. Not a Room entity — assembled from existing tables.
 */
public class ClassSetup implements Serializable {

    private Department department;
    private Semester semester;
    private Section section;
    private Subject subject;

    public ClassSetup() {
    }

    public ClassSetup(Department department, Semester semester, Section section, Subject subject) {
        this.department = department;
        this.semester = semester;
        this.section = section;
        this.subject = subject;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public boolean hasNamedSection() {
        return section != null && section.getName() != null && !section.getName().trim().isEmpty();
    }

    public String semesterLabel() {
        if (semester == null) {
            return "";
        }
        if (semester.getName() != null && !semester.getName().trim().isEmpty()) {
            return semester.getName().trim();
        }
        return "Semester " + semester.getSemesterNumber();
    }

    public String matchesQuery(String query) {
        StringBuilder haystack = new StringBuilder();
        if (department != null) {
            haystack.append(department.displayLabel()).append(' ');
            if (department.getName() != null) {
                haystack.append(department.getName()).append(' ');
            }
            haystack.append(DepartmentShift.label(department.getShift())).append(' ');
        }
        haystack.append(semesterLabel()).append(' ');
        if (hasNamedSection()) {
            haystack.append(section.getName()).append(' ');
        }
        if (subject != null) {
            if (subject.getName() != null) {
                haystack.append(subject.getName()).append(' ');
            }
            if (subject.getCode() != null) {
                haystack.append(subject.getCode());
            }
        }
        return haystack.toString().toLowerCase(Locale.getDefault());
    }
}
