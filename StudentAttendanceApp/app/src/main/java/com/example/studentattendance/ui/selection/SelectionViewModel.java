package com.example.studentattendance.ui.selection;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Lecture;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.DepartmentRepository;
import com.example.studentattendance.service.RepositoryProvider;

/**
 * Loads the semester list for a department, the section list for a semester,
 * and teacher-created courses/lectures from Room.
 */
public class SelectionViewModel extends AndroidViewModel {

    private final DepartmentRepository departmentRepository;
    private final MutableLiveData<NetworkResult<List<Department>>> departments = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Semester>>> semesters = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Section>>> sections = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Subject>>> subjects = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Lecture>>> lectures = new MutableLiveData<>();

    public SelectionViewModel(@NonNull Application application) {
        super(application);
        departmentRepository = RepositoryProvider.getInstance(application).getDepartmentRepository();
    }

    public LiveData<NetworkResult<List<Department>>> getDepartments() {
        return departments;
    }

    public LiveData<NetworkResult<List<Semester>>> getSemesters() {
        return semesters;
    }

    public LiveData<NetworkResult<List<Section>>> getSections() {
        return sections;
    }

    public LiveData<NetworkResult<List<Subject>>> getSubjects() {
        return subjects;
    }

    public LiveData<NetworkResult<List<Lecture>>> getLectures() {
        return lectures;
    }

    public void loadLectures(String sectionId, String subjectId) {
        if (sectionId == null || subjectId == null) {
            lectures.setValue(NetworkResult.success(new java.util.ArrayList<>()));
            return;
        }
        lectures.setValue(NetworkResult.loading());
        departmentRepository.getLectures(sectionId, subjectId, new ApiCallback<List<Lecture>>() {
            @Override
            public void onSuccess(List<Lecture> data) {
                lectures.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                lectures.setValue(NetworkResult.error(message));
            }
        });
    }

    public void createLecture(String sectionId, String subjectId, int lectureNumber, String title) {
        departmentRepository.createLecture(sectionId, subjectId, lectureNumber, title, new ApiCallback<Lecture>() {
            @Override
            public void onSuccess(Lecture data) {
                loadLectures(sectionId, subjectId);
            }

            @Override
            public void onError(String message) {
                lectures.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteLecture(String lectureId, String sectionId, String subjectId) {
        departmentRepository.deleteLecture(lectureId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadLectures(sectionId, subjectId);
            }

            @Override
            public void onError(String message) {
                lectures.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadDepartments() {
        departments.setValue(NetworkResult.loading());
        departmentRepository.getDepartments(new ApiCallback<List<Department>>() {
            @Override
            public void onSuccess(List<Department> data) {
                departments.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                departments.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSemesters(String departmentId) {
        semesters.setValue(NetworkResult.loading());
        departmentRepository.getSemesters(departmentId, new ApiCallback<List<Semester>>() {
            @Override
            public void onSuccess(List<Semester> data) {
                semesters.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                semesters.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSections(String semesterId) {
        sections.setValue(NetworkResult.loading());
        departmentRepository.getSections(semesterId, new ApiCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {
                sections.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                sections.setValue(NetworkResult.error(message));
            }
        });
    }

    public void loadSubjects(String departmentId) {
        subjects.setValue(NetworkResult.loading());
        departmentRepository.getSubjects(departmentId, new ApiCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> data) {
                subjects.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                subjects.setValue(NetworkResult.error(message));
            }
        });
    }

    public void createSubject(String departmentId, String name, String code) {
        departmentRepository.createSubject(departmentId, name, code, new ApiCallback<Subject>() {
            @Override
            public void onSuccess(Subject data) {
                loadSubjects(departmentId);
            }

            @Override
            public void onError(String message) {
                subjects.setValue(NetworkResult.error(message));
            }
        });
    }

    public void updateSubject(String subjectId, String departmentId, String name, String code) {
        departmentRepository.updateSubject(subjectId, name, code, false,
                new ApiCallback<Subject>() {
                    @Override
                    public void onSuccess(Subject data) {
                        loadSubjects(departmentId);
                    }

                    @Override
                    public void onError(String message) {
                        subjects.setValue(NetworkResult.error(message));
                    }
                });
    }

    public void deleteSubject(String subjectId, String departmentId) {
        departmentRepository.deleteSubject(subjectId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadSubjects(departmentId);
            }

            @Override
            public void onError(String message) {
                subjects.setValue(NetworkResult.error(message));
            }
        });
    }

    public void createSemester(String departmentId, String name, int semesterNumber) {
        departmentRepository.createSemester(departmentId, name, semesterNumber, new ApiCallback<Semester>() {
            @Override
            public void onSuccess(Semester data) {
                loadSemesters(departmentId);
            }

            @Override
            public void onError(String message) {
                semesters.setValue(NetworkResult.error(message));
            }
        });
    }

    public void createSection(String semesterId, String name) {
        departmentRepository.createSection(semesterId, name, new ApiCallback<Section>() {
            @Override
            public void onSuccess(Section data) {
                loadSections(semesterId);
            }

            @Override
            public void onError(String message) {
                sections.setValue(NetworkResult.error(message));
            }
        });
    }

    public void updateSemester(String semesterId, String departmentId, String name, Integer number, Boolean archived) {
        departmentRepository.updateSemester(semesterId, name, number, archived, new ApiCallback<Semester>() {
            @Override
            public void onSuccess(Semester data) {
                loadSemesters(departmentId);
            }

            @Override
            public void onError(String message) {
                semesters.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteSemester(String semesterId, String departmentId) {
        departmentRepository.deleteSemester(semesterId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadSemesters(departmentId);
            }

            @Override
            public void onError(String message) {
                semesters.setValue(NetworkResult.error(message));
            }
        });
    }

    public void updateSection(String sectionId, String semesterId, String name, Boolean archived) {
        departmentRepository.updateSection(sectionId, name, archived, new ApiCallback<Section>() {
            @Override
            public void onSuccess(Section data) {
                loadSections(semesterId);
            }

            @Override
            public void onError(String message) {
                sections.setValue(NetworkResult.error(message));
            }
        });
    }

    public void deleteSection(String sectionId, String semesterId) {
        departmentRepository.deleteSection(sectionId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadSections(semesterId);
            }

            @Override
            public void onError(String message) {
                sections.setValue(NetworkResult.error(message));
            }
        });
    }
}
