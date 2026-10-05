package com.example.studentattendance.repository;

import java.util.List;

import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;

public interface StudentRepository {
    void getStudents(String departmentId, String semesterId, String sectionId,
                      ApiCallback<List<Student>> callback);
    void addStudent(Student student, ApiCallback<Student> callback);
    void updateStudent(String studentId, String name, String rollNumber, String email, String phone,
                        Boolean archived, ApiCallback<Student> callback);
    void transferStudent(String studentId, String sectionId, ApiCallback<Student> callback);
    void deleteStudent(String studentId, ApiCallback<Void> callback);
    void importStudents(List<Student> students, ApiCallback<ImportResult> callback);
    void getAllStudents(ApiCallback<List<Student>> callback);
}
