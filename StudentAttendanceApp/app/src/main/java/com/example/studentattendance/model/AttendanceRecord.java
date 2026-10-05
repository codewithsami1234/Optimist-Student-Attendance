package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * Represents the attendance status of a single student for a single date.
 */
public class AttendanceRecord implements Serializable {

    private String id;
    private String studentId;
    private String studentName;
    private String rollNumber;
    private String sectionId;
    private String semesterId;
    private String departmentId;
    private String date; // yyyy-MM-dd
    private AttendanceStatus status;
    private String markedBy;
    private long markedAtMillis;

    public AttendanceRecord() {
    }

    public AttendanceRecord(String studentId, String studentName, String rollNumber,
                             String departmentId, String semesterId, String sectionId,
                             String date, AttendanceStatus status) {
        this.id = departmentId + "_" + semesterId + "_" + sectionId + "_" + studentId + "_" + date;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.departmentId = departmentId;
        this.semesterId = semesterId;
        this.sectionId = sectionId;
        this.date = date;
        this.status = status;
        this.markedAtMillis = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public long getMarkedAtMillis() {
        return markedAtMillis;
    }

    public void setMarkedAtMillis(long markedAtMillis) {
        this.markedAtMillis = markedAtMillis;
    }
}
