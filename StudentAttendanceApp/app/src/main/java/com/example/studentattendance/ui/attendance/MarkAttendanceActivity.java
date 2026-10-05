package com.example.studentattendance.ui.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.StudentAttendanceAdapter;
import com.example.studentattendance.databinding.ActivityMarkAttendanceBinding;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.main.MainActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.ViewUtils;

/**
 * The heart of the app: shows every student in the selected class with
 * Present / Absent / Late / Leave toggle buttons, a live running summary,
 * a "Mark All Present" shortcut, and a Save button that persists the
 * completed session (only once every student has been marked).
 */
public class MarkAttendanceActivity extends AppCompatActivity {

    private ActivityMarkAttendanceBinding binding;
    private MarkAttendanceViewModel viewModel;
    private StudentAttendanceAdapter adapter;

    private Department department;
    private Semester semester;
    private Section section;
    private com.example.studentattendance.model.Subject subject;
    private String date;
    private int lectureNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarkAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        department = (Department) getIntent().getSerializableExtra(Constants.EXTRA_DEPARTMENT);
        semester = (Semester) getIntent().getSerializableExtra(Constants.EXTRA_SEMESTER);
        section = (Section) getIntent().getSerializableExtra(Constants.EXTRA_SECTION);
        subject = (com.example.studentattendance.model.Subject) getIntent().getSerializableExtra(Constants.EXTRA_SUBJECT);
        date = getIntent().getStringExtra(Constants.EXTRA_DATE);
        lectureNumber = 1;
        if (date == null || date.isEmpty()) {
            date = DateUtils.today();
        }
        if (department == null || semester == null || section == null || date == null) {
            finish();
            return;
        }

        String subjectLabel = subject != null ? subject.getName() : getString(R.string.label_subject);
        StringBuilder classInfo = new StringBuilder();
        classInfo.append(department.displayLabel()).append(" · ").append(semester.getName());
        if (section.getName() != null && !section.getName().trim().isEmpty()) {
            classInfo.append(" · ").append(section.getName().trim());
        }
        classInfo.append(" · ").append(subjectLabel);
        if (subject != null && subject.getCode() != null && !subject.getCode().isEmpty()) {
            classInfo.append(" (").append(subject.getCode()).append(")");
        }
        binding.textClassInfo.setText(classInfo.toString());

        viewModel = new ViewModelProvider(this).get(MarkAttendanceViewModel.class);

        adapter = new StudentAttendanceAdapter((studentId, status) -> {
            viewModel.onStatusChanged(studentId, status);
            updateLiveSummary();
        });
        binding.recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerStudents.setAdapter(adapter);

        binding.buttonMarkAllPresent.setOnClickListener(v -> {
            adapter.markAllPresent();
            updateLiveSummary();
        });
        binding.editAttendanceSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        binding.buttonSaveAttendance.setOnClickListener(v -> saveAttendance());
        binding.includeError.buttonRetry.setOnClickListener(v -> loadStudents());

        viewModel.getStudents().observe(this, this::renderStudents);
        viewModel.getSaveResult().observe(this, this::renderSaveResult);
        viewModel.getExistingSessionLoaded().observe(this, loaded -> {
            if (Boolean.TRUE.equals(loaded)) {
                ViewUtils.toastShort(this, getString(R.string.existing_session_loaded));
            }
        });

        loadStudents();
    }

    private void loadStudents() {
        String subjectId = subject != null ? subject.getId() : null;
        viewModel.loadStudents(department.getId(), semester.getId(), section.getId(), subjectId, date, lectureNumber);
    }

    private void saveAttendance() {
        String error = viewModel.validateAllMarked();
        if (error != null) {
            ViewUtils.toastLong(this, error);
            return;
        }
        String subjectId = subject != null ? subject.getId() : null;
        String subjectName = subject != null ? subject.getName() : getString(R.string.label_subject);
        viewModel.saveAttendance(department.getId(), department.displayLabel(), semester.getId(),
                semester.getName(), section.getId(), section.getName(), subjectId, subjectName, date,
                lectureNumber);
    }

    private void renderStudents(NetworkResult<List<Student>> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                showState(State.LOADING);
                break;
            case SUCCESS:
                List<Student> data = result.getData();
                if (data == null || data.isEmpty()) {
                    binding.includeEmpty.textEmptyTitle.setText(R.string.students_empty_title);
                    binding.includeEmpty.textEmptySubtitle.setText(R.string.mark_attendance_empty_students);
                    showState(State.EMPTY);
                } else {
                    adapter.submitList(data, viewModel.getLiveStatusMap());
                    binding.textSubtitle.setText(getString(R.string.mark_attendance_subtitle,
                            data.size(), DateUtils.toDisplayFormat(date)));
                    showState(State.CONTENT);
                    updateLiveSummary();
                }
                break;
            case ERROR:
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                break;
        }
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.recyclerStudents.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeEmpty.layoutEmpty.setVisibility(state == State.EMPTY ? View.VISIBLE : View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
        binding.buttonSaveAttendance.setEnabled(state == State.CONTENT);
        binding.buttonMarkAllPresent.setEnabled(state == State.CONTENT);
    }

    private void updateLiveSummary() {
        int present = viewModel.countByStatus(AttendanceStatus.PRESENT);
        int absent = viewModel.countByStatus(AttendanceStatus.ABSENT);
        int late = viewModel.countByStatus(AttendanceStatus.LATE);
        int leave = viewModel.countByStatus(AttendanceStatus.LEAVE);
        binding.textLiveSummary.setText(getString(R.string.attendance_summary_format, present, absent, late, leave));
    }

    private void renderSaveResult(NetworkResult<AttendanceSession> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                binding.buttonSaveAttendance.setEnabled(false);
                break;
            case SUCCESS:
                binding.buttonSaveAttendance.setEnabled(true);
                int present = viewModel.countByStatus(AttendanceStatus.PRESENT);
                int absent = viewModel.countByStatus(AttendanceStatus.ABSENT);
                int late = viewModel.countByStatus(AttendanceStatus.LATE);
                int leave = viewModel.countByStatus(AttendanceStatus.LEAVE);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.attendance_saved_success)
                        .setMessage(getString(R.string.attendance_saved_detail, present, absent, late, leave))
                        .setPositiveButton(R.string.dashboard_action_view_history, (dialog, which) -> openHistory())
                        .setCancelable(false)
                        .show();
                break;
            case ERROR:
                binding.buttonSaveAttendance.setEnabled(true);
                ViewUtils.toastLong(this, result.getMessage());
                break;
        }
    }

    private void openHistory() {
        Intent history = new Intent(this, MainActivity.class);
        history.putExtra(Constants.EXTRA_OPEN_TAB, R.id.nav_history);
        history.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(history);
        finish();
    }
}
