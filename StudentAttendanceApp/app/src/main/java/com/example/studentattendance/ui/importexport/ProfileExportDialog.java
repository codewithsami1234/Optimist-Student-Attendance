package com.example.studentattendance.ui.importexport;

import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.DialogExportAttendanceBinding;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.DepartmentShift;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.utils.AttendanceExportRows;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.HistoryFilterOptions;

/**
 * Profile → Export Excel. Asks for a real class, then reuses the View History
 * consolidated workbook. The dialog only lists values that exist in Room.
 */
final class ProfileExportDialog {

    private final ImportExportActivity activity;
    private final ImportExportViewModel viewModel;
    private final List<ClassSetup> setups;
    private final DialogExportAttendanceBinding binding;

    private final String[] shift = new String[1];
    private final String[] departmentId = new String[1];
    private final String[] semesterId = new String[1];
    private final String[] sectionId = new String[1];
    private final String[] subjectId = new String[1];
    private final String[] courseCode = new String[1];
    private String fromDate;
    private String toDate;
    private boolean generating;

    private ProfileExportDialog(ImportExportActivity activity, ImportExportViewModel viewModel,
                                List<ClassSetup> setups) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.setups = setups == null ? new ArrayList<>() : setups;
        this.binding = DialogExportAttendanceBinding.inflate(LayoutInflater.from(activity));
    }

    static void show(ImportExportActivity activity, ImportExportViewModel viewModel,
                     List<ClassSetup> setups) {
        new ProfileExportDialog(activity, viewModel, setups).open();
    }

    private void open() {
        binding.inputLayoutSemester.setVisibility(View.VISIBLE);
        updateDateLabels();
        bindShift();
        binding.cardFromDate.setOnClickListener(v -> pickDate(true));
        binding.cardToDate.setOnClickListener(v -> pickDate(false));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.action_generate_excel, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            android.widget.Button generate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button cancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            generate.setOnClickListener(v -> generate(dialog, generate, cancel));
        });
        dialog.show();
    }

    private void generate(AlertDialog dialog, android.widget.Button generate, android.widget.Button cancel) {
        if (generating) {
            return;
        }
        String error = validate();
        if (error != null) {
            showError(error);
            return;
        }
        generating = true;
        generate.setEnabled(false);
        cancel.setEnabled(false);
        generate.setText(R.string.action_generating_excel);
        binding.progressExport.setVisibility(View.VISIBLE);
        binding.textExportError.setVisibility(View.GONE);

        viewModel.loadScopedSessionsForExport(
                departmentId[0], semesterId[0], sectionId[0], subjectId[0], courseCode[0], shift[0],
                fromDate, toDate, setups, new ApiCallback<List<com.example.studentattendance.model.AttendanceSession>>() {
                    @Override
                    public void onSuccess(List<com.example.studentattendance.model.AttendanceSession> sessions) {
                        if (sessions == null || sessions.isEmpty()) {
                            resetGenerate(generate, cancel, activity.getString(R.string.export_profile_no_records));
                            return;
                        }
                        viewModel.loadAdjustmentDeltas(new ApiCallback<Map<String, Integer>>() {
                            @Override
                            public void onSuccess(Map<String, Integer> deltas) {
                                AttendanceExportRows.ReportMeta meta = buildMeta(sessions);
                                String fileName = AttendanceExportRows.buildProfileExportFileName(
                                        meta.departmentName, meta.shiftName, meta.semesterName,
                                        meta.sectionName, meta.courseCode);
                                generating = false;
                                dialog.dismiss();
                                activity.launchProfileExport(meta, sessions, deltas, fileName);
                            }

                            @Override
                            public void onError(String message) {
                                onSuccess(new LinkedHashMap<>());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        resetGenerate(generate, cancel, message == null
                                ? activity.getString(R.string.export_profile_no_records) : message);
                    }
                });
    }

    private void resetGenerate(android.widget.Button generate, android.widget.Button cancel, String error) {
        generating = false;
        generate.setEnabled(true);
        cancel.setEnabled(true);
        generate.setText(R.string.action_generate_excel);
        binding.progressExport.setVisibility(View.GONE);
        showError(error);
    }

    private AttendanceExportRows.ReportMeta buildMeta(
            List<com.example.studentattendance.model.AttendanceSession> sessions) {
        ClassSetup setup = findSelected();
        AttendanceExportRows.ReportMeta meta = new AttendanceExportRows.ReportMeta();
        if (setup != null && setup.getDepartment() != null) {
            meta.departmentName = setup.getDepartment().getName();
        }
        meta.shiftName = DepartmentShift.label(shift[0]);
        meta.semesterName = setup == null ? "" : setup.semesterLabel();
        if (setup != null && setup.getSection() != null && setup.getSection().getName() != null) {
            meta.sectionName = setup.getSection().getName();
        }
        if (setup != null && setup.getSubject() != null) {
            meta.courseName = setup.getSubject().getName();
            meta.courseCode = setup.getSubject().getCode();
        }
        String[] span = dateSpan(sessions);
        meta.fromDate = fromDate != null ? fromDate : span[0];
        meta.toDate = toDate != null ? toDate : span[1];
        return meta;
    }

    private String[] dateSpan(List<com.example.studentattendance.model.AttendanceSession> sessions) {
        String min = null;
        String max = null;
        if (sessions != null) {
            for (com.example.studentattendance.model.AttendanceSession session : sessions) {
                if (session == null || session.getDate() == null || session.getDate().isEmpty()) {
                    continue;
                }
                if (min == null || session.getDate().compareTo(min) < 0) {
                    min = session.getDate();
                }
                if (max == null || session.getDate().compareTo(max) > 0) {
                    max = session.getDate();
                }
            }
        }
        return new String[]{min, max};
    }

    private String validate() {
        if (shift[0] == null) {
            return activity.getString(R.string.export_error_select_shift);
        }
        if (departmentId[0] == null) {
            return activity.getString(R.string.export_error_select_department);
        }
        if (semesterId[0] == null) {
            return activity.getString(R.string.export_error_select_semester);
        }
        if (sectionId[0] == null) {
            return activity.getString(R.string.export_error_select_section);
        }
        if (subjectId[0] == null) {
            return activity.getString(R.string.export_error_select_course);
        }
        if ((fromDate == null) != (toDate == null)) {
            return fromDate == null
                    ? activity.getString(R.string.export_error_select_from_date)
                    : activity.getString(R.string.export_error_select_to_date);
        }
        if (fromDate != null && fromDate.compareTo(toDate) > 0) {
            return activity.getString(R.string.export_error_date_order);
        }
        return null;
    }

    private void bindShift() {
        Map<String, HistoryFilterOptions.Option> unique = new LinkedHashMap<>();
        for (ClassSetup setup : setups) {
            Department department = setup == null ? null : setup.getDepartment();
            if (department == null) {
                continue;
            }
            String code = DepartmentShift.normalize(department.getShift());
            unique.put(code, new HistoryFilterOptions.Option(code, DepartmentShift.label(code)));
        }
        shift[0] = populate(binding.dropdownShift, new ArrayList<>(unique.values()), shift[0], false);
        binding.dropdownShift.setOnItemClickListener((p, v, pos, id) -> {
            shift[0] = optionId(binding.dropdownShift, pos);
            clearBelowShift();
            bindDepartments();
        });
        bindDepartments();
    }

    private void bindDepartments() {
        Map<String, HistoryFilterOptions.Option> unique = new LinkedHashMap<>();
        for (ClassSetup setup : matching(true, false, false, false)) {
            Department department = setup.getDepartment();
            unique.put(department.getId(), new HistoryFilterOptions.Option(
                    department.getId(), department.getName()));
        }
        departmentId[0] = populate(binding.dropdownDepartment, new ArrayList<>(unique.values()),
                departmentId[0], false);
        binding.dropdownDepartment.setOnItemClickListener((p, v, pos, id) -> {
            departmentId[0] = optionId(binding.dropdownDepartment, pos);
            clearBelowDepartment();
            bindSemesters();
        });
        bindSemesters();
    }

    private void bindSemesters() {
        if (departmentId[0] == null) {
            semesterId[0] = populate(binding.dropdownSemester, new ArrayList<>(), null, false);
            clearBelowSemester();
            bindSections();
            return;
        }
        Map<String, HistoryFilterOptions.Option> unique = new LinkedHashMap<>();
        for (ClassSetup setup : matching(true, true, false, false)) {
            Semester semester = setup.getSemester();
            if (semester == null || semester.getId() == null) {
                continue;
            }
            unique.put(semester.getId(), new HistoryFilterOptions.Option(
                    semester.getId(), setup.semesterLabel()));
        }
        semesterId[0] = populate(binding.dropdownSemester, new ArrayList<>(unique.values()),
                semesterId[0], false);
        binding.dropdownSemester.setOnItemClickListener((p, v, pos, id) -> {
            semesterId[0] = optionId(binding.dropdownSemester, pos);
            clearBelowSemester();
            bindSections();
        });
        bindSections();
    }

    private void bindSections() {
        if (semesterId[0] == null) {
            sectionId[0] = populate(binding.dropdownSection, new ArrayList<>(), null, false);
            clearBelowSection();
            bindCourses();
            return;
        }
        Map<String, HistoryFilterOptions.Option> unique = new LinkedHashMap<>();
        for (ClassSetup setup : matching(true, true, true, false)) {
            Section section = setup.getSection();
            if (section == null || section.getId() == null) {
                continue;
            }
            String name = section.getName() == null ? "" : section.getName().trim();
            if (name.isEmpty()) {
                name = activity.getString(R.string.label_no_section);
            }
            unique.put(section.getId(), new HistoryFilterOptions.Option(section.getId(), name));
        }
        sectionId[0] = populate(binding.dropdownSection, new ArrayList<>(unique.values()),
                sectionId[0], false);
        binding.dropdownSection.setOnItemClickListener((p, v, pos, id) -> {
            sectionId[0] = optionId(binding.dropdownSection, pos);
            clearBelowSection();
            bindCourses();
        });
        bindCourses();
    }

    private void bindCourses() {
        if (sectionId[0] == null) {
            subjectId[0] = populate(binding.dropdownCourse, new ArrayList<>(), null, false);
            courseCode[0] = populate(binding.dropdownCourseCode, new ArrayList<>(), null, false);
            return;
        }
        Map<String, HistoryFilterOptions.Option> courses = new LinkedHashMap<>();
        Map<String, HistoryFilterOptions.Option> codes = new LinkedHashMap<>();
        for (ClassSetup setup : matching(true, true, true, true)) {
            Subject subject = setup.getSubject();
            if (subject == null || subject.getId() == null) {
                continue;
            }
            courses.put(subject.getId(), new HistoryFilterOptions.Option(
                    subject.getId(), subject.getName() == null ? "" : subject.getName()));
            String code = subject.getCode() == null ? "" : subject.getCode().trim();
            if (!code.isEmpty()) {
                codes.put(code.toUpperCase(java.util.Locale.US),
                        new HistoryFilterOptions.Option(code.toUpperCase(java.util.Locale.US), code));
            }
        }
        subjectId[0] = populate(binding.dropdownCourse, new ArrayList<>(courses.values()),
                subjectId[0], false);
        if (subjectId[0] != null) {
            ClassSetup selected = findSelected();
            if (selected != null && selected.getSubject() != null && selected.getSubject().getCode() != null) {
                courseCode[0] = selected.getSubject().getCode().trim().toUpperCase(java.util.Locale.US);
            }
        }
        courseCode[0] = populate(binding.dropdownCourseCode, new ArrayList<>(codes.values()),
                courseCode[0], false);
        binding.dropdownCourse.setOnItemClickListener((p, v, pos, id) -> {
            subjectId[0] = optionId(binding.dropdownCourse, pos);
            ClassSetup selected = findSelected();
            courseCode[0] = selected != null && selected.getSubject() != null
                    && selected.getSubject().getCode() != null
                    ? selected.getSubject().getCode().trim().toUpperCase(java.util.Locale.US)
                    : null;
            bindCourses();
        });
        binding.dropdownCourseCode.setOnItemClickListener((p, v, pos, id) -> {
            courseCode[0] = optionId(binding.dropdownCourseCode, pos);
            if (courseCode[0] == null) {
                return;
            }
            for (ClassSetup setup : matching(true, true, true, true)) {
                Subject subject = setup.getSubject();
                if (subject != null && subject.getCode() != null
                        && courseCode[0].equalsIgnoreCase(subject.getCode().trim())) {
                    subjectId[0] = subject.getId();
                    populate(binding.dropdownCourse, new ArrayList<>(courses.values()), subjectId[0], false);
                    break;
                }
            }
        });
    }

    private List<ClassSetup> matching(boolean useShift, boolean useDepartment, boolean useSemester,
                                      boolean useSection) {
        List<ClassSetup> matched = new ArrayList<>();
        for (ClassSetup setup : setups) {
            if (setup == null || setup.getDepartment() == null) {
                continue;
            }
            if (useShift && shift[0] != null
                    && !shift[0].equals(DepartmentShift.normalize(setup.getDepartment().getShift()))) {
                continue;
            }
            if (useDepartment && departmentId[0] != null
                    && !departmentId[0].equals(setup.getDepartment().getId())) {
                continue;
            }
            if (useSemester && semesterId[0] != null
                    && (setup.getSemester() == null || !semesterId[0].equals(setup.getSemester().getId()))) {
                continue;
            }
            if (useSection && sectionId[0] != null
                    && (setup.getSection() == null || !sectionId[0].equals(setup.getSection().getId()))) {
                continue;
            }
            matched.add(setup);
        }
        return matched;
    }

    private ClassSetup findSelected() {
        for (ClassSetup setup : matching(true, true, true, true)) {
            if (setup.getSubject() != null && subjectId[0] != null
                    && subjectId[0].equals(setup.getSubject().getId())) {
                return setup;
            }
        }
        return null;
    }

    private void clearBelowShift() {
        departmentId[0] = null;
        clearBelowDepartment();
    }

    private void clearBelowDepartment() {
        semesterId[0] = null;
        clearBelowSemester();
    }

    private void clearBelowSemester() {
        sectionId[0] = null;
        clearBelowSection();
    }

    private void clearBelowSection() {
        subjectId[0] = null;
        courseCode[0] = null;
    }

    private String populate(AutoCompleteTextView view, List<HistoryFilterOptions.Option> items,
                            String selectedId, boolean includeAll) {
        List<HistoryFilterOptions.Option> options = new ArrayList<>();
        if (includeAll) {
            options.add(new HistoryFilterOptions.Option(null, activity.getString(R.string.filter_all)));
        }
        if (items != null) {
            options.addAll(items);
        }
        ArrayAdapter<HistoryFilterOptions.Option> adapter = new ArrayAdapter<>(
                activity, R.layout.item_dropdown, options);
        view.setAdapter(adapter);
        view.setTag(options);
        HistoryFilterOptions.Option selected = null;
        if (selectedId != null) {
            for (HistoryFilterOptions.Option option : options) {
                if (selectedId.equals(option.id)) {
                    selected = option;
                    break;
                }
            }
        }
        if (selected == null && options.size() == 1) {
            selected = options.get(0);
        }
        view.setText(selected == null ? "" : selected.label, false);
        return selected == null ? null : selected.id;
    }

    @SuppressWarnings("unchecked")
    private String optionId(AutoCompleteTextView view, int pos) {
        Object tag = view.getTag();
        if (!(tag instanceof List) || pos < 0) {
            return null;
        }
        List<HistoryFilterOptions.Option> items = (List<HistoryFilterOptions.Option>) tag;
        if (pos >= items.size()) {
            return null;
        }
        return items.get(pos).id;
    }

    private void pickDate(boolean from) {
        String existing = from ? fromDate : toDate;
        Calendar calendar = existing != null ? DateUtils.toCalendar(existing) : Calendar.getInstance();
        new DatePickerDialog(activity, (view, year, month, day) -> {
            String date = DateUtils.fromYearMonthDay(year, month, day);
            if (from) {
                fromDate = date;
            } else {
                toDate = date;
            }
            updateDateLabels();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabels() {
        binding.textFromDate.setText(fromDate == null
                ? activity.getString(R.string.filter_from_date)
                : "From: " + DateUtils.toDisplayFormat(fromDate));
        binding.textToDate.setText(toDate == null
                ? activity.getString(R.string.filter_to_date)
                : "To: " + DateUtils.toDisplayFormat(toDate));
    }

    private void showError(String message) {
        binding.textExportError.setVisibility(View.VISIBLE);
        binding.textExportError.setText(message);
    }
}
