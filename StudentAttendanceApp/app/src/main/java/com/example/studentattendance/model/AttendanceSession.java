package com.example.studentattendance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a saved attendance-taking session: a specific department + semester
 * + section + date, and the list of individual student records within it.
 * Used to power the Attendance History screen.
 */
public class AttendanceSession implements Serializable {

    private String id;
    private String departmentId;
    private String departmentName;
    private String semesterId;
    private String semesterName;
    private String sectionId;
    private String sectionName;
    private String subjectId;
    private String subjectName;
    private String subjectCode;
    private String date; // yyyy-MM-dd
    private int lectureNumber = 1;
    private List<AttendanceRecord> records = new ArrayList<>();
    private long createdAtMillis;

    public AttendanceSession() {
    }

    public AttendanceSession(String departmentId, String departmentName, String semesterId,
                              String semesterName, String sectionId, String sectionName, String date) {
        this.id = departmentId + "_" + semesterId + "_" + sectionId + "_" + date;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semesterId = semesterId;
        this.semesterName = semesterName;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.date = date;
        this.createdAtMillis = System.currentTimeMillis();
    }

    public int getPresentCount() {
        return countByStatus(AttendanceStatus.PRESENT);
    }

    public int getAbsentCount() {
        return countByStatus(AttendanceStatus.ABSENT);
    }

    public int getLateCount() {
        return countByStatus(AttendanceStatus.LATE);
    }

    public int getLeaveCount() {
        return countByStatus(AttendanceStatus.LEAVE);
    }

    private int countByStatus(AttendanceStatus status) {
        int count = 0;
        for (AttendanceRecord r : records) {
            if (r.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return records.size();
    }

    public double getAttendanceRate() {
        int present = getPresentCount();
        int absent = getAbsentCount();
        int late = getLateCount();
        int leave = getLeaveCount();
        return com.example.studentattendance.utils.AttendancePolicy.rate(present, absent, late, leave, 0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLectureNumber() {
        return lectureNumber < 1 ? 1 : lectureNumber;
    }

    public void setLectureNumber(int lectureNumber) {
        this.lectureNumber = lectureNumber < 1 ? 1 : lectureNumber;
    }

    public String getLectureLabel() {
        return "Lecture " + getLectureNumber();
    }

    public List<AttendanceRecord> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecord> records) {
        this.records = records;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }
}
