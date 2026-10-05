package com.example.studentattendance.ui.history;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.AttendanceHistoryAdapter;
import com.example.studentattendance.adapter.SemesterReviewAdapter;
import com.example.studentattendance.databinding.DialogExportAttendanceBinding;
import com.example.studentattendance.databinding.DialogHistoryFilterBinding;
import com.example.studentattendance.databinding.DialogSemesterReviewBinding;
import com.example.studentattendance.databinding.FragmentHistoryBinding;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.service.ExcelService;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.HistoryFilterOptions;
import com.example.studentattendance.utils.SemesterAttendance;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Shows every saved attendance session (most recent first) with filters for
 * department, semester, section, course, course code, and date range.
 */
public class AttendanceHistoryFragment extends Fragment {

    private static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private enum ExportMode { FILTERED_REPORT }

    private FragmentHistoryBinding binding;
    private AttendanceHistoryViewModel viewModel;
    private AttendanceHistoryAdapter adapter;
    private ExcelService excelService;

    private String pendingFromDate;
    private String pendingToDate;
    /** Export dialog dates — kept separate from history filter date state. */
    private String exportFromDate;
    private String exportToDate;
    private ExportMode pendingExport;
    private List<AttendanceSession> pendingReportSessions;
    private AttendanceExportRows.ReportMeta pendingReportMeta;
    private java.util.Map<String, Integer> pendingReportAdjustments;
    private boolean exportGenerating;

