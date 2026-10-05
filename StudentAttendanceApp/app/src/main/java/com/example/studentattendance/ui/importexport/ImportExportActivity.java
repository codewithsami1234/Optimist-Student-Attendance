package com.example.studentattendance.ui.importexport;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ActivityImportExportBinding;
import com.example.studentattendance.databinding.ViewIoActionRowBinding;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.ImportRowError;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.ViewUtils;

public class ImportExportActivity extends AppCompatActivity {

    private static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ActivityImportExportBinding binding;
    private ImportExportViewModel viewModel;

    private ActivityResultLauncher<String> exportExcelLauncher;
    private ActivityResultLauncher<String[]> importExcelLauncher;
    private boolean announceProfileExport;
    private AttendanceExportRows.ReportMeta pendingExportMeta;
    private List<AttendanceSession> pendingExportSessions;
    private Map<String, Integer> pendingExportDeltas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImportExportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ImportExportViewModel.class);
        setupCard(binding.cardImportExcel, R.drawable.ic_import, R.string.action_import_excel_sheet);
        setupCard(binding.cardExportExcel, R.drawable.ic_export, R.string.action_export_excel_sheet);
        registerLaunchers();

        binding.cardImportExcel.getRoot().setOnClickListener(v ->
                importExcelLauncher.launch(new String[]{
                        MIME_XLSX,
                        "application/vnd.ms-excel",
                        "*/*"
                }));
        binding.cardExportExcel.getRoot().setOnClickListener(v -> startProfileExport());

        viewModel.getOperationResult().observe(this, this::renderCountResult);
        viewModel.getImportResult().observe(this, this::renderImportResult);
    }

    private void setupCard(ViewIoActionRowBinding card, int icon, int title) {
        card.imageActionIcon.setImageResource(icon);
        card.textActionTitle.setText(title);
    }

    private void registerLaunchers() {
        exportExcelLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(MIME_XLSX),
                uri -> {
                    if (uri == null || pendingExportMeta == null || pendingExportSessions == null) {
                        announceProfileExport = false;
                        clearPendingExport();
                        return;
                    }
                    viewModel.writeConsolidatedExport(uri, pendingExportMeta,
                            pendingExportSessions, pendingExportDeltas);
                    clearPendingExport();
                });
        importExcelLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        viewModel.importAcademicExcel(uri);
                    }
                });
    }

    void launchProfileExport(AttendanceExportRows.ReportMeta meta, List<AttendanceSession> sessions,
                             Map<String, Integer> deltas, String fileName) {
        pendingExportMeta = meta;
        pendingExportSessions = sessions;
        pendingExportDeltas = deltas;
        announceProfileExport = true;
        exportExcelLauncher.launch(fileName);
    }

    private void startProfileExport() {
        binding.progressImportExport.setVisibility(View.VISIBLE);
        viewModel.fetchClassSetups(new ApiCallback<List<ClassSetup>>() {
            @Override
            public void onSuccess(List<ClassSetup> data) {
                binding.progressImportExport.setVisibility(View.GONE);
                if (data == null || data.isEmpty()) {
                    showExportNotice(getString(R.string.export_no_data_title),
                            getString(R.string.export_profile_no_classes));
                    return;
                }
                ProfileExportDialog.show(ImportExportActivity.this, viewModel, data);
            }

            @Override
            public void onError(String message) {
                binding.progressImportExport.setVisibility(View.GONE);
                showExportNotice(getString(R.string.export_no_data_title), message);
            }
        });
    }

    private void clearPendingExport() {
        pendingExportMeta = null;
        pendingExportSessions = null;
        pendingExportDeltas = null;
    }

    private void showExportNotice(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void renderImportResult(@Nullable NetworkResult<ImportResult> result) {
        if (result == null) {
            return;
        }
        if (result.isLoading()) {
            binding.progressImportExport.setVisibility(View.VISIBLE);
            return;
        }
        binding.progressImportExport.setVisibility(View.GONE);
        if (result.isError()) {
            showMessage(result.getMessage(), true);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.import_failed_title)
                    .setMessage(result.getMessage())
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        ImportResult data = result.getData();
        if (data == null) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        msg.append("Total rows: ").append(data.getTotalRows());
        msg.append("\nValid rows: ").append(data.getValidRows());
        msg.append("\nDepartments created: ").append(data.getDepartmentsCreated());
        msg.append("\nDepartments reused: ").append(data.getDepartmentsReused());
        msg.append("\nSemesters created: ").append(data.getSemestersCreated());
        msg.append("\nSemesters reused: ").append(data.getSemestersReused());
        msg.append("\nSections created: ").append(data.getSectionsCreated());
        msg.append("\nSections reused: ").append(data.getSectionsReused());
        msg.append("\nCourses created: ").append(data.getCoursesCreated());
        msg.append("\nCourses reused: ").append(data.getCoursesReused());
        msg.append("\nStudents imported: ").append(data.getImported());
        msg.append("\nDuplicate students skipped: ").append(data.getDuplicates());
        msg.append("\nInvalid rows: ").append(data.getInvalid());
        if (data.getErrors() != null && !data.getErrors().isEmpty()) {
            msg.append("\n");
            int shown = Math.min(data.getErrors().size(), 12);
            for (int i = 0; i < shown; i++) {
                ImportRowError error = data.getErrors().get(i);
                msg.append("\n").append(error == null ? "" : error.toString());
            }
            if (data.getErrors().size() > shown) {
                msg.append("\n… ").append(data.getErrors().size() - shown).append(" more");
            }
        }
        int title = data.getInvalid() > 0
                ? (data.getValidRows() > 0
                ? R.string.import_completed_warnings_title
                : R.string.import_failed_title)
                : R.string.import_completed_title;
        showMessage(msg.toString(), data.getValidRows() == 0 && data.getInvalid() > 0);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void renderCountResult(@Nullable NetworkResult<Integer> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                binding.progressImportExport.setVisibility(View.VISIBLE);
                binding.textResultMessage.setVisibility(View.GONE);
                break;
            case SUCCESS:
                binding.progressImportExport.setVisibility(View.GONE);
                if (announceProfileExport) {
                    announceProfileExport = false;
                    showMessage(getString(R.string.export_excel_success_message), false);
                    showExportNotice(getString(R.string.export_excel_success_title),
                            getString(R.string.export_excel_success_message));
                    break;
                }
                showMessage(getString(R.string.export_success_format, result.getData()), false);
                break;
            case ERROR:
                binding.progressImportExport.setVisibility(View.GONE);
                announceProfileExport = false;
                showMessage(result.getMessage(), true);
                break;
        }
    }

    private void showMessage(String message, boolean error) {
        binding.textResultMessage.setVisibility(View.VISIBLE);
        binding.textResultMessage.setTextColor(getResources().getColor(
                error ? R.color.error : R.color.status_present, null));
        binding.textResultMessage.setText(message);
        if (error) {
            ViewUtils.toastLong(this, message);
        }
    }
}
