package com.example.studentattendance.ui.students;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.StudentRosterAdapter;
import com.example.studentattendance.databinding.ActivityStudentsBinding;
import com.example.studentattendance.databinding.DialogAddStudentBinding;
import com.example.studentattendance.model.CsvParseResult;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.ImportResult;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Student;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.attendance.MarkAttendanceActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.ValidationUtils;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Student roster for a chosen class. When opened from Mark Attendance,
 * this is the Manage Students step with Excel import/export and Continue.
 */
public class StudentsActivity extends AppCompatActivity {

    private static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ActivityStudentsBinding binding;
    private StudentsViewModel viewModel;
    private StudentRosterAdapter adapter;

    private Department department;
    private Semester semester;
    private Section section;
    private Subject subject;
    private String date;
    private int lectureNumber;
    private boolean manageStudentsFlow;

    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        department = (Department) getIntent().getSerializableExtra(Constants.EXTRA_DEPARTMENT);
        semester = (Semester) getIntent().getSerializableExtra(Constants.EXTRA_SEMESTER);
        section = (Section) getIntent().getSerializableExtra(Constants.EXTRA_SECTION);
        subject = (Subject) getIntent().getSerializableExtra(Constants.EXTRA_SUBJECT);
        date = getIntent().getStringExtra(Constants.EXTRA_DATE);
        lectureNumber = getIntent().getIntExtra(Constants.EXTRA_LECTURE_NUMBER, 0);
        manageStudentsFlow = true;
        if (department == null || semester == null || section == null) {
            finish();
            return;
        }

