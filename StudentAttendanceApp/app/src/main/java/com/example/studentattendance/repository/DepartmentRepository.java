package com.example.studentattendance.repository;

import java.util.List;

import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Lecture;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.utils.AcademicExcelParser;

public interface DepartmentRepository {
    void getDepartments(ApiCallback<List<Department>> callback);
    void getClassSetups(ApiCallback<List<ClassSetup>> callback);
    void getSemesters(String departmentId, ApiCallback<List<Semester>> callback);
    void getSections(String semesterId, ApiCallback<List<Section>> callback);

    void createDepartment(String name, String code, String headOfDepartment, ApiCallback<Department> callback);
    void createAcademicSetup(String departmentName, String shift, int semesterNumber, String sectionName,
                             String courseName, String courseCode, ApiCallback<Department> callback);

    /**
     * Adds or reuses departments, semesters, sections, courses, and students from
     * a validated workbook. Does not delete attendance or existing students.
     */
    void importAcademicStructure(AcademicExcelParser.Result parsed, ApiCallback<ImportResult> callback);
    void updateAcademicSetup(String subjectId, String departmentName, String shift, int semesterNumber,
                             String sectionName, String courseName, String courseCode,
                             ApiCallback<Department> callback);
    void createSemester(String departmentId, String name, int semesterNumber, ApiCallback<Semester> callback);
    void createSection(String semesterId, String name, ApiCallback<Section> callback);

    void updateDepartment(String departmentId, String name, String code, String headOfDepartment, Boolean archived,
                          ApiCallback<Department> callback);
    void deleteDepartment(String departmentId, ApiCallback<Void> callback);
    void updateSemester(String semesterId, String name, Integer semesterNumber, Boolean archived,
                        ApiCallback<Semester> callback);
    void deleteSemester(String semesterId, ApiCallback<Void> callback);
    void updateSection(String sectionId, String name, Boolean archived, ApiCallback<Section> callback);
    void deleteSection(String sectionId, ApiCallback<Void> callback);

    void getSubjects(String departmentId, ApiCallback<List<Subject>> callback);
    void createSubject(String departmentId, String name, String code, ApiCallback<Subject> callback);
    void updateSubject(String subjectId, String name, String code, Boolean archived, ApiCallback<Subject> callback);
    void deleteSubject(String subjectId, ApiCallback<Void> callback);

    void getLectures(String sectionId, String subjectId, ApiCallback<List<Lecture>> callback);
    void createLecture(String sectionId, String subjectId, int lectureNumber, String title, ApiCallback<Lecture> callback);
    void deleteLecture(String lectureId, ApiCallback<Void> callback);
}
