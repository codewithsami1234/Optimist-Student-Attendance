package com.example.studentattendance.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.AttendanceHistoryAdapter;
import com.example.studentattendance.databinding.FragmentDashboardBinding;
import com.example.studentattendance.databinding.ItemLowAttendanceBinding;
import com.example.studentattendance.model.StudentAttendanceSummary;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.history.SessionDetailActivity;
import com.example.studentattendance.ui.reports.StudentReportDetailActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;

/**
 * Home screen: today's overview, low-attendance alerts, and recent sessions.
 * Statistics come from Room via {@link DashboardViewModel}.
 */
public class DashboardFragment extends Fragment {

    private enum State { LOADING, CONTENT, ERROR }

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private AttendanceHistoryAdapter recentActivityAdapter;
    private DashboardViewModel.DashboardData latestData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupStaticLabels();
        setupRecyclerView();
        setupCardNavigation();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadDashboard());
        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.loadDashboard());
        viewModel.getDashboardData().observe(getViewLifecycleOwner(), this::render);

        viewModel.loadDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadDashboard();
        }
    }

    private void setupStaticLabels() {
        User user = viewModel.getCurrentUser();
        if (user != null) {
            binding.textUserName.setText(user.getName());
            binding.textAvatarInitials.setText(user.getAvatarInitials());
        }

        binding.actionMarkAttendance.textActionLabel.setText(R.string.dashboard_action_mark_attendance);
        binding.actionMarkAttendance.imageActionIcon.setImageResource(R.drawable.ic_check_circle);
    }

    private void setupRecyclerView() {
        recentActivityAdapter = new AttendanceHistoryAdapter(session -> {
            Intent intent = new Intent(requireContext(), SessionDetailActivity.class);
            intent.putExtra(Constants.EXTRA_SESSION, session);
            startActivity(intent);
        });
        binding.recyclerRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecentActivity.setAdapter(recentActivityAdapter);
    }

    private void setupCardNavigation() {
        binding.actionMarkAttendance.getRoot().setOnClickListener(v -> openTab(R.id.nav_departments));
        binding.textFirstRun.setOnClickListener(v -> openTab(R.id.nav_departments));
    }

    private void openTab(int destinationId) {
        androidx.navigation.Navigation.findNavController(requireView()).navigate(destinationId);
    }

    private void render(NetworkResult<DashboardViewModel.DashboardData> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                if (latestData == null && !binding.swipeRefresh.isRefreshing()) {
                    showState(State.LOADING);
                }
                break;
            case SUCCESS:
                binding.swipeRefresh.setRefreshing(false);
                bindData(result.getData());
                showState(State.CONTENT);
                break;
            case ERROR:
                binding.swipeRefresh.setRefreshing(false);
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                break;
        }
    }

    private void showState(State state) {
        binding.swipeRefresh.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
    }

    private void bindData(DashboardViewModel.DashboardData data) {
        if (data == null) {
            return;
        }
        latestData = data;
        bindTodayOverview(data);

        recentActivityAdapter.submitList(data.recentSessions);
        boolean hasActivity = !data.recentSessions.isEmpty();
        binding.recyclerRecentActivity.setVisibility(hasActivity ? View.VISIBLE : View.GONE);
        binding.textNoRecentActivity.setVisibility(hasActivity ? View.GONE : View.VISIBLE);
        boolean firstRun = data.totalStudents == 0 && data.recentSessions.isEmpty();
        binding.textFirstRun.setVisibility(firstRun ? View.VISIBLE : View.GONE);

        bindLowAttendance(data.lowAttendanceStudents, data.lowAttendanceThreshold);
    }

    private void bindTodayOverview(DashboardViewModel.DashboardData data) {
        binding.textTodayDate.setText(DateUtils.toDisplayFormat(DateUtils.today()));
        boolean hasToday = data.hasAttendanceToday;
        binding.layoutTodayStats.setVisibility(hasToday ? View.VISIBLE : View.GONE);
        binding.layoutTodayBreakdown.setVisibility(hasToday ? View.VISIBLE : View.GONE);
        binding.layoutTodayEmpty.setVisibility(hasToday ? View.GONE : View.VISIBLE);
        if (!hasToday) {
            binding.progressTodayRate.setProgressCompat(0, false);
            binding.textTodayRate.setText("");
            return;
        }
        int percent = (int) Math.round(data.attendanceRateToday);
        binding.progressTodayRate.setProgressCompat(percent, false);
        binding.textTodayRate.setText(String.format(Locale.getDefault(), "%d%%", percent));
        binding.textTodayPresent.setText(String.valueOf(data.presentToday));
        binding.textTodayAbsent.setText(String.valueOf(data.absentToday));
        binding.textTodayLate.setText(String.valueOf(data.lateToday));
        binding.textTodayLeave.setText(String.valueOf(data.leaveToday));
    }

    private void bindLowAttendance(List<StudentAttendanceSummary> students, double threshold) {
        if (students == null) {
            students = new ArrayList<>();
        }
        binding.layoutLowAttendance.removeAllViews();
        binding.textNoAlerts.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
        binding.textNoAlerts.setText(getString(R.string.dashboard_no_alerts_format, threshold));
        for (StudentAttendanceSummary student : students) {
            ItemLowAttendanceBinding row = ItemLowAttendanceBinding.inflate(
                    LayoutInflater.from(requireContext()), binding.layoutLowAttendance, false);
            row.textStudentName.setText(student.getStudentName() + " · " + student.getRollNumber());
            int rate = (int) Math.round(student.getAttendanceRate());
            row.textRate.setText(rate + "%");
            row.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StudentReportDetailActivity.class);
                intent.putExtra(Constants.EXTRA_STUDENT_ID, student.getStudentId());
                startActivity(intent);
            });
            binding.layoutLowAttendance.addView(row.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
