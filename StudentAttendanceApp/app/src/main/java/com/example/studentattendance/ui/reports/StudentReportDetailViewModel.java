package com.example.studentattendance.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AnalyticsRepository;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;
import com.example.studentattendance.service.RepositoryProvider;

/**
 * Loads one student's attendance history and rate from local Room, and
 * records teacher-controlled attendance adjustments.
 */
public class StudentReportDetailViewModel extends AndroidViewModel {

    private final AnalyticsRepository analyticsRepository;
    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final MutableLiveData<NetworkResult<StudentAttendanceReport>> report = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<AttendanceAdjustment>>> adjustmentHistory = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<AttendanceAdjustment>> createAdjustmentResult = new MutableLiveData<>();
    private String studentId;

    public StudentReportDetailViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        analyticsRepository = provider.getAnalyticsRepository();
        adjustmentRepository = provider.getAttendanceAdjustmentRepository();
    }

    public LiveData<NetworkResult<StudentAttendanceReport>> getReport() {
        return report;
    }

    public LiveData<NetworkResult<List<AttendanceAdjustment>>> getAdjustmentHistory() {
        return adjustmentHistory;
    }

    public LiveData<NetworkResult<AttendanceAdjustment>> getCreateAdjustmentResult() {
        return createAdjustmentResult;
    }

    public void loadReport(String studentId) {
        this.studentId = studentId;
        report.setValue(NetworkResult.loading());
        analyticsRepository.getStudentReport(studentId, new ApiCallback<StudentAttendanceReport>() {
            @Override
            public void onSuccess(StudentAttendanceReport data) {
                report.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                report.setValue(NetworkResult.error(message));
            }
        });
        loadAdjustmentHistory();
    }

    public void loadAdjustmentHistory() {
        if (studentId == null) {
            return;
        }
        adjustmentRepository.getHistory(studentId, new ApiCallback<List<AttendanceAdjustment>>() {
            @Override
            public void onSuccess(List<AttendanceAdjustment> data) {
                adjustmentHistory.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                adjustmentHistory.setValue(NetworkResult.error(message));
            }
        });
    }

    /** Phase 4: apply teacher-controlled attendance relief. Refreshes the report + history on success. */
    public void createAdjustment(int delta, String reason) {
        if (studentId == null) {
            return;
        }
        createAdjustmentResult.setValue(NetworkResult.loading());
        adjustmentRepository.createAdjustment(studentId, delta, reason, new ApiCallback<AttendanceAdjustment>() {
            @Override
            public void onSuccess(AttendanceAdjustment data) {
                createAdjustmentResult.setValue(NetworkResult.success(data));
                com.example.studentattendance.sync.LocalNotificationPublisher
                        .publishUnreadNowAsync(getApplication());
                loadReport(studentId);
            }

            @Override
            public void onError(String message) {
                createAdjustmentResult.setValue(NetworkResult.error(message));
            }
        });
    }
}
