package com.example.studentattendance.ui.reports;

import android.app.DatePickerDialog;
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
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.SemesterReportAdapter;
import com.example.studentattendance.databinding.FragmentReportsBinding;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.HistoryFilter;
import com.example.studentattendance.utils.PdfReportHelper;

/**
 * Full-semester attendance report on the existing Reports screen. Filters,
 * summary, course grouping, and student rows all use Room + AttendancePolicy.
 */
public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsViewModel viewModel;
    private SemesterReportAdapter adapter;
    private ReportsViewModel.ReportsData latestData;
    private boolean suppressFilterEvents;
    private boolean suppressChipEvents;
    private boolean preserveScrollOnNextBind;
    private String lastAcademicFilterKey;

    private ActivityResultLauncher<String> createExportFileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        adapter = new SemesterReportAdapter();
        binding.recyclerStudentReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerStudentReports.setAdapter(adapter);

        createExportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                this::onExportFileChosen);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            preserveScrollOnNextBind = true;
            viewModel.refreshReports();
        });
        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.loadReports());
        binding.buttonExportReport.setOnClickListener(v -> startReportExport());
        binding.buttonPrintReport.setOnClickListener(v -> {
            if (latestData != null) {
                PdfReportHelper.printStudentReports(
                        requireActivity(), latestData.studentReports, latestData.overallRate);
            }
        });
        binding.buttonClearFilters.setOnClickListener(v -> {
            preserveScrollOnNextBind = true;
            lastAcademicFilterKey = null;
            viewModel.clearFilters();
            if (binding.editReportSearch.getText() != null) {
                binding.editReportSearch.setText("");
            }
            syncChipSelectionFromViewModel();
            populateFilters(latestData == null ? null : latestData.filterOptions, true);
        });
        binding.editReportSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                preserveScrollOnNextBind = true;
                viewModel.search(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (suppressChipEvents || checkedIds == null || checkedIds.isEmpty()) {
                // Ignore transient empty states while ChipGroup switches selection.
                return;
            }
            preserveScrollOnNextBind = true;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipSortName) {
                viewModel.setSortMode(ReportsViewModel.SortMode.NAME);
            } else if (checkedId == R.id.chipSortRoll) {
                viewModel.setSortMode(ReportsViewModel.SortMode.ROLL);
            } else if (checkedId == R.id.chipSortPresent) {
                viewModel.setSortMode(ReportsViewModel.SortMode.PRESENT);
            } else if (checkedId == R.id.chipSortAbsent) {
                viewModel.setSortMode(ReportsViewModel.SortMode.ABSENT);
            } else {
                viewModel.setSortMode(ReportsViewModel.SortMode.PERCENTAGE);
            }
        });
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (suppressChipEvents || checkedIds == null || checkedIds.isEmpty()) {
                // Empty callback was resetting Below 75% back to All Students.
                return;
            }
            preserveScrollOnNextBind = true;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipStatusBelow) {
                viewModel.setStatusFilter(ReportsViewModel.StatusFilter.BELOW_THRESHOLD);
            } else if (checkedId == R.id.chipStatusAbove) {
                viewModel.setStatusFilter(ReportsViewModel.StatusFilter.AT_OR_ABOVE);
            } else {
                viewModel.setStatusFilter(ReportsViewModel.StatusFilter.ALL);
            }
        });

        wireFilter(binding.dropdownFilterDepartment, id -> viewModel.setFilterDepartment(id));
        wireFilter(binding.dropdownFilterShift, id -> viewModel.setFilterShift(id));
        wireFilter(binding.dropdownFilterSemester, id -> viewModel.setFilterSemester(id));
        wireFilter(binding.dropdownFilterSection, id -> viewModel.setFilterSection(id));
        wireFilter(binding.dropdownFilterSubject, id -> viewModel.setFilterSubject(id));
        wireFilter(binding.dropdownFilterCourse, id -> viewModel.setFilterCourseCode(id));
        binding.textFilterFromDate.setOnClickListener(v -> pickDate(true));
        binding.textFilterToDate.setOnClickListener(v -> pickDate(false));
        binding.textFilterFromDate.setOnLongClickListener(v -> {
            preserveScrollOnNextBind = true;
            viewModel.setDateRange(null, viewModel.getFilterToDate());
            return true;
        });
        binding.textFilterToDate.setOnLongClickListener(v -> {
            preserveScrollOnNextBind = true;
            viewModel.setDateRange(viewModel.getFilterFromDate(), null);
            return true;
        });

        viewModel.getReportsData().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadReports();
        syncChipSelectionFromViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            preserveScrollOnNextBind = true;
            viewModel.refreshReports();
        }
    }

    private void wireFilter(AutoCompleteTextView dropdown, java.util.function.Consumer<String> onIdPicked) {
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            if (suppressFilterEvents) {
                return;
            }
            preserveScrollOnNextBind = true;
            onIdPicked.accept(idFromPosition(parent.getItemAtPosition(position)));
        });
    }

    private static String idFromPosition(Object item) {
        if (item instanceof ReportsViewModel.FilterOption) {
            return ((ReportsViewModel.FilterOption) item).id;
        }
        return null;
    }

    private void pickDate(boolean isFromDate) {
        String existing = isFromDate ? viewModel.getFilterFromDate() : viewModel.getFilterToDate();
        Calendar calendar = existing != null ? DateUtils.toCalendar(existing) : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = DateUtils.fromYearMonthDay(year, month, dayOfMonth);
                    preserveScrollOnNextBind = true;
                    if (isFromDate) {
                        viewModel.setDateRange(date, viewModel.getFilterToDate());
                    } else {
                        viewModel.setDateRange(viewModel.getFilterFromDate(), date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void render(NetworkResult<ReportsViewModel.ReportsData> result) {
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
                final int scrollY = binding.scrollReports.getScrollY();
                final boolean restoreScroll = preserveScrollOnNextBind;
                preserveScrollOnNextBind = false;
                latestData = result.getData();
                bindData(latestData);
                syncChipSelectionFromViewModel();
                showState(State.CONTENT);
                if (restoreScroll) {
                    binding.scrollReports.post(() -> {
                        if (binding != null) {
                            binding.scrollReports.scrollTo(0, scrollY);
                        }
                    });
                }
                break;
            case ERROR:
                binding.swipeRefresh.setRefreshing(false);
                preserveScrollOnNextBind = false;
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                break;
        }
    }

    private enum State { LOADING, CONTENT, ERROR }

    private void showState(State state) {
        binding.swipeRefresh.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeEmpty.layoutEmpty.setVisibility(View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
    }

    private void syncChipSelectionFromViewModel() {
        if (binding == null || viewModel == null) {
            return;
        }
        suppressChipEvents = true;
        ReportsViewModel.StatusFilter status = viewModel.getStatusFilter();
        if (status == ReportsViewModel.StatusFilter.BELOW_THRESHOLD) {
            binding.chipStatusBelow.setChecked(true);
        } else if (status == ReportsViewModel.StatusFilter.AT_OR_ABOVE) {
            binding.chipStatusAbove.setChecked(true);
        } else {
            binding.chipStatusAll.setChecked(true);
        }
        ReportsViewModel.SortMode sort = viewModel.getSortMode();
        if (sort == ReportsViewModel.SortMode.NAME) {
            binding.chipSortName.setChecked(true);
        } else if (sort == ReportsViewModel.SortMode.ROLL) {
            binding.chipSortRoll.setChecked(true);
        } else if (sort == ReportsViewModel.SortMode.PRESENT) {
            binding.chipSortPresent.setChecked(true);
        } else if (sort == ReportsViewModel.SortMode.ABSENT) {
            binding.chipSortAbsent.setChecked(true);
        } else {
            binding.chipSortPercentage.setChecked(true);
        }
        suppressChipEvents = false;
    }

    private void bindData(ReportsViewModel.ReportsData data) {
        if (data == null) {
            return;
        }
        // Rebinding dropdown adapters remasures NestedScrollView and causes jump.
        // Only refresh academic dropdowns when their scope/options actually change.
        String academicKey = academicFilterKey(data);
        if (!academicKey.equals(lastAcademicFilterKey)) {
            populateFilters(data.filterOptions, false);
            lastAcademicFilterKey = academicKey;
        } else {
            bindDateLabels();
        }
        binding.textReportHeaderMeta.setText(getString(
                R.string.reports_header_meta,
                data.headerDepartment,
                data.headerShift,
                data.headerSemester,
                data.headerSection,
                data.headerCourse,
                data.headerCourseCode));
        String period = data.reportingPeriod == null || data.reportingPeriod.isEmpty()
                ? getString(R.string.reports_stat_none)
                : data.reportingPeriod;
        binding.textReportPeriod.setText(getString(R.string.reports_period_label, period));

        binding.textReportRateValue.setText(String.format(Locale.US, "%.1f%%", data.overallRate));
        bindStat(binding.cardReportStudents, String.valueOf(data.totalStudents),
                getString(R.string.reports_stat_students),
                getString(R.string.reports_stat_students_hint), R.color.text_primary);
        bindStat(binding.cardReportSessions, String.valueOf(data.totalSessions),
                getString(R.string.reports_stat_sessions),
                getString(R.string.reports_stat_sessions_hint), R.color.text_primary);
        bindStat(binding.cardReportPresent, String.valueOf(data.presentCount),
                getString(R.string.reports_stat_present),
                getString(R.string.reports_stat_record_hint), R.color.status_present);
        bindStat(binding.cardReportAbsent, String.valueOf(data.absentCount),
                getString(R.string.reports_stat_absent),
                getString(R.string.reports_stat_record_hint), R.color.status_absent);
        bindStat(binding.cardReportLate, String.valueOf(data.lateCount),
                getString(R.string.reports_stat_late),
                getString(R.string.reports_stat_record_hint), R.color.status_late);
        bindStat(binding.cardReportLeave, String.valueOf(data.leaveCount),
                getString(R.string.reports_stat_leave),
                getString(R.string.reports_stat_record_hint), R.color.status_leave);
        bindStat(binding.cardReportAbove, String.valueOf(data.atOrAboveCount),
                getString(R.string.reports_stat_above),
                getString(R.string.reports_stat_threshold_hint), R.color.status_present);
        bindStat(binding.cardReportBelow, String.valueOf(data.belowCount),
                getString(R.string.reports_stat_below),
                getString(R.string.reports_stat_threshold_hint), R.color.status_absent);
        String highest = data.highestLabel == null || data.highestLabel.isEmpty()
                ? getString(R.string.reports_stat_none) : data.highestLabel;
        bindStat(binding.cardReportHighest, highest, getString(R.string.reports_stat_highest),
                getString(R.string.reports_stat_highest_hint), R.color.text_primary);
        String lowest = data.lowestLabel == null || data.lowestLabel.isEmpty()
                ? getString(R.string.reports_stat_none) : data.lowestLabel;
        binding.textLowestAttendance.setText(getString(R.string.reports_lowest_format, lowest));

        bindTrend(data.weeklyTrend, binding.chartWeeklyTrend, binding.textWeeklyEmpty);
        bindTrend(data.monthlyTrend, binding.chartMonthlyTrend, binding.textMonthlyEmpty);

        boolean noStudents = data.semester == null || !data.semester.hasVisibleStudents;
        binding.textStudentsEmpty.setVisibility(noStudents ? View.VISIBLE : View.GONE);
        binding.recyclerStudentReports.setVisibility(noStudents ? View.GONE : View.VISIBLE);
        binding.textStudentsEmpty.setText(emptyMessage(data));
        adapter.submit(data.semester);
    }

    private String academicFilterKey(ReportsViewModel.ReportsData data) {
        return String.valueOf(viewModel.getFilterDepartmentId())
                + "|" + viewModel.getFilterShift()
                + "|" + viewModel.getFilterSemesterId()
                + "|" + viewModel.getFilterSectionId()
                + "|" + viewModel.getFilterSubjectId()
                + "|" + viewModel.getFilterCourseCode()
                + "|" + viewModel.getFilterFromDate()
                + "|" + viewModel.getFilterToDate()
                + "|" + (data.filterOptions == null ? 0 : data.filterOptions.departments.size())
                + "|" + (data.filterOptions == null ? 0 : data.filterOptions.semesters.size())
                + "|" + (data.filterOptions == null ? 0 : data.filterOptions.sections.size())
                + "|" + (data.filterOptions == null ? 0 : data.filterOptions.subjects.size())
                + "|" + (data.filterOptions == null ? 0 : data.filterOptions.courseCodes.size());
    }

    private String emptyMessage(ReportsViewModel.ReportsData data) {
        if (data.statusFilterEmptyBecauseAllAbove) {
            return getString(R.string.reports_all_above_75);
        }
        if (!data.hasAttendance && data.semester != null && data.semester.hasActiveFilters) {
            return getString(R.string.reports_no_match);
        }
        if (!data.hasAttendance) {
            return getString(R.string.reports_no_attendance);
        }
        return getString(R.string.reports_students_empty);
    }

    private void bindDateLabels() {
        String from = viewModel.getFilterFromDate();
        String to = viewModel.getFilterToDate();
        binding.textFilterFromDate.setText(from == null
                ? getString(R.string.reports_filter_from)
                : DateUtils.toDisplayFormat(from));
        binding.textFilterToDate.setText(to == null
                ? getString(R.string.reports_filter_to)
                : DateUtils.toDisplayFormat(to));
    }

    private void bindStat(com.example.studentattendance.databinding.ViewReportStatCardBinding card,
                          String value, String label, String hint, int valueColorRes) {
        card.textStatValue.setText(value);
        card.textStatLabel.setText(label);
        card.textStatHint.setText(hint);
        card.textStatValue.setTextColor(androidx.core.content.ContextCompat.getColor(
                requireContext(), valueColorRes));
        card.textStatLabel.setTextColor(androidx.core.content.ContextCompat.getColor(
                requireContext(), R.color.text_primary));
        card.textStatHint.setTextColor(androidx.core.content.ContextCompat.getColor(
                requireContext(), R.color.text_secondary));
        card.getRoot().setClickable(false);
        card.getRoot().setFocusable(false);
        card.getRoot().setForeground(null);
    }

    private void bindTrend(List<DailyTrend> trend,
                           com.example.studentattendance.ui.dashboard.TrendChartView chart,
                           android.widget.TextView empty) {
        boolean hasData = trend != null && !trend.isEmpty();
        chart.setVisibility(hasData ? View.VISIBLE : View.INVISIBLE);
        empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        chart.setTrend(hasData ? trend : new ArrayList<>());
    }

    private void populateFilters(ReportsViewModel.FilterOptions options, boolean forceAll) {
        if (binding == null) {
            return;
        }
        ReportsViewModel.FilterOptions safe = options == null
                ? new ReportsViewModel.FilterOptions() : options;
        suppressFilterEvents = true;
        bindDropdown(binding.dropdownFilterDepartment, safe.departments,
                forceAll ? null : viewModel.getFilterDepartmentId());
        bindShiftDropdown(forceAll ? null : viewModel.getFilterShift());
        bindDropdown(binding.dropdownFilterSemester, safe.semesters,
                forceAll ? null : viewModel.getFilterSemesterId());
        bindDropdown(binding.dropdownFilterSection, safe.sections,
                forceAll ? null : viewModel.getFilterSectionId());
        bindDropdown(binding.dropdownFilterSubject, safe.subjects,
                forceAll ? null : viewModel.getFilterSubjectId());
        bindDropdown(binding.dropdownFilterCourse, safe.courseCodes,
                forceAll ? null : viewModel.getFilterCourseCode());
        suppressFilterEvents = false;
        bindDateLabels();
    }

    private void bindShiftDropdown(String selectedShift) {
        List<ReportsViewModel.FilterOption> items = new ArrayList<>();
        items.add(new ReportsViewModel.FilterOption(null, getString(R.string.reports_filter_all)));
        items.add(new ReportsViewModel.FilterOption(
                com.example.studentattendance.model.DepartmentShift.MORNING,
                com.example.studentattendance.model.DepartmentShift.label(
                        com.example.studentattendance.model.DepartmentShift.MORNING)));
        items.add(new ReportsViewModel.FilterOption(
                com.example.studentattendance.model.DepartmentShift.EVENING,
                com.example.studentattendance.model.DepartmentShift.label(
                        com.example.studentattendance.model.DepartmentShift.EVENING)));
        ArrayAdapter<ReportsViewModel.FilterOption> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, items);
        binding.dropdownFilterShift.setAdapter(adapter);
        binding.dropdownFilterShift.setTag(items);
        String label = getString(R.string.reports_filter_all);
        String normalized = HistoryFilter.normalizeShift(selectedShift);
        if (normalized != null) {
            for (ReportsViewModel.FilterOption item : items) {
                if (normalized.equals(item.id)) {
                    label = item.label;
                    break;
                }
            }
        }
        binding.dropdownFilterShift.setText(label, false);
    }

    private void bindDropdown(AutoCompleteTextView dropdown,
                              List<ReportsViewModel.FilterOption> values,
                              String selectedId) {
        List<ReportsViewModel.FilterOption> items = new ArrayList<>();
        items.add(new ReportsViewModel.FilterOption(null, getString(R.string.reports_filter_all)));
        if (values != null) {
            items.addAll(values);
        }
        ArrayAdapter<ReportsViewModel.FilterOption> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown, items);
        dropdown.setAdapter(adapter);
        dropdown.setTag(items);
        String label = getString(R.string.reports_filter_all);
        if (selectedId != null) {
            for (ReportsViewModel.FilterOption item : items) {
                if (selectedId.equals(item.id)) {
                    label = item.label;
                    break;
                }
            }
        }
        dropdown.setText(label, false);
    }

    private void startReportExport() {
        if (latestData == null || latestData.semester == null || !latestData.semester.hasVisibleStudents) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.export_no_data_title)
                    .setMessage(R.string.export_no_data_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        String shiftLabel = viewModel.getFilterShift() == null
                ? null
                : com.example.studentattendance.model.DepartmentShift.label(viewModel.getFilterShift());
        String fileName = AttendanceExportRows.buildReportsExportFileName(
                latestData.headerDepartment,
                shiftLabel,
                latestData.headerSemester,
                latestData.headerSection,
                latestData.headerCourseCode,
                viewModel.getFilterFromDate(),
                viewModel.getFilterToDate());
        createExportFileLauncher.launch(fileName);
    }

    private void onExportFileChosen(@Nullable Uri uri) {
        if (uri == null || latestData == null || latestData.semester == null
                || !latestData.semester.hasVisibleStudents) {
            return;
        }
        RepositoryProvider.getInstance(requireContext()).getExcelService()
                .exportSemesterReport(uri, latestData.semester, viewModel.currentFilter(),
                        new ApiCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer rowCount) {
                                if (!isAdded()) {
                                    return;
                                }
                                new AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.export_excel_success_title)
                                        .setMessage(R.string.export_excel_success_message)
                                        .setPositiveButton(R.string.ok, null)
                                        .show();
                            }

                            @Override
                            public void onError(String message) {
                                if (!isAdded()) {
                                    return;
                                }
                                new AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.export_no_data_title)
                                        .setMessage(message)
                                        .setPositiveButton(R.string.ok, null)
                                        .show();
                            }
                        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
