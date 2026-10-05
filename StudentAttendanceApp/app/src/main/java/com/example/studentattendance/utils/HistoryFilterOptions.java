package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;

/**
 * Builds History dropdown options from real class setups using IDs, not
 * display-name matching. Child lists shrink with the selected parent; "All"
 * is represented by a null id.
 */
public final class HistoryFilterOptions {

    public static final class Option {
        public final String id;
        public final String label;

        public Option(String id, String label) {
            this.id = id;
            this.label = label == null ? "" : label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final class Snapshot {
        public final List<Option> departments = new ArrayList<>();
        public final List<Option> semesters = new ArrayList<>();
        public final List<Option> sections = new ArrayList<>();
        public final List<Option> subjects = new ArrayList<>();
        public final List<Option> courseCodes = new ArrayList<>();
    }

    private HistoryFilterOptions() {
    }

    public static Snapshot from(List<ClassSetup> setups, String departmentId,
                                String semesterId, String sectionId) {
        Snapshot snapshot = new Snapshot();
        Map<String, Option> departments = new LinkedHashMap<>();
        Map<String, Option> semesters = new LinkedHashMap<>();
        Map<String, Option> sections = new LinkedHashMap<>();
        Map<String, Option> subjects = new LinkedHashMap<>();
        Map<String, Option> codes = new LinkedHashMap<>();
        if (setups == null) {
            return snapshot;
        }
        for (ClassSetup setup : setups) {
            if (setup == null) {
                continue;
            }
            Department department = setup.getDepartment();
            Semester semester = setup.getSemester();
            Section section = setup.getSection();
            Subject subject = setup.getSubject();
            if (department != null && department.getId() != null) {
                departments.putIfAbsent(department.getId(),
                        new Option(department.getId(), department.displayLabel()));
            }
            if (!matches(departmentId, department == null ? null : department.getId())) {
                continue;
            }
            if (semester != null && semester.getId() != null) {
                semesters.putIfAbsent(semester.getId(),
                        new Option(semester.getId(), setup.semesterLabel()));
            }
            if (!matches(semesterId, semester == null ? null : semester.getId())) {
                continue;
            }
            if (section != null && section.getId() != null) {
                sections.putIfAbsent(section.getId(),
                        new Option(section.getId(), HistoryFilter.sectionLabel(section.getName())));
            }
            if (!matches(sectionId, section == null ? null : section.getId())) {
                continue;
            }
            if (subject != null && subject.getId() != null) {
                subjects.putIfAbsent(subject.getId(),
                        new Option(subject.getId(), subject.toString()));
                String code = HistoryFilter.normalizeCode(subject.getCode());
                if (code != null) {
                    codes.putIfAbsent(code, new Option(code, code));
                }
            }
        }
        snapshot.departments.addAll(departments.values());
        snapshot.semesters.addAll(semesters.values());
        snapshot.sections.addAll(sections.values());
        snapshot.subjects.addAll(subjects.values());
        snapshot.courseCodes.addAll(codes.values());
        return snapshot;
    }

    public static String labelFor(List<Option> options, String id) {
        if (id == null || options == null) {
            return null;
        }
        for (Option option : options) {
            if (option != null && id.equals(option.id)) {
                return option.label;
            }
        }
        return null;
    }

    private static boolean matches(String selectedId, String candidateId) {
        return selectedId == null || selectedId.equals(candidateId);
    }
}
