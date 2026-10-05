package com.example.studentattendance.ui.selection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.CatalogChoiceAdapter;
import com.example.studentattendance.databinding.ActivitySelectionBinding;
import com.example.studentattendance.databinding.DialogAddSectionBinding;
import com.example.studentattendance.databinding.DialogAddSemesterBinding;
import com.example.studentattendance.databinding.DialogAddSubjectBinding;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.model.Section;
import com.example.studentattendance.model.Semester;
import com.example.studentattendance.model.Subject;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.students.StudentsActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.ViewUtils;

import androidx.appcompat.app.AlertDialog;

/**
 * Attendance workflow: Department → Semester → Section → Course.
 * Teachers create these Room records with Add actions; attendance is saved for today.
 */
public class SelectionActivity extends AppCompatActivity {

    private ActivitySelectionBinding binding;
    private SelectionViewModel viewModel;
    private Department department;
    private String selectedDate;

    private CatalogChoiceAdapter<Semester> semesterAdapter;
    private CatalogChoiceAdapter<Section> sectionAdapter;
    private CatalogChoiceAdapter<Subject> subjectAdapter;
    private final List<Semester> semesterItems = new ArrayList<>();
    private final List<Section> sectionItems = new ArrayList<>();
    private final List<Subject> subjectItems = new ArrayList<>();
    private Semester selectedSemester;
    private Section selectedSection;
    private Subject selectedSubject;
    private String pendingSubjectName;
    private String pendingSemesterName;
    private String pendingSectionName;
    private boolean rosterMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(SelectionViewModel.class);
        rosterMode = getIntent().getBooleanExtra(Constants.EXTRA_ROSTER_MODE, false);
        applySelectionMode();

        selectedDate = DateUtils.today();
        binding.textSelectedDate.setText(DateUtils.toDisplayFormat(selectedDate));

        setupSemesterList();
        setupSectionList();
        setupSubjectList();
        binding.cardDepartment.setOnClickListener(v -> showDepartmentPicker());

        binding.buttonLoadStudents.setOnClickListener(v -> {
            if (rosterMode) {
                proceedToViewRoster();
            } else {
                proceedToManageStudents();
            }
        });
        binding.buttonViewRoster.setOnClickListener(v -> proceedToViewRoster());
        binding.textAddSemester.setOnClickListener(v -> showAddSemesterDialog());
        binding.textAddSection.setOnClickListener(v -> showAddSectionDialog());
        binding.textAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        viewModel.getDepartments().observe(this, this::onDepartmentsLoaded);
        viewModel.getSemesters().observe(this, this::onSemestersLoaded);
        viewModel.getSections().observe(this, this::onSectionsLoaded);
        viewModel.getSubjects().observe(this, this::onSubjectsLoaded);

