package com.example.studentattendance.ui.departments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.service.RepositoryProvider;

/**
 * Loads saved class/course cards and applies client-side search filtering.
 */
public class DepartmentsViewModel extends AndroidViewModel {

    private final DepartmentRepository departmentRepository;
    private final MutableLiveData<NetworkResult<List<ClassSetup>>> departments = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<Department>> createResult = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<Void>> deleteResult = new MutableLiveData<>();
    private List<ClassSetup> allSetups = new ArrayList<>();
    private String currentQuery = "";

    public DepartmentsViewModel(@NonNull Application application) {
        super(application);
        departmentRepository = RepositoryProvider.getInstance(application).getDepartmentRepository();
    }

    public LiveData<NetworkResult<List<ClassSetup>>> getDepartments() {
        return departments;
    }

    public LiveData<NetworkResult<Department>> getCreateResult() {
        return createResult;
    }

    public LiveData<NetworkResult<Void>> getDeleteResult() {
        return deleteResult;
    }

    public void createDepartment(String name, String code, String headOfDepartment) {
        createResult.setValue(NetworkResult.loading());
        departmentRepository.createDepartment(name, code, headOfDepartment, new ApiCallback<Department>() {
            @Override
            public void onSuccess(Department data) {
                createResult.setValue(NetworkResult.success(data));
                loadDepartments();
            }

            @Override
            public void onError(String message) {
                createResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void createAcademicSetup(String departmentName, String shift, int semesterNumber,
                                    String sectionName, String courseName, String courseCode) {
        createResult.setValue(NetworkResult.loading());
        departmentRepository.createAcademicSetup(departmentName, shift, semesterNumber, sectionName,
                courseName, courseCode, new ApiCallback<Department>() {
                    @Override
                    public void onSuccess(Department data) {
                        createResult.setValue(NetworkResult.success(data));
                        loadDepartments();
                    }

                    @Override
                    public void onError(String message) {
                        createResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void updateAcademicSetup(String subjectId, String departmentName, String shift,
                                    int semesterNumber, String sectionName, String courseName,
                                    String courseCode) {
        createResult.setValue(NetworkResult.loading());
        departmentRepository.updateAcademicSetup(subjectId, departmentName, shift, semesterNumber,
                sectionName, courseName, courseCode, new ApiCallback<Department>() {
                    @Override
                    public void onSuccess(Department data) {
                        createResult.setValue(NetworkResult.success(data));
                        loadDepartments();
                    }

                    @Override
                    public void onError(String message) {
                        createResult.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void updateDepartment(String departmentId, String name, String code, String head, Boolean archived) {
        createResult.setValue(NetworkResult.loading());
        departmentRepository.updateDepartment(departmentId, name, code, head, archived, new ApiCallback<Department>() {
            @Override
            public void onSuccess(Department data) {
                createResult.setValue(NetworkResult.success(data));
                loadDepartments();
            }

            @Override
            public void onError(String message) {
                createResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteDepartment(String departmentId) {
        departmentRepository.deleteDepartment(departmentId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadDepartments();
            }

            @Override
            public void onError(String message) {
                createResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteClassSetup(String subjectId) {
        deleteResult.setValue(NetworkResult.loading());
        departmentRepository.deleteSubject(subjectId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                deleteResult.setValue(NetworkResult.success(null));
                loadDepartments();
            }

            @Override
            public void onError(String message) {
                deleteResult.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadDepartments() {
        departments.setValue(NetworkResult.loading());
        departmentRepository.getClassSetups(new ApiCallback<List<ClassSetup>>() {
            @Override
            public void onSuccess(List<ClassSetup> data) {
                allSetups = data == null ? new ArrayList<>() : data;
                applyFilter();
            }

            @Override
            public void onError(String message) {
                departments.setValue(NetworkResult.error(message));
            }
        });
    }

    public void search(String query) {
        currentQuery = query == null ? "" : query.toLowerCase(Locale.getDefault()).trim();
        applyFilter();
    }

    private void applyFilter() {
        if (currentQuery.isEmpty()) {
            departments.setValue(NetworkResult.success(allSetups));
            return;
        }
        List<ClassSetup> filtered = new ArrayList<>();
        for (ClassSetup setup : allSetups) {
            if (setup.matchesQuery(currentQuery).contains(currentQuery)) {
                filtered.add(setup);
            }
        }
        departments.setValue(NetworkResult.success(filtered));
    }
}
