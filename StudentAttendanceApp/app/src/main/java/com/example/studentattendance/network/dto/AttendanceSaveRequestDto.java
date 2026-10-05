package com.example.studentattendance.network.dto;

import java.util.List;

public class AttendanceSaveRequestDto {
    private final String departmentId;
    private final String departmentName;
    private final String semesterId;
    private final String semesterName;
    private final String sectionId;
    private final String sectionName;
    private final String subjectId;
    private final String subjectName;
    private final String date;
    private final List<AttendanceRecordInputDto> records;

    public AttendanceSaveRequestDto(String departmentId, String departmentName, String semesterId,
                                     String semesterName, String sectionId, String sectionName,
                                     String subjectId, String subjectName, String date,
                                     List<AttendanceRecordInputDto> records) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semesterId = semesterId;
        this.semesterName = semesterName;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.date = date;
        this.records = records;
    }
}