        viewModel.loadDepartments();
        if (savedInstanceState != null) {
            rosterMode = savedInstanceState.getBoolean(Constants.EXTRA_ROSTER_MODE, rosterMode);
            applySelectionMode();
            department = (Department) savedInstanceState.getSerializable(Constants.EXTRA_DEPARTMENT);
            selectedSemester = (Semester) savedInstanceState.getSerializable(Constants.EXTRA_SEMESTER);
            selectedSection = (Section) savedInstanceState.getSerializable(Constants.EXTRA_SECTION);
            selectedSubject = (Subject) savedInstanceState.getSerializable(Constants.EXTRA_SUBJECT);
        } else {
            department = (Department) getIntent().getSerializableExtra(Constants.EXTRA_DEPARTMENT);
        }
        if (department != null) {
            applyDepartment(department, false);
        } else {
            binding.textDepartmentName.setText(R.string.hint_choose_department);
        }
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.EXTRA_DEPARTMENT, department);
        outState.putSerializable(Constants.EXTRA_SEMESTER, selectedSemester);
        outState.putSerializable(Constants.EXTRA_SECTION, selectedSection);
        outState.putSerializable(Constants.EXTRA_SUBJECT, selectedSubject);
        outState.putString(Constants.EXTRA_DATE, selectedDate);
        outState.putBoolean(Constants.EXTRA_ROSTER_MODE, rosterMode);
    }

    private void applySelectionMode() {
        binding.layoutLectureBlock.setVisibility(View.GONE);
        binding.layoutDateBlock.setVisibility(View.GONE);
        binding.layoutCourseBlock.setVisibility(rosterMode ? View.GONE : View.VISIBLE);
        binding.buttonViewRoster.setVisibility(View.GONE);
        if (rosterMode) {
            binding.toolbar.setTitle(R.string.title_students);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_students);
            }
            binding.buttonLoadStudents.setText(R.string.action_view_roster);
        } else {
            binding.toolbar.setTitle(R.string.title_select_class);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_select_class);
            }
            binding.buttonLoadStudents.setText(R.string.action_continue_manage_students);
        }
    }

    private void onDepartmentsLoaded(NetworkResult<List<Department>> result) {
        if (result != null && result.isError()) {
            ViewUtils.toast(this, result.getMessage());
        }
    }

    private void showDepartmentPicker() {
        NetworkResult<List<Department>> result = viewModel.getDepartments().getValue();
        if (result == null || result.isLoading()) {
            ViewUtils.toastShort(this, getString(R.string.hint_choose_department));
            viewModel.loadDepartments();
            return;
        }
        if (result.isError()) {
            ViewUtils.toast(this, result.getMessage());
            viewModel.loadDepartments();
            return;
        }
        List<Department> list = result.getData();
        if (list == null || list.isEmpty()) {
            ViewUtils.toast(this, getString(R.string.selection_empty_departments));
            return;
        }
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Department item = list.get(i);
            String name = item.displayLabel();
            String code = item.getCode();
            names[i] = (code != null && !code.isEmpty()) ? name + " (" + code + ")" : name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.hint_choose_department)
                .setItems(names, (dialog, which) -> applyDepartment(list.get(which), true))
                .show();
    }

    private void applyDepartment(Department selected, boolean resetDownstream) {
        department = selected;
        binding.textDepartmentName.setText(selected.displayLabel());
        if (resetDownstream) {
            selectedSemester = null;
            selectedSection = null;
            selectedSubject = null;
            semesterItems.clear();
            sectionItems.clear();
            subjectItems.clear();
            refreshSemesterList();
            refreshSectionList();
            refreshSubjectList();
        }
        viewModel.loadSemesters(selected.getId());
        viewModel.loadSubjects(selected.getId());
    }

    private void showAddSemesterDialog() {
        if (!requireDepartment()) {
            return;
        }
        DialogAddSemesterBinding dialogBinding = DialogAddSemesterBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_semester_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = dialogBinding.editSemesterName.getText() != null
                            ? dialogBinding.editSemesterName.getText().toString().trim() : "";
                    String numberText = dialogBinding.editSemesterNumber.getText() != null
                            ? dialogBinding.editSemesterNumber.getText().toString().trim() : "";
                    if (name.isEmpty() || numberText.isEmpty()) {
                        ViewUtils.toastShort(this, getString(R.string.error_required_fields));
                        return;
                    }
                    int number;
                    try {
                        number = Integer.parseInt(numberText);
                    } catch (NumberFormatException e) {
                        ViewUtils.toastShort(this, getString(R.string.error_required_fields));
                        return;
                    }
                    viewModel.createSemester(department.getId(), name, number);
                    pendingSemesterName = name;
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showAddSectionDialog() {
        if (selectedSemester == null) {
            ViewUtils.toast(this, getString(R.string.error_select_semester));
            return;
        }
        DialogAddSectionBinding dialogBinding = DialogAddSectionBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_section_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = dialogBinding.editSectionName.getText() != null
                            ? dialogBinding.editSectionName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        ViewUtils.toastShort(this, getString(R.string.error_required_fields));
                        return;
                    }
                    viewModel.createSection(selectedSemester.getId(), name);
                    pendingSectionName = name;
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDeleteSemester(Semester semester) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_semester)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    if (department != null) {
                        viewModel.deleteSemester(semester.getId(), department.getId());
                    }
                    if (selectedSemester != null && semester.getId().equals(selectedSemester.getId())) {
                        selectedSemester = null;
                        selectedSection = null;
                        sectionItems.clear();
                        refreshSectionList();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDeleteSection(Section section) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_section)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    if (selectedSemester != null) {
                        viewModel.deleteSection(section.getId(), selectedSemester.getId());
                    }
                    if (selectedSection != null && section.getId().equals(selectedSection.getId())) {
                        selectedSection = null;
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void setupSemesterList() {
        semesterAdapter = new CatalogChoiceAdapter<>(Semester::toString, new CatalogChoiceAdapter.Listener<Semester>() {
            @Override
            public void onSelected(Semester item) {
                selectedSemester = item;
                selectedSection = null;
                sectionItems.clear();
                refreshSemesterList();
                refreshSectionList();
                if (selectedSemester != null) {
                    viewModel.loadSections(selectedSemester.getId());
                }
            }

            @Override
            public void onDelete(Semester item) {
                confirmDeleteSemester(item);
            }
        });
        binding.recyclerSemesters.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSemesters.setAdapter(semesterAdapter);
        refreshSemesterList();
    }

    private void setupSectionList() {
        sectionAdapter = new CatalogChoiceAdapter<>(Section::toString, new CatalogChoiceAdapter.Listener<Section>() {
            @Override
            public void onSelected(Section item) {
                selectedSection = item;
                refreshSectionList();
            }

            @Override
            public void onDelete(Section item) {
                confirmDeleteSection(item);
            }
        });
        binding.recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSections.setAdapter(sectionAdapter);
        refreshSectionList();
    }

    private void setupSubjectList() {
        subjectAdapter = new CatalogChoiceAdapter<>(Subject::toString, new CatalogChoiceAdapter.Listener<Subject>() {
            @Override
            public void onSelected(Subject item) {
                selectedSubject = item;
                refreshSubjectList();
            }

            @Override
            public void onDelete(Subject item) {
                confirmDeleteSubject(item);
            }
        });
        binding.recyclerSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSubjects.setAdapter(subjectAdapter);
        refreshSubjectList();
    }

    private void onSemestersLoaded(NetworkResult<List<Semester>> result) {
        if (result != null && result.isError()) {
            ViewUtils.toast(this, result.getMessage());
            return;
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        semesterItems.clear();
        semesterItems.addAll(result.getData());
        Semester match = null;
        if (pendingSemesterName != null) {
            for (Semester semester : semesterItems) {
                if (pendingSemesterName.equalsIgnoreCase(semester.getName())
                        || pendingSemesterName.equalsIgnoreCase(semester.toString())) {
                    match = semester;
                    break;
                }
            }
            pendingSemesterName = null;
        }
        if (match == null) {
            match = findByToStringOrId(semesterItems, selectedSemester);
        }
        selectedSemester = match;
        refreshSemesterList();
        if (selectedSemester != null) {
            viewModel.loadSections(selectedSemester.getId());
        } else {
            sectionItems.clear();
            selectedSection = null;
            refreshSectionList();
        }
    }

    private void onSectionsLoaded(NetworkResult<List<Section>> result) {
        if (result != null && result.isError()) {
            ViewUtils.toast(this, result.getMessage());
            return;
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        sectionItems.clear();
        sectionItems.addAll(result.getData());
        Section match = null;
        if (pendingSectionName != null) {
            for (Section section : sectionItems) {
                if (pendingSectionName.equalsIgnoreCase(section.getName())
                        || pendingSectionName.equalsIgnoreCase(section.toString())) {
                    match = section;
                    break;
                }
            }
            pendingSectionName = null;
        }
        if (match == null) {
            match = findByToStringOrId(sectionItems, selectedSection);
        }
        selectedSection = match;
        refreshSectionList();
    }

    private void onSubjectsLoaded(NetworkResult<List<Subject>> result) {
        if (result != null && result.isError()) {
            ViewUtils.toast(this, result.getMessage());
            return;
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        subjectItems.clear();
        subjectItems.addAll(result.getData());
        Subject match = null;
        if (pendingSubjectName != null) {
            for (Subject subject : subjectItems) {
                if (pendingSubjectName.equalsIgnoreCase(subject.getName())
                        || pendingSubjectName.equalsIgnoreCase(subject.toString())) {
                    match = subject;
                    break;
                }
            }
            pendingSubjectName = null;
        }
        if (match == null) {
            match = findByToStringOrId(subjectItems, selectedSubject);
        }
        selectedSubject = match;
        refreshSubjectList();
    }

    private void refreshSubjectList() {
        if (subjectAdapter == null) {
            return;
        }
        subjectAdapter.submit(subjectItems, selectedSubject == null ? null : selectedSubject.getId());
        boolean empty = subjectItems.isEmpty();
        binding.textSubjectEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerSubjects.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void refreshSemesterList() {
        if (semesterAdapter == null) {
            return;
        }
        semesterAdapter.submit(semesterItems, selectedSemester == null ? null : selectedSemester.getId());
        boolean empty = semesterItems.isEmpty();
        binding.textSemesterEmpty.setText(department == null
                ? getString(R.string.hint_choose_department)
                : getString(empty ? R.string.selection_empty_semesters : R.string.hint_choose_semester));
        binding.textSemesterEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerSemesters.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void refreshSectionList() {
        if (sectionAdapter == null) {
            return;
        }
        sectionAdapter.submit(sectionItems, selectedSection == null ? null : selectedSection.getId());
        boolean empty = sectionItems.isEmpty();
        binding.textSectionEmpty.setText(selectedSemester == null
                ? getString(R.string.hint_choose_semester)
                : getString(empty ? R.string.selection_empty_sections : R.string.hint_choose_section));
        binding.textSectionEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerSections.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDeleteSubject(Subject subject) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_delete_course, subject.toString()))
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    viewModel.deleteSubject(subject.getId(), department.getId());
                    if (selectedSubject != null && subject.getId().equals(selectedSubject.getId())) {
                        selectedSubject = null;
                        refreshSubjectList();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private <T> T findByToStringOrId(List<T> items, T current) {
        if (current == null) {
            return null;
        }
        String currentText = current.toString();
        String currentId = entityId(current);
        for (T item : items) {
            if (item == null) {
                continue;
            }
            if (currentId != null && currentId.equals(entityId(item))) {
                return item;
            }
            if (currentText != null && currentText.equals(item.toString())) {
                return item;
            }
        }
        return null;
    }

    private String entityId(Object entity) {
        if (entity instanceof Semester) {
            return ((Semester) entity).getId();
        }
        if (entity instanceof Section) {
            return ((Section) entity).getId();
        }
        if (entity instanceof Subject) {
            return ((Subject) entity).getId();
        }
        if (entity instanceof Department) {
            return ((Department) entity).getId();
        }
        return null;
    }

    private void showAddSubjectDialog() {
        if (!requireDepartment()) {
            return;
        }
        DialogAddSubjectBinding dialogBinding = DialogAddSubjectBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_subject_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String n = dialogBinding.editSubjectName.getText() == null
                            ? "" : dialogBinding.editSubjectName.getText().toString().trim();
                    String c = dialogBinding.editSubjectCode.getText() == null
                            ? "" : dialogBinding.editSubjectCode.getText().toString().trim();
                    if (n.isEmpty() || c.isEmpty()) {
                        ViewUtils.toastShort(this, getString(R.string.error_required_fields));
                        return;
                    }
                    viewModel.createSubject(department.getId(), n, c);
                    pendingSubjectName = n;
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private boolean requireDepartment() {
        if (department == null) {
            ViewUtils.toast(this, getString(R.string.error_select_department));
            return false;
        }
        return true;
    }

    private boolean validateSelection() {
        if (department == null || department.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_department));
            return false;
        }
        if (selectedSemester == null || selectedSemester.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_semester));
            return false;
        }
        if (selectedSection == null || selectedSection.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_section));
            return false;
        }
        if (selectedSubject == null || selectedSubject.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_subject));
            return false;
        }
        return true;
    }

    private boolean validateRosterSelection() {
        if (department == null || department.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_department));
            return false;
        }
        if (selectedSemester == null || selectedSemester.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_semester));
            return false;
        }
        if (selectedSection == null || selectedSection.getId() == null) {
            ViewUtils.toast(this, getString(R.string.error_select_section));
            return false;
        }
        return true;
    }

    private void proceedToManageStudents() {
        if (!validateSelection()) {
            return;
        }
        Intent intent = new Intent(this, StudentsActivity.class);
        intent.putExtra(Constants.EXTRA_DEPARTMENT, department);
        intent.putExtra(Constants.EXTRA_SEMESTER, selectedSemester);
        intent.putExtra(Constants.EXTRA_SECTION, selectedSection);
        intent.putExtra(Constants.EXTRA_SUBJECT, selectedSubject);
        intent.putExtra(Constants.EXTRA_DATE, DateUtils.today());
        intent.putExtra(Constants.EXTRA_MANAGE_STUDENTS, true);
        startActivity(intent);
    }

    private void proceedToViewRoster() {
        if (!validateRosterSelection()) {
            return;
        }
        Intent intent = new Intent(this, StudentsActivity.class);
        intent.putExtra(Constants.EXTRA_DEPARTMENT, department);
        intent.putExtra(Constants.EXTRA_SEMESTER, selectedSemester);
        intent.putExtra(Constants.EXTRA_SECTION, selectedSection);
        startActivity(intent);
    }
}
