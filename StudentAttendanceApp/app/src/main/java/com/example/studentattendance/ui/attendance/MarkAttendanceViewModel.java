package com.example.studentattendance.ui.attendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AttendanceRepository;
import com.example.studentattendance.repository.StudentRepository;
import com.example.studentattendance.service.RepositoryProvider;

public class MarkAttendanceViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    private final MutableLiveData<NetworkResult<List<Student>>> students = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<AttendanceSession>> saveResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> existingSessionLoaded = new MutableLiveData<>(false);

    private final Map<String, AttendanceStatus> liveStatusMap = new HashMap<>();
    private final Map<String, String> studentNames = new HashMap<>();
    private final Map<String, String> studentRolls = new HashMap<>();

    public MarkAttendanceViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        studentRepository = provider.getStudentRepository();
        attendanceRepository = provider.getAttendanceRepository();
    }

    public LiveData<NetworkResult<List<Student>>> getStudents() { return students; }
    public LiveData<NetworkResult<AttendanceSession>> getSaveResult() { return saveResult; }
    public LiveData<Boolean> getExistingSessionLoaded() { return existingSessionLoaded; }

    public Map<String, AttendanceStatus> getLiveStatusMap() { return liveStatusMap; }

    public void loadStudents(String departmentId, String semesterId, String sectionId,
                              String subjectId, String date, int lectureNumber) {
        students.setValue(NetworkResult.loading());
        studentRepository.getStudents(departmentId, semesterId, sectionId, new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> data) {
                liveStatusMap.clear();
                studentNames.clear();
                studentRolls.clear();
                for (Student s : data) {
                    liveStatusMap.put(s.getId(), AttendanceStatus.NOT_MARKED);
                    studentNames.put(s.getId(), s.getName());
                    studentRolls.put(s.getId(), s.getRollNumber());
                }
                students.setValue(NetworkResult.success(data));
                loadExistingSession(departmentId, semesterId, sectionId, subjectId, date, lectureNumber);
            }

            @Override
            public void onError(String message) {
                students.setValue(NetworkResult.error(message));
            }
        });
    }

    private void loadExistingSession(String departmentId, String semesterId, String sectionId,
                                      String subjectId, String date, int lectureNumber) {
        attendanceRepository.getSession(departmentId, semesterId, sectionId, subjectId, date, lectureNumber,
                new ApiCallback<AttendanceSession>() {
                    @Override
                    public void onSuccess(AttendanceSession session) {
                        if (session != null && session.getRecords() != null) {
                            for (AttendanceRecord record : session.getRecords()) {
                                if (liveStatusMap.containsKey(record.getStudentId())) {
                                    liveStatusMap.put(record.getStudentId(), record.getStatus());
                                }
                            }
                            existingSessionLoaded.setValue(true);
                            NetworkResult<List<Student>> current = students.getValue();
                            if (current != null && current.getData() != null) {
                                students.setValue(NetworkResult.success(current.getData()));
                            }
                        } else {
                            existingSessionLoaded.setValue(false);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        existingSessionLoaded.setValue(false);
                    }
                });
    }

    public void onStatusChanged(String studentId, AttendanceStatus status) {
        liveStatusMap.put(studentId, status);
    }

    public int countByStatus(AttendanceStatus status) {
        int count = 0;
        for (AttendanceStatus s : liveStatusMap.values()) {
            if (s == status) {
                count++;
            }
        }
        return count;
    }

    public String validateAllMarked() {
        if (liveStatusMap.isEmpty()) {
            return "There are no students to save attendance for.";
        }
        for (AttendanceStatus status : liveStatusMap.values()) {
            if (status == null || status == AttendanceStatus.NOT_MARKED) {
                return "Please mark attendance for every student before saving.";
            }
        }
        return null;
    }

    public void saveAttendance(String departmentId, String departmentName, String semesterId,
                                String semesterName, String sectionId, String sectionName,
                                String subjectId, String subjectName, String date, int lectureNumber) {
        String error = validateAllMarked();
        if (error != null) {
            saveResult.setValue(NetworkResult.error(error));
            return;
        }
        saveResult.setValue(NetworkResult.loading());
        attendanceRepository.saveSession(departmentId, departmentName, semesterId, semesterName,
                sectionId, sectionName, subjectId, subjectName, date, lectureNumber, liveStatusMap, studentNames, studentRolls,
                new ApiCallback<AttendanceSession>() {
                    @Override
                    public void onSuccess(AttendanceSession data) {
                        saveResult.setValue(NetworkResult.success(data));
                        com.example.studentattendance.sync.LocalNotificationPublisher
                                .publishUnreadNowAsync(getApplication());
                        com.example.studentattendance.sync.SyncScheduler.enqueueNow(getApplication());
                    }

                    @Override
                    public void onError(String message) {
                        saveResult.setValue(NetworkResult.error(message));
                    }
                });
    }
}
