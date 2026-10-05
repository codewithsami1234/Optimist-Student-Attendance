package com.example.studentattendance.ui.students;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.service.ExcelService;
import com.example.studentattendance.service.RepositoryProvider;

/**
 * Loads and searches the student roster for a specific department/semester/section,
 * and handles adding a new student to that section.
 */
public class StudentsViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final ExcelService excelService;
    private final MutableLiveData<NetworkResult<List<Student>>> students = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<Student>> addStudentResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<Integer>> exportResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<com.example.studentattendance.model.CsvParseResult>> parseResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<com.example.studentattendance.model.ImportResult>> importResult = new MutableLiveData<>();

    private List<Student> allStudents = new ArrayList<>();
    private String currentQuery = "";
    private com.example.studentattendance.model.CsvParseResult lastParse;

    public StudentsViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        studentRepository = provider.getStudentRepository();
        excelService = provider.getExcelService();
    }

    public LiveData<NetworkResult<List<Student>>> getStudents() {
        return students;
    }

    public LiveData<NetworkResult<Student>> getAddStudentResult() {
        return addStudentResult;
    }

    public LiveData<NetworkResult<Integer>> getExportResult() {
        return exportResult;
    }

    public LiveData<NetworkResult<com.example.studentattendance.model.CsvParseResult>> getParseResult() {
        return parseResult;
    }

    public LiveData<NetworkResult<com.example.studentattendance.model.ImportResult>> getImportResult() {
        return importResult;
    }

    public void loadStudents(String departmentId, String semesterId, String sectionId) {
        students.setValue(NetworkResult.loading());
        studentRepository.getStudents(departmentId, semesterId, sectionId, new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> data) {
                allStudents = data;
                applyFilter();
            }

            @Override
            public void onError(String message) {
                students.setValue(NetworkResult.error(message));
            }
        });
    }

    public void search(String query) {
        currentQuery = query == null ? "" : query.toLowerCase(Locale.getDefault()).trim();
        applyFilter();
    }

    private void applyFilter() {
        if (currentQuery.isEmpty()) {
            students.setValue(NetworkResult.success(allStudents));
            return;
        }
        List<Student> filtered = new ArrayList<>();
        for (Student s : allStudents) {
            if (s.getName().toLowerCase(Locale.getDefault()).contains(currentQuery)
                    || s.getRollNumber().toLowerCase(Locale.getDefault()).contains(currentQuery)) {
                filtered.add(s);
            }
        }
        students.setValue(NetworkResult.success(filtered));
    }

    public void addStudent(Student student, String departmentId, String semesterId, String sectionId) {
        addStudentResult.setValue(NetworkResult.loading());
        studentRepository.addStudent(student, new ApiCallback<Student>() {
            @Override
            public void onSuccess(Student data) {
                addStudentResult.setValue(NetworkResult.success(data));
                loadStudents(departmentId, semesterId, sectionId);
            }

            @Override
            public void onError(String message) {
                addStudentResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void updateStudent(String studentId, String name, String roll, String email, String phone,
                               Boolean archived, String departmentId, String semesterId, String sectionId) {
        addStudentResult.setValue(NetworkResult.loading());
        studentRepository.updateStudent(studentId, name, roll, email, phone, archived, new ApiCallback<Student>() {
            @Override
            public void onSuccess(Student data) {
                addStudentResult.setValue(NetworkResult.success(data));
                loadStudents(departmentId, semesterId, sectionId);
            }

            @Override
            public void onError(String message) {
                addStudentResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void transferStudent(String studentId, String newSectionId, String departmentId,
                                 String semesterId, String sectionId) {
        addStudentResult.setValue(NetworkResult.loading());
        studentRepository.transferStudent(studentId, newSectionId, new ApiCallback<Student>() {
            @Override
            public void onSuccess(Student data) {
                addStudentResult.setValue(NetworkResult.success(data));
                loadStudents(departmentId, semesterId, sectionId);
            }

            @Override
            public void onError(String message) {
                addStudentResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteStudent(String studentId, String departmentId, String semesterId, String sectionId) {
        studentRepository.deleteStudent(studentId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadStudents(departmentId, semesterId, sectionId);
            }

            @Override
            public void onError(String message) {
                addStudentResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void exportStudents(android.net.Uri destinationUri) {
        exportResult.setValue(NetworkResult.loading());
        excelService.exportStudents(destinationUri, allStudents, new ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                exportResult.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                exportResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void parseImport(android.net.Uri sourceUri, String departmentId, String semesterId, String sectionId) {
        parseResult.setValue(NetworkResult.loading());
        excelService.parseStudentImport(sourceUri, departmentId, semesterId, sectionId,
                new ApiCallback<com.example.studentattendance.model.CsvParseResult>() {
                    @Override
                    public void onSuccess(com.example.studentattendance.model.CsvParseResult data) {
                        lastParse = data;
                        parseResult.setValue(NetworkResult.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        parseResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void confirmImport(String departmentId, String semesterId, String sectionId) {
        if (lastParse == null || lastParse.validStudents.isEmpty()) {
            importResult.setValue(NetworkResult.error("There are no valid rows to import."));
            return;
        }
        importResult.setValue(NetworkResult.loading());
        studentRepository.importStudents(lastParse.validStudents,
                new ApiCallback<com.example.studentattendance.model.ImportResult>() {
                    @Override
                    public void onSuccess(com.example.studentattendance.model.ImportResult data) {
                        importResult.setValue(NetworkResult.success(data));
                        loadStudents(departmentId, semesterId, sectionId);
                    }

                    @Override
                    public void onError(String message) {
                        importResult.setValue(NetworkResult.error(message));
                    }
                });
    }
}
