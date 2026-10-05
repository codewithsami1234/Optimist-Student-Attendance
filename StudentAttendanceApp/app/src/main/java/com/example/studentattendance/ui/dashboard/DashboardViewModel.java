package com.example.studentattendance.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.DashboardStats;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.repository.AnalyticsRepository;
import com.example.studentattendance.repository.AuthRepository;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.ReportAnalytics;

/**
 * Loads today's snapshot, low-attendance alerts, and recent sessions from
 * local Room analytics. Reports still owns weekly/monthly trends.
 */
public class DashboardViewModel extends AndroidViewModel {

    public static class DashboardData {
        public int totalStudents;
        public int presentToday;
        public int absentToday;
        public int lateToday;
        public int leaveToday;
        public double attendanceRateToday;
        public boolean hasAttendanceToday;
        public List<AttendanceSession> recentSessions = new ArrayList<>();
        public List<StudentAttendanceSummary> lowAttendanceStudents = new ArrayList<>();
        public double lowAttendanceThreshold = 75;
    }

    private final AuthRepository authRepository;
    private final AnalyticsRepository analyticsRepository;

    private final MutableLiveData<NetworkResult<DashboardData>> dashboardData = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance(application);
        authRepository = provider.getAuthRepository();
        analyticsRepository = provider.getAnalyticsRepository();
    }

    public User getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<NetworkResult<DashboardData>> getDashboardData() {
        return dashboardData;
    }

    public void loadDashboard() {
        dashboardData.setValue(NetworkResult.loading());

        analyticsRepository.getDashboardStats(new ApiCallback<DashboardStats>() {
            @Override
            public void onSuccess(DashboardStats stats) {
                DashboardData data = new DashboardData();
                data.totalStudents = stats.getTotalStudents();
                data.presentToday = stats.getPresentToday();
                data.absentToday = stats.getAbsentToday();
                data.lateToday = stats.getLateToday();
                data.leaveToday = stats.getLeaveToday();
                ReportAnalytics.TodayOverview today = ReportAnalytics.todayOverview(
                        stats.getPresentToday(), stats.getAbsentToday(),
                        stats.getLateToday(), stats.getLeaveToday());
                data.attendanceRateToday = today.attendanceRate;
                data.hasAttendanceToday = today.hasData;
                data.recentSessions = stats.getRecentSessions() != null ? stats.getRecentSessions() : new ArrayList<>();
                data.lowAttendanceStudents = stats.getLowAttendanceStudents() != null
                        ? stats.getLowAttendanceStudents() : new ArrayList<>();
                data.lowAttendanceThreshold = stats.getLowAttendanceThreshold();
                dashboardData.setValue(NetworkResult.success(data));
            }

            @Override
            public void onError(String message) {
                dashboardData.setValue(NetworkResult.error(message));
            }
        });
    }
}
