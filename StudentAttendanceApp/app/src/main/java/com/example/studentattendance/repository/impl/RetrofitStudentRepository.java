package com.example.studentattendance.repository.impl;

import java.util.List;

import android.content.Context;

import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.network.dto.StudentTransferRequestDto;
import com.example.studentattendance.network.dto.StudentUpdateRequestDto;
import com.example.studentattendance.repository.StudentRepository;

public class RetrofitStudentRepository implements StudentRepository {

    private final Context appContext;

    public RetrofitStudentRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void getStudents(String departmentId, String semesterId, String sectionId,
                             ApiCallback<List<Student>> callback) {
        if (sectionId == null) {
            callback.onError("Please select a section first.");
            return;
        }
        RetrofitExec.enqueue(api().getStudents(departmentId, semesterId, sectionId, false), callback);
    }

    @Override
    public void addStudent(Student student, ApiCallback<Student> callback) {
        RetrofitExec.enqueue(api().addStudent(student), callback);
    }

    @Override
    public void updateStudent(String studentId, String name, String rollNumber, String email, String phone,
                               Boolean archived, ApiCallback<Student> callback) {
        RetrofitExec.enqueue(api().updateStudent(studentId,
                new StudentUpdateRequestDto(name, rollNumber, email, phone, archived)), callback);
    }

    @Override
    public void transferStudent(String studentId, String sectionId, ApiCallback<Student> callback) {
        RetrofitExec.enqueue(api().transferStudent(studentId, new StudentTransferRequestDto(sectionId)), callback);
    }

    @Override
    public void deleteStudent(String studentId, ApiCallback<Void> callback) {
        RetrofitExec.enqueue(api().deleteStudent(studentId), callback);
    }

    @Override
    public void importStudents(List<Student> students, ApiCallback<ImportResult> callback) {
        if (students == null || students.isEmpty()) {
            callback.onError("No valid student rows were found in the selected file.");
            return;
        }
        RetrofitExec.enqueue(api().importStudents(students), callback);
    }

    @Override
    public void getAllStudents(ApiCallback<List<Student>> callback) {
        RetrofitExec.enqueue(api().getAllStudents(true), callback);
    }
}