        binding.toolbar.setTitle(R.string.title_manage_students);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_manage_students);
        }
        binding.buttonContinueAttendance.setVisibility(View.VISIBLE);
        binding.layoutManageActions.setVisibility(View.VISIBLE);

        StringBuilder subtitle = new StringBuilder();
        subtitle.append(department.displayLabel()).append(" · ").append(semester.getName());
        if (section.getName() != null && !section.getName().trim().isEmpty()) {
            subtitle.append(" · ").append(section.getName().trim());
        }
        if (subject != null) {
            if (subject.getName() != null && !subject.getName().isEmpty()) {
                subtitle.append(" · ").append(subject.getName());
            }
            if (subject.getCode() != null && !subject.getCode().isEmpty()) {
                subtitle.append(" (").append(subject.getCode()).append(")");
            }
        }
        binding.textClassSubtitle.setText(subtitle.toString());

        viewModel = new ViewModelProvider(this).get(StudentsViewModel.class);
        adapter = new StudentRosterAdapter(new StudentRosterAdapter.OnStudentActionListener() {
            @Override
            public void onStudentClicked(Student student) {
                confirmDelete(student);
            }

            @Override
            public void onStudentDelete(Student student) {
                confirmDelete(student);
            }
        });
        binding.recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerStudents.setAdapter(adapter);

        registerLaunchers();

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadStudents);
        binding.includeError.buttonRetry.setOnClickListener(v -> loadStudents());
        binding.fabAddStudent.setOnClickListener(v -> showAddStudentDialog());
        binding.buttonImportExcel.setOnClickListener(v ->
                importLauncher.launch(new String[]{MIME_XLSX, "application/vnd.ms-excel", "*/*"}));
        binding.buttonExportExcel.setOnClickListener(v -> {
            String code = subject != null && subject.getCode() != null && !subject.getCode().isEmpty()
                    ? subject.getCode() : "students";
            exportLauncher.launch("students_" + code + ".xlsx");
        });
        binding.buttonContinueAttendance.setOnClickListener(v -> continueToMarkAttendance());

        viewModel.getStudents().observe(this, this::render);
        viewModel.getAddStudentResult().observe(this, this::handleAddStudentResult);
        viewModel.getExportResult().observe(this, this::handleExportResult);
        viewModel.getParseResult().observe(this, this::handleParseResult);
        viewModel.getImportResult().observe(this, this::handleImportResult);

        loadStudents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null && department != null && semester != null && section != null) {
            loadStudents();
        }
    }

    private void registerLaunchers() {
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(MIME_XLSX),
                uri -> {
                    if (uri != null) {
                        viewModel.exportStudents(uri);
                    }
                });
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        viewModel.parseImport(uri, department.getId(), semester.getId(), section.getId());
                    }
                });
    }

    private void continueToMarkAttendance() {
        if (date == null || date.isEmpty()) {
            date = DateUtils.today();
        }
        if (subject == null || subject.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_subject));
            return;
        }
        Intent intent = new Intent(this, MarkAttendanceActivity.class);
        intent.putExtra(Constants.EXTRA_DEPARTMENT, department);
        intent.putExtra(Constants.EXTRA_SEMESTER, semester);
        intent.putExtra(Constants.EXTRA_SECTION, section);
        intent.putExtra(Constants.EXTRA_SUBJECT, subject);
        intent.putExtra(Constants.EXTRA_DATE, date != null ? date : DateUtils.today());
        startActivity(intent);
    }

    private void loadStudents() {
        viewModel.loadStudents(department.getId(), semester.getId(), section.getId());
    }

    private void render(NetworkResult<List<Student>> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                if (!binding.swipeRefresh.isRefreshing()) {
                    showState(State.LOADING);
                }
                break;
            case SUCCESS:
                binding.swipeRefresh.setRefreshing(false);
                List<Student> data = result.getData();
                if (data == null || data.isEmpty()) {
                    showState(State.EMPTY);
                } else {
                    adapter.submitList(data);
                    showState(State.CONTENT);
                }
                break;
            case ERROR:
                binding.swipeRefresh.setRefreshing(false);
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                break;
        }
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.swipeRefresh.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeEmpty.layoutEmpty.setVisibility(state == State.EMPTY ? View.VISIBLE : View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
        if (state == State.EMPTY) {
            binding.includeEmpty.textEmptyTitle.setText(R.string.students_empty_title);
            binding.includeEmpty.textEmptySubtitle.setText(R.string.students_empty_subtitle);
        }
    }

    private void showAddStudentDialog() {
        DialogAddStudentBinding dialogBinding = DialogAddStudentBinding.inflate(LayoutInflater.from(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateAddStudentForm(dialogBinding)) {
                String name = dialogBinding.editName.getText().toString().trim();
                String roll = dialogBinding.editRoll.getText().toString().trim();
                String email = dialogBinding.editEmail.getText() == null
                        ? "" : dialogBinding.editEmail.getText().toString().trim();
                String phone = dialogBinding.editPhone.getText() == null
                        ? "" : dialogBinding.editPhone.getText().toString().trim();

                Student student = new Student(null, roll, name, department.getId(),
                        semester.getId(), section.getId(), email, phone);
                viewModel.addStudent(student, department.getId(), semester.getId(), section.getId());
                dialog.dismiss();
            }
        }));

        dialog.show();
    }

    private void showStudentActions(Student student) {
        String[] options = manageStudentsFlow
                ? new String[]{getString(R.string.action_delete)}
                : new String[]{
                    getString(R.string.action_edit),
                    getString(R.string.action_transfer),
                    getString(R.string.action_archive),
                    getString(R.string.action_delete)
                };
        new AlertDialog.Builder(this)
                .setTitle(student.getName())
                .setItems(options, (dialog, which) -> {
                    if (manageStudentsFlow) {
                        confirmDelete(student);
                        return;
                    }
                    switch (which) {
                        case 0:
                            showEditStudentDialog(student);
                            break;
                        case 1:
                            showTransferDialog(student);
                            break;
                        case 2:
                            confirmArchive(student);
                            break;
                        case 3:
                            confirmDelete(student);
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    private void showEditStudentDialog(Student student) {
        DialogAddStudentBinding dialogBinding = DialogAddStudentBinding.inflate(LayoutInflater.from(this));
        dialogBinding.editName.setText(student.getName());
        dialogBinding.editRoll.setText(student.getRollNumber());
        dialogBinding.editEmail.setText(student.getEmail());
        dialogBinding.editPhone.setText(student.getPhone());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_student_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!validateAddStudentForm(dialogBinding)) {
                return;
            }
            viewModel.updateStudent(student.getId(),
                    dialogBinding.editName.getText().toString().trim(),
                    dialogBinding.editRoll.getText().toString().trim(),
                    dialogBinding.editEmail.getText().toString().trim(),
                    dialogBinding.editPhone.getText().toString().trim(),
                    student.isArchived(), department.getId(), semester.getId(), section.getId());
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showTransferDialog(Student student) {
        RepositoryProvider.getInstance(this).getDepartmentRepository()
                .getSections(semester.getId(), new com.example.studentattendance.network.ApiCallback<java.util.List<Section>>() {
                    @Override
                    public void onSuccess(java.util.List<Section> sections) {
                        if (sections == null || sections.isEmpty()) {
                            ViewUtils.toast(StudentsActivity.this, getString(R.string.students_empty_title));
                            return;
                        }
                        String[] names = new String[sections.size()];
                        for (int i = 0; i < sections.size(); i++) {
                            names[i] = sections.get(i).getName();
                        }
                        new AlertDialog.Builder(StudentsActivity.this)
                                .setTitle(R.string.action_transfer)
                                .setItems(names, (d, which) -> {
                                    Section dest = sections.get(which);
                                    viewModel.transferStudent(student.getId(), dest.getId(),
                                            department.getId(), semester.getId(), section.getId());
                                    ViewUtils.toast(StudentsActivity.this, getString(R.string.student_transferred));
                                })
                                .setNegativeButton(R.string.action_cancel, null)
                                .show();
                    }

                    @Override
                    public void onError(String message) {
                        ViewUtils.toastLong(StudentsActivity.this, message);
                    }
                });
    }

    private void confirmArchive(Student student) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_archive_student)
                .setPositiveButton(R.string.action_archive, (d, w) ->
                        viewModel.updateStudent(student.getId(), student.getName(), student.getRollNumber(),
                                student.getEmail(), student.getPhone(), true,
                                department.getId(), semester.getId(), section.getId()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDelete(Student student) {
        String name = student.getName() == null ? "" : student.getName();
        String roll = student.getRollNumber() == null ? "" : student.getRollNumber();
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_delete_student_named, name, roll))
                .setPositiveButton(R.string.action_delete, (d, w) ->
                        viewModel.deleteStudent(student.getId(), department.getId(), semester.getId(), section.getId()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private boolean validateAddStudentForm(DialogAddStudentBinding dialogBinding) {
        dialogBinding.inputLayoutName.setError(null);
        dialogBinding.inputLayoutRoll.setError(null);
        dialogBinding.inputLayoutEmail.setError(null);
        dialogBinding.inputLayoutPhone.setError(null);

        boolean valid = true;
        if (!ValidationUtils.isNotEmpty(dialogBinding.editName.getText())) {
            dialogBinding.inputLayoutName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!ValidationUtils.isNotEmpty(dialogBinding.editRoll.getText())) {
            dialogBinding.inputLayoutRoll.setError(getString(R.string.error_roll_required));
            valid = false;
        }
        CharSequence email = dialogBinding.editEmail.getText();
        if (email != null && email.length() > 0 && !ValidationUtils.isValidEmail(email)) {
            dialogBinding.inputLayoutEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        CharSequence phone = dialogBinding.editPhone.getText();
        if (phone != null && phone.length() > 0 && !ValidationUtils.isValidPhone(phone)) {
            dialogBinding.inputLayoutPhone.setError(getString(R.string.error_roll_required));
            valid = false;
        }
        return valid;
    }

    private void handleAddStudentResult(NetworkResult<Student> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isSuccess()) {
            ViewUtils.toast(this, getString(R.string.action_add_student) + " \u2713");
        } else if (result.isError()) {
            ViewUtils.toastLong(this, result.getMessage());
        }
    }

    private void handleExportResult(NetworkResult<Integer> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isSuccess()) {
            ViewUtils.toast(this, getString(R.string.export_success_format, result.getData()));
        } else if (result.isError()) {
            ViewUtils.toastLong(this, result.getMessage());
        }
    }

    private void handleParseResult(NetworkResult<CsvParseResult> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isError()) {
            ViewUtils.toastLong(this, result.getMessage());
            return;
        }
        CsvParseResult parse = result.getData();
        if (parse == null) {
            return;
        }
        if (parse.validStudents.isEmpty()) {
            StringBuilder errors = new StringBuilder(getString(R.string.import_no_valid_rows));
            int shown = Math.min(parse.errors.size(), 10);
            for (int i = 0; i < shown; i++) {
                errors.append("\n").append(parse.errors.get(i));
            }
            ViewUtils.toastLong(this, errors.toString());
            return;
        }
        StringBuilder body = new StringBuilder();
        body.append(getString(R.string.import_preview_format,
                parse.validStudents.size(), parse.invalidCount, parse.duplicateCount));
        if (!parse.errors.isEmpty()) {
            body.append("\n\n").append(getString(R.string.import_blocked_errors)).append("\n");
            int shown = Math.min(parse.errors.size(), 20);
            for (int i = 0; i < shown; i++) {
                body.append("\n").append(parse.errors.get(i));
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.import_preview_title)
                .setMessage(body.toString())
                .setPositiveButton(R.string.import_confirm, (d, w) ->
                        viewModel.confirmImport(department.getId(), semester.getId(), section.getId()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void handleImportResult(NetworkResult<ImportResult> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isError()) {
            ViewUtils.toastLong(this, result.getMessage());
            return;
        }
        ImportResult data = result.getData();
        if (data == null) {
            return;
        }
        ViewUtils.toastLong(this, getString(R.string.import_result_format,
                data.getImported(), data.getDuplicates(), data.getInvalid()));
    }
}