    private ActivityResultLauncher<String> createExportFileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AttendanceHistoryViewModel.class);
        excelService = RepositoryProvider.getInstance(requireContext()).getExcelService();

        adapter = new AttendanceHistoryAdapter(this::openSessionDetail);
        binding.recyclerSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSessions.setAdapter(adapter);

        createExportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(MIME_XLSX), this::onExportFileChosen);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSessions());
        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.loadSessions());
        View.OnClickListener openFilters = v -> showFilterDialog();
        binding.chipDepartmentFilter.setOnClickListener(openFilters);
        binding.chipSemesterFilter.setOnClickListener(openFilters);
        binding.chipSectionFilter.setOnClickListener(openFilters);
        binding.chipSubjectFilter.setOnClickListener(openFilters);
        binding.chipCourseCodeFilter.setOnClickListener(openFilters);
        binding.chipDateRangeFilter.setOnClickListener(openFilters);
        binding.buttonClearFilters.setOnClickListener(v -> {
            viewModel.clearFilters();
            updateFilterChips();
        });
        binding.buttonExportExcel.setOnClickListener(v -> startFilteredExport());
        binding.buttonBelow75.setOnClickListener(v -> viewModel.loadSemesterReview());

        viewModel.getSessions().observe(getViewLifecycleOwner(), this::render);
        viewModel.getClassSetups().observe(getViewLifecycleOwner(), setups -> updateFilterChips());
        viewModel.getSemesterReview().observe(getViewLifecycleOwner(), this::renderSemesterReview);

        viewModel.loadFilterOptions();
        viewModel.loadSessions();
        updateFilterChips();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadFilterOptions();
            viewModel.loadSessions();
        }
    }

    private void openSessionDetail(AttendanceSession session) {
        Intent intent = new Intent(requireContext(), SessionDetailActivity.class);
        intent.putExtra(Constants.EXTRA_SESSION, session);
        startActivity(intent);
    }

    private void showFilterDialog() {
        DialogHistoryFilterBinding dialogBinding = DialogHistoryFilterBinding.inflate(LayoutInflater.from(requireContext()));
        HistoryFilter filter = viewModel.getFilter();
        final String[] departmentId = { filter.departmentId };
        final String[] semesterId = { filter.semesterId };
        final String[] sectionId = { filter.sectionId };
        final String[] subjectId = { filter.subjectId };
        final String[] courseCode = { filter.courseCode };

        bindFilterDropdowns(dialogBinding, departmentId, semesterId, sectionId, subjectId, courseCode);

        pendingFromDate = filter.fromDate;
        pendingToDate = filter.toDate;
        updateFilterDateLabels(dialogBinding);
        dialogBinding.cardFromDate.setOnClickListener(v -> pickDate(true, dialogBinding));
        dialogBinding.cardToDate.setOnClickListener(v -> pickDate(false, dialogBinding));

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_apply_filter, (dialog, which) -> {
                    viewModel.applyFilters(departmentId[0], semesterId[0], sectionId[0],
                            subjectId[0], courseCode[0], pendingFromDate, pendingToDate);
                    updateFilterChips();
                })
                .setNeutralButton(R.string.action_clear_filter, (dialog, which) -> {
                    viewModel.clearFilters();
                    updateFilterChips();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void bindFilterDropdowns(DialogHistoryFilterBinding dialogBinding,
                                     String[] departmentId, String[] semesterId,
                                     String[] sectionId, String[] subjectId, String[] courseCode) {
        HistoryFilterOptions.Snapshot snapshot = viewModel.options(departmentId[0], semesterId[0], sectionId[0]);
        populateDropdown(dialogBinding.dropdownDepartment, snapshot.departments, departmentId[0]);
        populateDropdown(dialogBinding.dropdownSemester, snapshot.semesters, semesterId[0]);
        populateDropdown(dialogBinding.dropdownSection, snapshot.sections, sectionId[0]);
        populateDropdown(dialogBinding.dropdownSubject, snapshot.subjects, subjectId[0]);
        populateDropdown(dialogBinding.dropdownCourseCode, snapshot.courseCodes, courseCode[0]);

        dialogBinding.dropdownDepartment.setOnItemClickListener((p, v, pos, id) -> {
            departmentId[0] = optionId(dialogBinding.dropdownDepartment, pos);
            semesterId[0] = null;
            sectionId[0] = null;
            subjectId[0] = null;
            courseCode[0] = null;
            bindChildDropdowns(dialogBinding, departmentId[0], null, null, semesterId, sectionId, subjectId, courseCode);
        });
        dialogBinding.dropdownSemester.setOnItemClickListener((p, v, pos, id) -> {
            semesterId[0] = optionId(dialogBinding.dropdownSemester, pos);
            sectionId[0] = null;
            subjectId[0] = null;
            courseCode[0] = null;
            bindChildDropdowns(dialogBinding, departmentId[0], semesterId[0], null,
                    semesterId, sectionId, subjectId, courseCode);
        });
        dialogBinding.dropdownSection.setOnItemClickListener((p, v, pos, id) -> {
            sectionId[0] = optionId(dialogBinding.dropdownSection, pos);
            subjectId[0] = null;
            courseCode[0] = null;
            bindChildDropdowns(dialogBinding, departmentId[0], semesterId[0], sectionId[0],
                    semesterId, sectionId, subjectId, courseCode);
        });
        dialogBinding.dropdownSubject.setOnItemClickListener((p, v, pos, id) ->
                subjectId[0] = optionId(dialogBinding.dropdownSubject, pos));
        dialogBinding.dropdownCourseCode.setOnItemClickListener((p, v, pos, id) ->
                courseCode[0] = optionId(dialogBinding.dropdownCourseCode, pos));
    }

    private void bindChildDropdowns(DialogHistoryFilterBinding dialogBinding,
                                    String departmentId, String semesterId, String sectionId,
                                    String[] semesterHolder, String[] sectionHolder,
                                    String[] subjectHolder, String[] courseHolder) {
        HistoryFilterOptions.Snapshot snapshot = viewModel.options(departmentId, semesterId, sectionId);
        populateDropdown(dialogBinding.dropdownSemester, snapshot.semesters, semesterHolder[0]);
        populateDropdown(dialogBinding.dropdownSection, snapshot.sections, sectionHolder[0]);
        populateDropdown(dialogBinding.dropdownSubject, snapshot.subjects, subjectHolder[0]);
        populateDropdown(dialogBinding.dropdownCourseCode, snapshot.courseCodes, courseHolder[0]);
        dialogBinding.dropdownSemester.setOnItemClickListener((p, v, pos, id) -> {
            semesterHolder[0] = optionId(dialogBinding.dropdownSemester, pos);
            sectionHolder[0] = null;
            subjectHolder[0] = null;
            courseHolder[0] = null;
            bindChildDropdowns(dialogBinding, departmentId, semesterHolder[0], null,
                    semesterHolder, sectionHolder, subjectHolder, courseHolder);
        });
        dialogBinding.dropdownSection.setOnItemClickListener((p, v, pos, id) -> {
            sectionHolder[0] = optionId(dialogBinding.dropdownSection, pos);
            subjectHolder[0] = null;
            courseHolder[0] = null;
            bindChildDropdowns(dialogBinding, departmentId, semesterHolder[0], sectionHolder[0],
                    semesterHolder, sectionHolder, subjectHolder, courseHolder);
        });
        dialogBinding.dropdownSubject.setOnItemClickListener((p, v, pos, id) ->
                subjectHolder[0] = optionId(dialogBinding.dropdownSubject, pos));
        dialogBinding.dropdownCourseCode.setOnItemClickListener((p, v, pos, id) ->
                courseHolder[0] = optionId(dialogBinding.dropdownCourseCode, pos));
    }

    @SuppressWarnings("unchecked")
    private String optionId(AutoCompleteTextView view, int pos) {
        Object tag = view.getTag();
        if (!(tag instanceof List)) {
            return null;
        }
        List<HistoryFilterOptions.Option> items = (List<HistoryFilterOptions.Option>) tag;
        if (pos < 0 || pos >= items.size()) {
            return null;
        }
        return items.get(pos).id;
    }

    private void populateDropdown(AutoCompleteTextView view, List<HistoryFilterOptions.Option> values,
                                  String selectedId) {
        List<HistoryFilterOptions.Option> items = new ArrayList<>();
        items.add(new HistoryFilterOptions.Option(null, getString(R.string.filter_all)));
        if (values != null) {
            items.addAll(values);
        }
        ArrayAdapter<HistoryFilterOptions.Option> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, items);
        view.setAdapter(adapter);
        HistoryFilterOptions.Option selected = items.get(0);
        if (selectedId != null) {
            for (HistoryFilterOptions.Option item : items) {
                if (selectedId.equals(item.id)) {
                    selected = item;
                    break;
                }
            }
        }
        view.setText(selected.label, false);
        view.setTag(items);
    }

    private void updateFilterChips() {
        if (binding == null || viewModel == null) {
            return;
        }
        HistoryFilter filter = viewModel.getFilter();
        HistoryFilterOptions.Snapshot all = viewModel.options(null, null, null);
        setChip(binding.chipDepartmentFilter, HistoryFilterOptions.labelFor(all.departments, filter.departmentId),
                R.string.filter_by_department, filter.departmentId != null);
        setChip(binding.chipSemesterFilter, HistoryFilterOptions.labelFor(all.semesters, filter.semesterId),
                R.string.filter_by_semester, filter.semesterId != null);
        setChip(binding.chipSectionFilter, HistoryFilterOptions.labelFor(all.sections, filter.sectionId),
                R.string.filter_by_section, filter.sectionId != null);
        HistoryFilterOptions.Snapshot scoped = viewModel.options(filter.departmentId, filter.semesterId, filter.sectionId);
        setChip(binding.chipSubjectFilter, HistoryFilterOptions.labelFor(scoped.subjects, filter.subjectId),
                R.string.filter_by_subject, filter.subjectId != null);
        setChip(binding.chipCourseCodeFilter, filter.courseCode, R.string.filter_by_course_code,
                filter.courseCode != null);
        if (filter.fromDate != null || filter.toDate != null) {
            binding.chipDateRangeFilter.setText(
                    (filter.fromDate == null ? "…" : DateUtils.toDisplayFormat(filter.fromDate))
                            + " – "
                            + (filter.toDate == null ? "…" : DateUtils.toDisplayFormat(filter.toDate)));
            binding.chipDateRangeFilter.setChecked(true);
        } else {
            binding.chipDateRangeFilter.setText(R.string.filter_from_date);
            binding.chipDateRangeFilter.setChecked(false);
        }
    }

    private void setChip(com.google.android.material.chip.Chip chip, String label, int defaultRes, boolean active) {
        chip.setText(label == null || label.isEmpty() ? getString(defaultRes) : label);
        chip.setChecked(active);
    }

    private void pickDate(boolean isFromDate, DialogHistoryFilterBinding dialogBinding) {
        String existing = isFromDate ? pendingFromDate : pendingToDate;
        Calendar calendar = existing != null ? DateUtils.toCalendar(existing) : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = DateUtils.fromYearMonthDay(year, month, dayOfMonth);
                    if (isFromDate) {
                        pendingFromDate = date;
                    } else {
                        pendingToDate = date;
                    }
                    updateFilterDateLabels(dialogBinding);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateFilterDateLabels(DialogHistoryFilterBinding dialogBinding) {
        dialogBinding.textFromDate.setText(pendingFromDate == null
                ? getString(R.string.filter_from_date)
                : "From: " + DateUtils.toDisplayFormat(pendingFromDate));
        dialogBinding.textToDate.setText(pendingToDate == null
                ? getString(R.string.filter_to_date)
                : "To: " + DateUtils.toDisplayFormat(pendingToDate));
    }

    private void startFilteredExport() {
        showExportAttendanceDialog();
    }

    private void showExportAttendanceDialog() {
        if (viewModel == null) {
            return;
        }
        viewModel.loadFilterOptions();
        DialogExportAttendanceBinding dialogBinding =
                DialogExportAttendanceBinding.inflate(LayoutInflater.from(requireContext()));
        HistoryFilter current = viewModel.getFilter();
        final String[] departmentId = { current.departmentId };
        final String[] shift = { current.shift };
        final String[] sectionId = { current.sectionId };
        final String[] subjectId = { current.subjectId };
        final String[] courseCode = { current.courseCode };
        exportFromDate = current.fromDate;
        exportToDate = current.toDate;
        updateExportDateLabels(dialogBinding);
        bindExportDropdowns(dialogBinding, departmentId, shift, sectionId, subjectId, courseCode);
        dialogBinding.cardFromDate.setOnClickListener(v -> pickExportDate(true, dialogBinding));
        dialogBinding.cardToDate.setOnClickListener(v -> pickExportDate(false, dialogBinding));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_generate_excel, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            android.widget.Button generate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button cancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            generate.setOnClickListener(v -> {
                if (exportGenerating) {
                    return;
                }
                dialogBinding.textExportError.setVisibility(View.GONE);
                String derivedSemesterId = semesterIdForSection(sectionId[0]);
                String error = validateExportSelection(departmentId[0], exportFromDate, exportToDate);
                if (error != null) {
                    dialogBinding.textExportError.setVisibility(View.VISIBLE);
                    dialogBinding.textExportError.setText(error);
                    return;
                }
                exportGenerating = true;
                generate.setEnabled(false);
                cancel.setEnabled(false);
                generate.setText(R.string.action_generating_excel);
                dialogBinding.progressExport.setVisibility(View.VISIBLE);

                HistoryFilterOptions.Snapshot labels = viewModel.options(
                        departmentId[0], derivedSemesterId, sectionId[0]);
                AttendanceExportRows.ReportMeta meta = new AttendanceExportRows.ReportMeta();
                meta.departmentName = HistoryFilterOptions.labelFor(labels.departments, departmentId[0]);
                if (shift[0] != null) {
                    meta.shiftName = com.example.studentattendance.model.DepartmentShift.label(shift[0]);
                }
                meta.semesterName = semesterNameForSection(sectionId[0]);
                meta.sectionName = sectionId[0] == null
                        ? "" : HistoryFilterOptions.labelFor(labels.sections, sectionId[0]);
                meta.courseName = subjectId[0] == null ? "" : courseNameForSubject(subjectId[0]);
                meta.courseCode = courseCode[0] != null
                        ? courseCode[0]
                        : (subjectId[0] == null ? "" : courseCodeForSubject(subjectId[0]));
                meta.fromDate = exportFromDate;
                meta.toDate = exportToDate;

                viewModel.loadSessionsForExport(departmentId[0], derivedSemesterId, sectionId[0],
                        subjectId[0], courseCode[0], shift[0], exportFromDate, exportToDate,
                        new ApiCallback<List<AttendanceSession>>() {
                            @Override
                            public void onSuccess(List<AttendanceSession> sessions) {
                                if (!isAdded()) {
                                    exportGenerating = false;
                                    return;
                                }
                                if (sessions == null || sessions.isEmpty()
                                        || !hasExportableAttendance(sessions)) {
                                    exportGenerating = false;
                                    generate.setEnabled(true);
                                    cancel.setEnabled(true);
                                    generate.setText(R.string.action_generate_excel);
                                    dialogBinding.progressExport.setVisibility(View.GONE);
                                    dialogBinding.textExportError.setVisibility(View.VISIBLE);
                                    dialogBinding.textExportError.setText(
                                            R.string.export_error_no_records);
                                    return;
                                }
                                viewModel.loadAdjustmentDeltas(new ApiCallback<java.util.Map<String, Integer>>() {
                                    @Override
                                    public void onSuccess(java.util.Map<String, Integer> deltas) {
                                        if (!isAdded()) {
                                            exportGenerating = false;
                                            return;
                                        }
                                        pendingExport = ExportMode.FILTERED_REPORT;
                                        pendingReportSessions = sessions;
                                        pendingReportMeta = meta;
                                        pendingReportAdjustments = deltas;
                                        String fileName = AttendanceExportRows.buildReportFileName(
                                                meta.departmentName, meta.semesterName, meta.sectionName,
                                                meta.courseCode, meta.fromDate, meta.toDate);
                                        dialog.dismiss();
                                        exportGenerating = false;
                                        createExportFileLauncher.launch(fileName);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        onSuccess(new java.util.HashMap<>());
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                if (!isAdded()) {
                                    exportGenerating = false;
                                    return;
                                }
                                exportGenerating = false;
                                generate.setEnabled(true);
                                cancel.setEnabled(true);
                                generate.setText(R.string.action_generate_excel);
                                dialogBinding.progressExport.setVisibility(View.GONE);
                                dialogBinding.textExportError.setVisibility(View.VISIBLE);
                                dialogBinding.textExportError.setText(message == null
                                        ? getString(R.string.export_error_no_records) : message);
                            }
                        });
            });
        });
        dialog.show();
    }

    /**
     * True when at least one student has a marked status (not only NOT_MARKED).
     * Matches {@link SemesterAttendance#reviews} / consolidated report inclusion.
     */
    private boolean hasExportableAttendance(List<AttendanceSession> sessions) {
        return !SemesterAttendance.reviews(sessions, null, AttendancePolicy.DEFAULT_LOW_THRESHOLD)
                .isEmpty();
    }

    private com.example.studentattendance.model.Subject findSubject(String subjectId) {
        if (subjectId == null) {
            return null;
        }
        List<?> setups = viewModel.getClassSetups().getValue();
        if (setups == null) {
            return null;
        }
        for (Object item : setups) {
            if (!(item instanceof com.example.studentattendance.model.ClassSetup)) {
                continue;
            }
            com.example.studentattendance.model.ClassSetup setup =
                    (com.example.studentattendance.model.ClassSetup) item;
            if (setup.getSubject() != null && subjectId.equals(setup.getSubject().getId())) {
                return setup.getSubject();
            }
        }
        return null;
    }

    private com.example.studentattendance.model.ClassSetup findSetupForSection(String sectionId) {
        if (sectionId == null) {
            return null;
        }
        List<?> setups = viewModel.getClassSetups().getValue();
        if (setups == null) {
            return null;
        }
        for (Object item : setups) {
            if (!(item instanceof com.example.studentattendance.model.ClassSetup)) {
                continue;
            }
            com.example.studentattendance.model.ClassSetup setup =
                    (com.example.studentattendance.model.ClassSetup) item;
            if (setup.getSection() != null && sectionId.equals(setup.getSection().getId())) {
                return setup;
            }
        }
        return null;
    }

    private String semesterIdForSection(String sectionId) {
        com.example.studentattendance.model.ClassSetup setup = findSetupForSection(sectionId);
        if (setup != null && setup.getSemester() != null) {
            return setup.getSemester().getId();
        }
        return null;
    }

    private String semesterNameForSection(String sectionId) {
        com.example.studentattendance.model.ClassSetup setup = findSetupForSection(sectionId);
        if (setup != null) {
            return setup.semesterLabel();
        }
        return "";
    }

    private String courseNameForSubject(String subjectId) {
        com.example.studentattendance.model.Subject subject = findSubject(subjectId);
        if (subject != null && subject.getName() != null && !subject.getName().isEmpty()) {
            return subject.getName();
        }
        HistoryFilterOptions.Snapshot snapshot = viewModel.options(null, null, null);
        String label = HistoryFilterOptions.labelFor(snapshot.subjects, subjectId);
        return label == null ? "" : label;
    }

    private String courseCodeForSubject(String subjectId) {
        com.example.studentattendance.model.Subject subject = findSubject(subjectId);
        if (subject != null) {
            return subject.getCode() == null ? "" : subject.getCode();
        }
        return "";
    }

    private String validateExportSelection(String departmentId, String fromDate, String toDate) {
        if (departmentId == null || departmentId.isEmpty()) {
            return getString(R.string.export_error_select_department);
        }
        if (fromDate == null || fromDate.isEmpty()) {
            return getString(R.string.export_error_select_from_date);
        }
        if (toDate == null || toDate.isEmpty()) {
            return getString(R.string.export_error_select_to_date);
        }
        if (fromDate.compareTo(toDate) > 0) {
            return getString(R.string.export_error_date_order);
        }
        return null;
    }

    private void bindExportDropdowns(DialogExportAttendanceBinding dialogBinding,
                                     String[] departmentId, String[] shift,
                                     String[] sectionId, String[] subjectId, String[] courseCode) {
        HistoryFilterOptions.Snapshot all = viewModel.options(null, null, null);
        departmentId[0] = populateRequiredDropdown(dialogBinding.dropdownDepartment,
                all.departments, departmentId[0]);
        shift[0] = populateOptionalShiftDropdown(dialogBinding.dropdownShift, shift[0]);
        refreshExportChildren(dialogBinding, departmentId[0], sectionId, subjectId, courseCode);

        dialogBinding.dropdownDepartment.setOnItemClickListener((p, v, pos, id) -> {
            departmentId[0] = requiredOptionId(dialogBinding.dropdownDepartment, pos);
            sectionId[0] = null;
            subjectId[0] = null;
            courseCode[0] = null;
            refreshExportChildren(dialogBinding, departmentId[0], sectionId, subjectId, courseCode);
        });
        dialogBinding.dropdownShift.setOnItemClickListener((p, v, pos, id) ->
                shift[0] = optionalOptionId(dialogBinding.dropdownShift, pos));
        dialogBinding.dropdownSection.setOnItemClickListener((p, v, pos, id) -> {
            sectionId[0] = optionalOptionId(dialogBinding.dropdownSection, pos);
            subjectId[0] = null;
            courseCode[0] = null;
            refreshExportChildren(dialogBinding, departmentId[0], sectionId, subjectId, courseCode);
        });
        dialogBinding.dropdownCourse.setOnItemClickListener((p, v, pos, id) -> {
            subjectId[0] = optionalOptionId(dialogBinding.dropdownCourse, pos);
            if (subjectId[0] != null && courseCode[0] == null) {
                String code = courseCodeForSubject(subjectId[0]);
                if (code != null && !code.isEmpty()) {
                    courseCode[0] = HistoryFilter.normalizeCode(code);
                    HistoryFilterOptions.Snapshot scoped = viewModel.options(
                            departmentId[0], semesterIdForSection(sectionId[0]), sectionId[0]);
                    populateDropdown(dialogBinding.dropdownCourseCode, scoped.courseCodes, courseCode[0]);
                }
            }
        });
        dialogBinding.dropdownCourseCode.setOnItemClickListener((p, v, pos, id) ->
                courseCode[0] = optionalOptionId(dialogBinding.dropdownCourseCode, pos));
    }

    /**
     * Cascades optional section → course → course code. Semester is derived
     * from the selected section when present.
     */
    private void refreshExportChildren(DialogExportAttendanceBinding dialogBinding,
                                       String departmentId,
                                       String[] sectionHolder, String[] subjectHolder,
                                       String[] courseCodeHolder) {
        HistoryFilterOptions.Snapshot forDepartment = viewModel.options(departmentId, null, null);
        populateDropdown(dialogBinding.dropdownSection, forDepartment.sections, sectionHolder[0]);
        // Keep selected section only if still present (populateDropdown sets text; sync holder).
        sectionHolder[0] = selectedOptionalId(dialogBinding.dropdownSection, sectionHolder[0]);

        String semesterId = semesterIdForSection(sectionHolder[0]);
        HistoryFilterOptions.Snapshot forSection =
                viewModel.options(departmentId, semesterId, sectionHolder[0]);
        populateDropdown(dialogBinding.dropdownCourse, forSection.subjects, subjectHolder[0]);
        subjectHolder[0] = selectedOptionalId(dialogBinding.dropdownCourse, subjectHolder[0]);
        populateDropdown(dialogBinding.dropdownCourseCode, forSection.courseCodes, courseCodeHolder[0]);
        courseCodeHolder[0] = selectedOptionalId(dialogBinding.dropdownCourseCode, courseCodeHolder[0]);

        dialogBinding.dropdownSection.setOnItemClickListener((p, v, pos, id) -> {
            sectionHolder[0] = optionalOptionId(dialogBinding.dropdownSection, pos);
            subjectHolder[0] = null;
            courseCodeHolder[0] = null;
            refreshExportChildren(dialogBinding, departmentId, sectionHolder, subjectHolder,
                    courseCodeHolder);
        });
        dialogBinding.dropdownCourse.setOnItemClickListener((p, v, pos, id) ->
                subjectHolder[0] = optionalOptionId(dialogBinding.dropdownCourse, pos));
        dialogBinding.dropdownCourseCode.setOnItemClickListener((p, v, pos, id) ->
                courseCodeHolder[0] = optionalOptionId(dialogBinding.dropdownCourseCode, pos));
    }

    private String populateOptionalShiftDropdown(AutoCompleteTextView view, String selectedShift) {
        List<HistoryFilterOptions.Option> items = new ArrayList<>();
        items.add(new HistoryFilterOptions.Option(null, getString(R.string.filter_all)));
        items.add(new HistoryFilterOptions.Option(
                com.example.studentattendance.model.DepartmentShift.MORNING,
                com.example.studentattendance.model.DepartmentShift.label(
                        com.example.studentattendance.model.DepartmentShift.MORNING)));
        items.add(new HistoryFilterOptions.Option(
                com.example.studentattendance.model.DepartmentShift.EVENING,
                com.example.studentattendance.model.DepartmentShift.label(
                        com.example.studentattendance.model.DepartmentShift.EVENING)));
        ArrayAdapter<HistoryFilterOptions.Option> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, items);
        view.setAdapter(adapter);
        view.setTag(items);
        HistoryFilterOptions.Option selected = items.get(0);
        String normalized = HistoryFilter.normalizeShift(selectedShift);
        if (normalized != null) {
            for (HistoryFilterOptions.Option item : items) {
                if (normalized.equals(item.id)) {
                    selected = item;
                    break;
                }
            }
        }
        view.setText(selected.label, false);
        return selected.id;
    }

    @SuppressWarnings("unchecked")
    private String optionalOptionId(AutoCompleteTextView view, int pos) {
        Object tag = view.getTag();
        if (!(tag instanceof List)) {
            return null;
        }
        List<HistoryFilterOptions.Option> items = (List<HistoryFilterOptions.Option>) tag;
        if (pos < 0 || pos >= items.size()) {
            return null;
        }
        return items.get(pos).id;
    }

    @SuppressWarnings("unchecked")
    private String selectedOptionalId(AutoCompleteTextView view, String preferredId) {
        Object tag = view.getTag();
        if (!(tag instanceof List)) {
            return null;
        }
        List<HistoryFilterOptions.Option> items = (List<HistoryFilterOptions.Option>) tag;
        if (preferredId != null) {
            for (HistoryFilterOptions.Option item : items) {
                if (preferredId.equals(item.id)) {
                    return preferredId;
                }
            }
        }
        String label = view.getText() == null ? "" : view.getText().toString();
        for (HistoryFilterOptions.Option item : items) {
            if (label.equals(item.label)) {
                return item.id;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String requiredOptionId(AutoCompleteTextView view, int pos) {
        Object tag = view.getTag();
        if (!(tag instanceof List)) {
            return null;
        }
        List<HistoryFilterOptions.Option> items = (List<HistoryFilterOptions.Option>) tag;
        if (pos < 0 || pos >= items.size()) {
            return null;
        }
        return items.get(pos).id;
    }

    private String populateRequiredDropdown(AutoCompleteTextView view,
                                            List<HistoryFilterOptions.Option> values,
                                            String selectedId) {
        List<HistoryFilterOptions.Option> items = new ArrayList<>();
        if (values != null) {
            items.addAll(values);
        }
        ArrayAdapter<HistoryFilterOptions.Option> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, items);
        view.setAdapter(adapter);
        view.setTag(items);
        if (items.isEmpty()) {
            view.setText("", false);
            return null;
        }
        HistoryFilterOptions.Option selected = null;
        if (selectedId != null) {
            for (HistoryFilterOptions.Option item : items) {
                if (selectedId.equals(item.id)) {
                    selected = item;
                    break;
                }
            }
        }
        if (selected == null && items.size() == 1) {
            selected = items.get(0);
        }
        view.setText(selected == null ? "" : selected.label, false);
        return selected == null ? null : selected.id;
    }

    private void pickExportDate(boolean isFromDate, DialogExportAttendanceBinding dialogBinding) {
        String existing = isFromDate ? exportFromDate : exportToDate;
        Calendar calendar = existing != null ? DateUtils.toCalendar(existing) : Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = DateUtils.fromYearMonthDay(year, month, dayOfMonth);
                    if (isFromDate) {
                        exportFromDate = date;
                    } else {
                        exportToDate = date;
                    }
                    updateExportDateLabels(dialogBinding);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void updateExportDateLabels(DialogExportAttendanceBinding dialogBinding) {
        dialogBinding.textFromDate.setText(exportFromDate == null
                ? getString(R.string.filter_from_date)
                : "From: " + DateUtils.toDisplayFormat(exportFromDate));
        dialogBinding.textToDate.setText(exportToDate == null
                ? getString(R.string.filter_to_date)
                : "To: " + DateUtils.toDisplayFormat(exportToDate));
    }

    private void onExportFileChosen(Uri uri) {
        if (uri == null) {
            // User cancelled the system file picker — do not report success.
            clearPendingExport();
            return;
        }
        if (pendingExport == ExportMode.FILTERED_REPORT) {
            if (pendingReportSessions == null || pendingReportMeta == null) {
                ViewUtils.toast(requireContext(), getString(R.string.history_export_empty));
                clearPendingExport();
                return;
            }
            excelService.exportConsolidatedAttendanceReport(
                    uri,
                    pendingReportMeta,
                    pendingReportSessions,
                    pendingReportAdjustments,
                    new ApiCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer data) {
                            ViewUtils.toast(requireContext(), getString(R.string.export_success_students,
                                    data == null ? 0 : data));
                            clearPendingExport();
                        }

                        @Override
                        public void onError(String message) {
                            ViewUtils.toastLong(requireContext(), message);
                            clearPendingExport();
                        }
                    });
            return;
        }
        ViewUtils.toast(requireContext(), getString(R.string.history_export_empty));
        clearPendingExport();
    }

    private void clearPendingExport() {
        pendingExport = null;
        pendingReportSessions = null;
        pendingReportMeta = null;
        pendingReportAdjustments = null;
    }

    private void renderSemesterReview(NetworkResult<List<SemesterAttendance.Review>> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isError()) {
            ViewUtils.toastLong(requireContext(), result.getMessage());
            return;
        }
        List<SemesterAttendance.Review> reviews = result.getData();
        DialogSemesterReviewBinding dialogBinding = DialogSemesterReviewBinding.inflate(
                LayoutInflater.from(requireContext()));
        List<SemesterAttendance.Review> below = SemesterAttendance.belowThreshold(reviews);
        if (below.isEmpty()) {
            dialogBinding.textReviewMessage.setVisibility(View.VISIBLE);
            dialogBinding.textReviewMessage.setText(R.string.semester_review_all_ok);
        }
        SemesterReviewAdapter reviewAdapter = new SemesterReviewAdapter();
        dialogBinding.recyclerReview.setLayoutManager(new LinearLayoutManager(requireContext()));
        dialogBinding.recyclerReview.setAdapter(reviewAdapter);
        // Show only students below the AttendancePolicy threshold (not the full list).
        reviewAdapter.submitList(below);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.semester_review_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void render(NetworkResult<List<AttendanceSession>> result) {
        if (result == null || binding == null) {
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
                List<AttendanceSession> data = result.getData();
                if (data == null || data.isEmpty()) {
                    if (viewModel.hasActiveFilters()) {
                        binding.includeEmpty.textEmptyTitle.setText(R.string.history_empty_filtered_title);
                        binding.includeEmpty.textEmptySubtitle.setText(R.string.history_empty_filtered_subtitle);
                    } else {
                        binding.includeEmpty.textEmptyTitle.setText(R.string.history_empty_title);
                        binding.includeEmpty.textEmptySubtitle.setText(R.string.history_empty_subtitle);
                    }
                    adapter.submitList(new ArrayList<>());
                    showState(State.EMPTY);
                } else {
                    adapter.submitList(data);
                    showState(State.CONTENT);
                }
                updateFilterChips();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
