package com.example.studentattendance.repository.impl;

import java.util.List;

import android.content.Context;

import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Lecture;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.network.dto.DepartmentCreateRequestDto;
import com.example.studentattendance.network.dto.DepartmentUpdateRequestDto;
import com.example.studentattendance.network.dto.SectionCreateRequestDto;
import com.example.studentattendance.network.dto.SectionUpdateRequestDto;
import com.example.studentattendance.network.dto.SemesterCreateRequestDto;
import com.example.studentattendance.network.dto.SemesterUpdateRequestDto;
import com.example.studentattendance.network.dto.SubjectCreateRequestDto;
import com.example.studentattendance.network.dto.SubjectUpdateRequestDto;
import com.example.studentattendance.repository.DepartmentRepository;

public class RetrofitDepartmentRepository implements DepartmentRepository {

    private final Context appContext;

    public RetrofitDepartmentRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void getDepartments(ApiCallback<List<Department>> callback) {
        RetrofitExec.enqueue(api().getDepartments(), callback);
    }

    @Override
    public void getClassSetups(ApiCallback<List<ClassSetup>> callback) {
        callback.onError("Set up departments in the local workspace.");
    }

    @Override
    public void getSemesters(String departmentId, ApiCallback<List<Semester>> callback) {
        if (departmentId == null) {
            callback.onError("Please select a department first.");
            return;
        }
        RetrofitExec.enqueue(api().getSemesters(departmentId), callback);
    }

    @Override
    public void getSections(String semesterId, ApiCallback<List<Section>> callback) {
        if (semesterId == null) {
            callback.onError("Please select a semester first.");
            return;
        }
        RetrofitExec.enqueue(api().getSections(semesterId), callback);
    }

    @Override
    public void createDepartment(String name, String code, String headOfDepartment, ApiCallback<Department> callback) {
        RetrofitExec.enqueue(api().createDepartment(new DepartmentCreateRequestDto(name, code, headOfDepartment)), callback);
    }

    @Override
    public void createAcademicSetup(String departmentName, String shift, int semesterNumber, String sectionName,
                                    String courseName, String courseCode, ApiCallback<Department> callback) {
        callback.onError("Set up departments in the local workspace.");
    }

    @Override
    public void importAcademicStructure(com.example.studentattendance.utils.AcademicExcelParser.Result parsed,
                                        ApiCallback<com.example.studentattendance.model.ImportResult> callback) {
        callback.onError("Import Excel in the local workspace.");
    }

    @Override
    public void updateAcademicSetup(String subjectId, String departmentName, String shift, int semesterNumber,
                                    String sectionName, String courseName, String courseCode,
                                    ApiCallback<Department> callback) {
        callback.onError("Set up departments in the local workspace.");
    }

    @Override
    public void createSemester(String departmentId, String name, int semesterNumber, ApiCallback<Semester> callback) {
        RetrofitExec.enqueue(api().createSemester(departmentId, new SemesterCreateRequestDto(name, semesterNumber)), callback);
    }

    @Override
    public void createSection(String semesterId, String name, ApiCallback<Section> callback) {
        RetrofitExec.enqueue(api().createSection(semesterId, new SectionCreateRequestDto(name)), callback);
    }

    @Override
    public void updateDepartment(String departmentId, String name, String code, String headOfDepartment,
                                  Boolean archived, ApiCallback<Department> callback) {
        RetrofitExec.enqueue(api().updateDepartment(departmentId,
                new DepartmentUpdateRequestDto(name, code, headOfDepartment, archived)), callback);
    }

    @Override
    public void deleteDepartment(String departmentId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().deleteDepartment(departmentId), callback);
    }

    @Override
    public void updateSemester(String semesterId, String name, Integer semesterNumber, Boolean archived,
                                ApiCallback<Semester> callback) {
        RetrofitExec.enqueue(api().updateSemester(semesterId,
                new SemesterUpdateRequestDto(name, semesterNumber, archived)), callback);
    }

    @Override
    public void deleteSemester(String semesterId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().deleteSemester(semesterId), callback);
    }

    @Override
    public void updateSection(String sectionId, String name, Boolean archived, ApiCallback<Section> callback) {
        RetrofitExec.enqueue(api().updateSection(sectionId, new SectionUpdateRequestDto(name, archived)), callback);
    }

    @Override
    public void deleteSection(String sectionId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().deleteSection(sectionId), callback);
    }

    @Override
    public void getSubjects(String departmentId, ApiCallback<List<Subject>> callback) {
        RetrofitExec.enqueue(api().getSubjects(departmentId, false), callback);
    }

    @Override
    public void createSubject(String departmentId, String name, String code, ApiCallback<Subject> callback) {
        RetrofitExec.enqueue(api().createSubject(departmentId, new SubjectCreateRequestDto(name, code)), callback);
    }

    @Override
    public void updateSubject(String subjectId, String name, String code, Boolean archived,
                               ApiCallback<Subject> callback) {
        RetrofitExec.enqueue(api().updateSubject(subjectId, new SubjectUpdateRequestDto(name, code, archived)), callback);
    }

    @Override
    public void deleteSubject(String subjectId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().deleteSubject(subjectId), callback);
    }

    @Override
    public void getLectures(String sectionId, String subjectId, ApiCallback<List<Lecture>> callback) {
        callback.onSuccess(new java.util.ArrayList<>());
    }

    @Override
    public void createLecture(String sectionId, String subjectId, int lectureNumber, String title,
                              ApiCallback<Lecture> callback) {
        callback.onError("Lectures are saved on this device.");
    }

    @Override
    public void deleteLecture(String lectureId, ApiCallback<Void> callback) {
        callback.onError("Lectures are saved on this device.");
    }
}
