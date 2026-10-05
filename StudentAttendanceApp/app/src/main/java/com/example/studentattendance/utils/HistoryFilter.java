package com.example.studentattendance.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.DepartmentShift;

/**
 * AND-filter for History sessions against real Room-backed session fields.
 * A null/"All" value means no restriction on that axis.
 */
public final class HistoryFilter {

    public String departmentId;
    public String semesterId;
    public String sectionId;
    public String subjectId;
    public String courseCode;
    public String fromDate;
    public String toDate;
    /** {@link DepartmentShift} code, or null for All. */
    public String shift;
    /** Department IDs that match {@link #shift}; empty when shift set but nothing matches. */
    private Set<String> shiftDepartmentIds;

    public HistoryFilter() {
    }

    public HistoryFilter(String departmentId, String semesterId, String sectionId,
                         String subjectId, String courseCode, String fromDate, String toDate) {
        this(departmentId, semesterId, sectionId, subjectId, courseCode, fromDate, toDate, null);
    }

    public HistoryFilter(String departmentId, String semesterId, String sectionId,
                         String subjectId, String courseCode, String fromDate, String toDate,
                         String shift) {
        this.departmentId = emptyToNull(departmentId);
        this.semesterId = emptyToNull(semesterId);
        this.sectionId = emptyToNull(sectionId);
        this.subjectId = emptyToNull(subjectId);
        this.courseCode = normalizeCode(courseCode);
        this.fromDate = emptyToNull(fromDate);
        this.toDate = emptyToNull(toDate);
        this.shift = normalizeShift(shift);
    }

    public boolean hasAny() {
        return departmentId != null || semesterId != null || sectionId != null
                || subjectId != null || courseCode != null || fromDate != null || toDate != null
                || shift != null;
    }

    /**
     * Resolves which department IDs belong to the selected shift using live
     * class setups. Call after setting {@link #shift}.
     */
    public void bindShiftDepartments(List<ClassSetup> setups) {
        if (shift == null) {
            shiftDepartmentIds = null;
            return;
        }
        Set<String> ids = new HashSet<>();
        if (setups != null) {
            for (ClassSetup setup : setups) {
                if (setup == null || setup.getDepartment() == null) {
                    continue;
                }
                Department department = setup.getDepartment();
                if (department.getId() == null) {
                    continue;
                }
                if (shift.equals(DepartmentShift.normalize(department.getShift()))) {
                    ids.add(department.getId());
                }
            }
        }
        shiftDepartmentIds = ids;
    }

    public boolean matches(AttendanceSession session) {
        if (session == null) {
            return false;
        }
        if (departmentId != null && !departmentId.equals(session.getDepartmentId())) {
            return false;
        }
        if (shift != null) {
            Set<String> allowed = shiftDepartmentIds == null
                    ? Collections.<String>emptySet() : shiftDepartmentIds;
            if (session.getDepartmentId() == null || !allowed.contains(session.getDepartmentId())) {
                return false;
            }
        }
        if (semesterId != null && !semesterId.equals(session.getSemesterId())) {
            return false;
        }
        if (sectionId != null && !sectionId.equals(session.getSectionId())) {
            return false;
        }
        if (subjectId != null && !subjectId.equals(session.getSubjectId())) {
            return false;
        }
        if (courseCode != null) {
            String code = normalizeCode(session.getSubjectCode());
            if (!courseCode.equals(code)) {
                return false;
            }
        }
        String date = session.getDate() == null ? "" : session.getDate();
        if (fromDate != null && date.compareTo(fromDate) < 0) {
            return false;
        }
        if (toDate != null && date.compareTo(toDate) > 0) {
            return false;
        }
        return true;
    }

    public static List<AttendanceSession> apply(List<AttendanceSession> sessions, HistoryFilter filter) {
        List<AttendanceSession> result = new ArrayList<>();
        if (sessions == null) {
            return result;
        }
        HistoryFilter safe = filter == null ? new HistoryFilter() : filter;
        for (AttendanceSession session : sessions) {
            if (safe.matches(session)) {
                result.add(session);
            }
        }
        return result;
    }

    public static List<AttendanceRecord> byStatus(List<AttendanceRecord> records, AttendanceStatus status) {
        List<AttendanceRecord> result = new ArrayList<>();
        if (records == null) {
            return result;
        }
        for (AttendanceRecord record : records) {
            if (record == null) {
                continue;
            }
            if (status == null || record.getStatus() == status) {
                result.add(record);
            }
        }
        return result;
    }

    public static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public static String normalizeCode(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().toUpperCase(Locale.US);
    }

    public static String normalizeShift(String value) {
        if (value == null || value.trim().isEmpty()
                || "all".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return DepartmentShift.normalize(value);
    }

    public static String sectionLabel(String name) {
        return name == null || name.trim().isEmpty() ? "No section" : name.trim();
    }

    public static String formatClassLine(AttendanceSession session) {
        if (session == null) {
            return "";
        }
        StringBuilder line = new StringBuilder();
        appendPart(line, session.getDepartmentName());
        appendPart(line, session.getSemesterName());
        appendPart(line, sectionLabel(session.getSectionName()));
        appendPart(line, session.getSubjectName());
        String code = session.getSubjectCode();
        if (code != null && !code.trim().isEmpty()) {
            appendPart(line, code.trim());
        }
        return line.toString();
    }

    private static void appendPart(StringBuilder line, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (line.length() > 0) {
            line.append(" · ");
        }
        line.append(value.trim());
    }
}
