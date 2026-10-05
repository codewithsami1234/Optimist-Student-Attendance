package com.example.studentattendance.ui.history;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.AttendanceDetailAdapter;
import com.example.studentattendance.databinding.ActivitySessionDetailBinding;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.service.ExcelService;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.attendance.MarkAttendanceActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Detail view of one saved attendance session, with status filters and Excel export.
 */
public class SessionDetailActivity extends AppCompatActivity {

    private static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ActivitySessionDetailBinding binding;
    private AttendanceDetailAdapter adapter;
    private AttendanceSession session;
    private AttendanceStatus statusFilter;
    private ExcelService excelService;
    private ActivityResultLauncher<String> createExportFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySessionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        excelService = RepositoryProvider.getInstance(this).getExcelService();

        session = (AttendanceSession) getIntent().getSerializableExtra(Constants.EXTRA_SESSION);
        if (session == null) {
            finish();
            return;
        }

        adapter = new AttendanceDetailAdapter();
        binding.recyclerAttendanceDetails.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAttendanceDetails.setAdapter(adapter);

        createExportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(MIME_XLSX), this::onExportFileChosen);

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds == null || checkedIds.isEmpty()
                    ? R.id.chipStatusAll : checkedIds.get(0);
            if (checkedId == R.id.chipStatusPresent) {
                statusFilter = AttendanceStatus.PRESENT;
            } else if (checkedId == R.id.chipStatusAbsent) {
                statusFilter = AttendanceStatus.ABSENT;
            } else if (checkedId == R.id.chipStatusLate) {
                statusFilter = AttendanceStatus.LATE;
            } else if (checkedId == R.id.chipStatusLeave) {
                statusFilter = AttendanceStatus.LEAVE;
            } else {
                statusFilter = null;
            }
            renderRecords();
        });
        binding.buttonExportSession.setOnClickListener(v ->
                createExportFileLauncher.launch(exportFileName()));
        binding.buttonEditAttendance.setOnClickListener(v -> openEditor(session));

        bindHeader();
        renderRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadSession();
    }

    private void reloadSession() {
        if (session == null) {
            return;
        }
        RepositoryProvider.getInstance(this).getAttendanceRepository().getSession(
                session.getDepartmentId(), session.getSemesterId(), session.getSectionId(),
                session.getSubjectId(), session.getDate(), session.getLectureNumber(),
                new ApiCallback<AttendanceSession>() {
                    @Override
                    public void onSuccess(AttendanceSession data) {
                        if (data != null) {
                            session = data;
                            bindHeader();
                            renderRecords();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Keep the session already shown from History.
                    }
                });
    }

    private void bindHeader() {
        binding.textClassInfo.setText(HistoryFilter.formatClassLine(session));
        binding.textDate.setText(DateUtils.toDisplayFormat(session.getDate()));
        binding.textSummary.setText(getString(R.string.session_summary_detail,
                session.getTotalCount(), session.getPresentCount(), session.getAbsentCount(),
                session.getLateCount(), session.getLeaveCount(), session.getAttendanceRate()));
    }

    private void renderRecords() {
        List<AttendanceRecord> filtered = HistoryFilter.byStatus(session.getRecords(), statusFilter);
        adapter.submitList(filtered);
        boolean empty = filtered.isEmpty();
        binding.recyclerAttendanceDetails.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.textEmptyFiltered.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private String exportFileName() {
        String date = session.getDate() == null ? "attendance" : session.getDate();
        String code = session.getSubjectCode() == null || session.getSubjectCode().isEmpty()
                ? "class" : session.getSubjectCode();
        return code + "_" + date + ".xlsx";
    }

    private void onExportFileChosen(Uri uri) {
        if (uri == null || session == null) {
            return;
        }
        // Always re-load this exact session from Room before writing Excel so the
        // file cannot accidentally include other dates/sessions.
        RepositoryProvider.getInstance(this).getAttendanceRepository().getSession(
                session.getDepartmentId(), session.getSemesterId(), session.getSectionId(),
                session.getSubjectId(), session.getDate(), session.getLectureNumber(),
                new ApiCallback<AttendanceSession>() {
                    @Override
                    public void onSuccess(AttendanceSession data) {
                        AttendanceSession toExport = data != null ? data : session;
                        excelService.exportSessionDayAttendance(uri, toExport, new ApiCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer count) {
                                ViewUtils.toast(SessionDetailActivity.this,
                                        getString(R.string.export_success_format, count == null ? 0 : count));
                            }

                            @Override
                            public void onError(String message) {
                                ViewUtils.toastLong(SessionDetailActivity.this, message);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        excelService.exportSessionDayAttendance(uri, session, new ApiCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer count) {
                                ViewUtils.toast(SessionDetailActivity.this,
                                        getString(R.string.export_success_format, count == null ? 0 : count));
                            }

                            @Override
                            public void onError(String err) {
                                ViewUtils.toastLong(SessionDetailActivity.this, err);
                            }
                        });
                    }
                });
    }

    private void openEditor(AttendanceSession session) {
        Department department = new Department(session.getDepartmentId(), session.getDepartmentName(), "", 0, null);
        Semester semester = new Semester(session.getSemesterId(), session.getDepartmentId(),
                session.getSemesterName(), 0);
        Section section = new Section(session.getSectionId(), session.getSemesterId(),
                session.getSectionName(), 0);
        Subject subject = new Subject();
        subject.setId(session.getSubjectId());
        subject.setDepartmentId(session.getDepartmentId());
        subject.setName(session.getSubjectName());
        subject.setCode(session.getSubjectCode());
        Intent intent = new Intent(this, MarkAttendanceActivity.class);
        intent.putExtra(Constants.EXTRA_DEPARTMENT, department);
        intent.putExtra(Constants.EXTRA_SEMESTER, semester);
        intent.putExtra(Constants.EXTRA_SECTION, section);
        intent.putExtra(Constants.EXTRA_SUBJECT, subject);
        intent.putExtra(Constants.EXTRA_DATE, session.getDate());
        intent.putExtra(Constants.EXTRA_LECTURE_NUMBER, session.getLectureNumber());
        startActivity(intent);
    }
}
