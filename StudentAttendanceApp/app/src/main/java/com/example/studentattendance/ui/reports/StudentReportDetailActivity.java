package com.example.studentattendance.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.AttendanceAdjustmentAdapter;
import com.example.studentattendance.adapter.AttendanceByDateAdapter;
import com.example.studentattendance.databinding.ActivityStudentReportDetailBinding;
import com.example.studentattendance.databinding.DialogAdjustAttendanceBinding;
import com.example.studentattendance.databinding.DialogAdjustmentHistoryBinding;
import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.model.StudentAttendanceReport;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Phase 3: a single student's full attendance report - overall rate,
 * present/absent/late/leave breakdown, and the complete date-by-date
 * history - backed entirely by GET /api/students/{id}/report.
 * <p>
 * Phase 4: also the home of teacher-controlled attendance relief for this
 * student - "Adjust Attendance" applies a reasoned +/- correction on top of
 * the raw record count (never editing the day-by-day history itself), and
 * "Adjustment History" shows the full, permanent audit trail of every such
 * change.
 * <p>
 * Reached from the Reports screen's per-student list and from Dashboard's
 * low-attendance alerts, both passing {@link Constants#EXTRA_STUDENT_ID}.
 */
public class StudentReportDetailActivity extends AppCompatActivity {

    private ActivityStudentReportDetailBinding binding;
    private StudentReportDetailViewModel viewModel;
    private AttendanceByDateAdapter historyAdapter;
    private String studentId;
    private List<AttendanceAdjustment> latestAdjustments = List.of();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentReportDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        studentId = getIntent().getStringExtra(Constants.EXTRA_STUDENT_ID);
        if (studentId == null || studentId.isEmpty()) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(StudentReportDetailViewModel.class);
        historyAdapter = new AttendanceByDateAdapter();
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHistory.setAdapter(historyAdapter);

        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.loadReport(studentId));
        binding.buttonAdjustAttendance.setOnClickListener(v -> showAdjustAttendanceDialog());
        binding.buttonViewAdjustmentHistory.setOnClickListener(v -> showAdjustmentHistoryDialog());

        viewModel.getReport().observe(this, this::render);
        viewModel.getAdjustmentHistory().observe(this, this::onAdjustmentHistoryLoaded);
        viewModel.getCreateAdjustmentResult().observe(this, this::onAdjustmentCreated);
        viewModel.loadReport(studentId);
    }

    private void render(NetworkResult<StudentAttendanceReport> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                showState(State.LOADING);
                break;
            case SUCCESS:
                StudentAttendanceReport data = result.getData();
                if (data == null || data.getTotalMarked() == 0) {
                    showState(State.EMPTY);
                } else {
                    bindData(data);
                    showState(State.CONTENT);
                }
                break;
            case ERROR:
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                break;
        }
    }

    private void bindData(StudentAttendanceReport data) {
        binding.textStudentName.setText(data.getStudentName());
        binding.textStudentMeta.setText(String.format(Locale.getDefault(), "%s · %s · %s · %s",
                data.getRollNumber(), data.getDepartmentName(), data.getSemesterName(), data.getSectionName()));

        int rate = (int) Math.round(data.getAttendanceRate());
        binding.textOverallRate.setText(rate + "%");
        binding.progressOverallRate.setProgress(rate);

        String countsText = String.format(Locale.getDefault(),
                "Present: %d · Absent: %d · Late: %d · Leave: %d\n%d sessions recorded",
                data.getPresentCount(), data.getAbsentCount(), data.getLateCount(), data.getLeaveCount(),
                data.getTotalMarked());
        if (data.getAdjustmentDelta() != 0) {
            String sign = data.getAdjustmentDelta() > 0 ? "+" : "";
            countsText += String.format(Locale.getDefault(),
                    "\nIncludes a teacher adjustment of %s%d day(s) → effective present: %d",
                    sign, data.getAdjustmentDelta(), data.getAdjustedPresentCount());
        }
        binding.textCountsSummary.setText(countsText);

        historyAdapter.submitList(data.getRecords());
    }

    private void onAdjustmentHistoryLoaded(NetworkResult<List<AttendanceAdjustment>> result) {
        if (result != null && result.getStatus() == NetworkResult.Status.SUCCESS && result.getData() != null) {
            latestAdjustments = result.getData();
            String label = latestAdjustments.isEmpty()
                    ? getString(R.string.action_view_adjustment_history)
                    : getString(R.string.action_view_adjustment_history) + " (" + latestAdjustments.size() + ")";
            binding.buttonViewAdjustmentHistory.setText(label);
        }
    }

    private void onAdjustmentCreated(NetworkResult<AttendanceAdjustment> result) {
        if (result == null) {
            return;
        }
        if (result.getStatus() == NetworkResult.Status.SUCCESS) {
            ViewUtils.toastShort(this, getString(R.string.adjustment_saved));
        } else if (result.getStatus() == NetworkResult.Status.ERROR) {
            ViewUtils.toastLong(this, result.getMessage());
        }
    }

    /** Phase 4: teacher-controlled attendance relief - requires a reason, bounded magnitude, permanently audited. */
    private void showAdjustAttendanceDialog() {
        DialogAdjustAttendanceBinding dialogBinding = DialogAdjustAttendanceBinding.inflate(LayoutInflater.from(this));
        int[] delta = {1};
        dialogBinding.textDeltaValue.setText(formatDelta(delta[0]));
        dialogBinding.buttonIncreaseDelta.setOnClickListener(v -> {
            if (delta[0] < 5) {
                delta[0]++;
            }
            dialogBinding.textDeltaValue.setText(formatDelta(delta[0]));
        });
        dialogBinding.buttonDecreaseDelta.setOnClickListener(v -> {
            if (delta[0] > -5) {
                delta[0]--;
            }
            dialogBinding.textDeltaValue.setText(formatDelta(delta[0]));
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.adjust_attendance_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String reason = dialogBinding.editAdjustmentReason.getText() != null
                            ? dialogBinding.editAdjustmentReason.getText().toString().trim() : "";
                    if (delta[0] == 0) {
                        ViewUtils.toastShort(this, getString(R.string.error_required_fields));
                        return;
                    }
                    if (reason.length() < 8) {
                        ViewUtils.toastShort(this, getString(R.string.error_reason_required));
                        return;
                    }
                    viewModel.createAdjustment(delta[0], reason);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private String formatDelta(int delta) {
        String sign = delta > 0 ? "+" : "";
        return sign + delta + " day" + (Math.abs(delta) == 1 ? "" : "s");
    }

    /** Phase 4: the permanent, read-only audit trail of every adjustment made for this student. */
    private void showAdjustmentHistoryDialog() {
        DialogAdjustmentHistoryBinding dialogBinding = DialogAdjustmentHistoryBinding.inflate(LayoutInflater.from(this));
        AttendanceAdjustmentAdapter adapter = new AttendanceAdjustmentAdapter();
        dialogBinding.recyclerAdjustmentHistory.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.recyclerAdjustmentHistory.setAdapter(adapter);
        adapter.submitList(latestAdjustments);
        dialogBinding.recyclerAdjustmentHistory.setVisibility(latestAdjustments.isEmpty() ? View.GONE : View.VISIBLE);
        dialogBinding.textNoAdjustments.setVisibility(latestAdjustments.isEmpty() ? View.VISIBLE : View.GONE);

        new AlertDialog.Builder(this)
                .setTitle(R.string.adjustment_history_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.recyclerHistory.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeEmpty.layoutEmpty.setVisibility(state == State.EMPTY ? View.VISIBLE : View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
        if (state == State.EMPTY) {
            binding.includeEmpty.textEmptyTitle.setText(R.string.student_report_empty_title);
            binding.includeEmpty.textEmptySubtitle.setText(R.string.student_report_empty_subtitle);
        }
    }
}
