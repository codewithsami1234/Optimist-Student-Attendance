package com.example.studentattendance.ui.departments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.DepartmentAdapter;
import com.example.studentattendance.databinding.DialogSetupDepartmentBinding;
import com.example.studentattendance.databinding.FragmentDepartmentsBinding;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Department;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.students.StudentsActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.DateUtils;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Lists saved department/course cards. Tapping a card opens Manage Students
 * for that exact academic record.
 */
public class DepartmentsFragment extends Fragment {

    private FragmentDepartmentsBinding binding;
    private DepartmentsViewModel viewModel;
    private DepartmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDepartmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DepartmentsViewModel.class);

        adapter = new DepartmentAdapter(new DepartmentAdapter.OnDepartmentClickListener() {
            @Override
            public void onDepartmentClick(ClassSetup setup) {
                openManageStudents(setup);
            }

            @Override
            public void onDepartmentLongClick(ClassSetup setup) {
                showDepartmentActions(setup);
            }
        });
        binding.recyclerDepartments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDepartments.setAdapter(adapter);

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

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadDepartments());
        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.loadDepartments());
        binding.fabAddDepartment.setOnClickListener(v -> showAddDepartmentDialog());

        viewModel.getDepartments().observe(getViewLifecycleOwner(), this::render);
        viewModel.getCreateResult().observe(getViewLifecycleOwner(), this::handleCreateResult);
        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), this::handleDeleteResult);
        viewModel.loadDepartments();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadDepartments();
        }
    }

    private void showAddDepartmentDialog() {
        showAcademicSetupDialog(null);
    }

    private void handleCreateResult(NetworkResult<Department> result) {
        if (result == null) {
            return;
        }
        if (result.isLoading()) {
            if (binding != null && !binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(true);
            }
            return;
        }
        if (binding != null) {
            binding.swipeRefresh.setRefreshing(false);
        }
        if (result.isSuccess()) {
            ViewUtils.toastShort(requireContext(), getString(R.string.setup_saved));
        } else if (result.isError()) {
            ViewUtils.toastLong(requireContext(), result.getMessage());
        }
    }

    private void openManageStudents(ClassSetup setup) {
        if (setup == null || setup.getDepartment() == null || setup.getSemester() == null
                || setup.getSection() == null || setup.getSubject() == null) {
            ViewUtils.toastShort(requireContext(), getString(R.string.error_required_fields));
            return;
        }
        Intent intent = new Intent(requireContext(), StudentsActivity.class);
        intent.putExtra(Constants.EXTRA_DEPARTMENT, setup.getDepartment());
        intent.putExtra(Constants.EXTRA_SEMESTER, setup.getSemester());
        intent.putExtra(Constants.EXTRA_SECTION, setup.getSection());
        intent.putExtra(Constants.EXTRA_SUBJECT, setup.getSubject());
        intent.putExtra(Constants.EXTRA_DATE, DateUtils.today());
        intent.putExtra(Constants.EXTRA_MANAGE_STUDENTS, true);
        startActivity(intent);
    }

    private void showDepartmentActions(ClassSetup setup) {
        Department department = setup.getDepartment();
        String[] options = new String[]{
                getString(R.string.action_edit),
                getString(R.string.action_delete)
        };
        String fallbackTitle = department != null ? department.displayLabel() : getString(R.string.label_department);
        new AlertDialog.Builder(requireContext())
                .setTitle(setup.getSubject() != null ? setup.getSubject().toString() : fallbackTitle)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDepartmentDialog(setup);
                    } else if (which == 1) {
                        confirmDeleteClass(setup);
                    }
                })
                .show();
    }

    private void confirmDeleteClass(ClassSetup setup) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_class_title)
                .setMessage(R.string.confirm_delete_class_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    if (setup.getSubject() != null) {
                        viewModel.deleteClassSetup(setup.getSubject().getId());
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void handleDeleteResult(NetworkResult<Void> result) {
        if (result == null) {
            return;
        }
        if (result.isLoading()) {
            if (binding != null && !binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(true);
            }
            return;
        }
        if (binding != null) {
            binding.swipeRefresh.setRefreshing(false);
        }
        if (result.isSuccess()) {
            ViewUtils.toastShort(requireContext(), getString(R.string.class_deleted));
        } else if (result.isError()) {
            ViewUtils.toastLong(requireContext(), result.getMessage());
        }
    }

    private void showEditDepartmentDialog(ClassSetup setup) {
        if (setup == null || setup.getDepartment() == null || setup.getSemester() == null
                || setup.getSection() == null || setup.getSubject() == null) {
            ViewUtils.toastShort(requireContext(), getString(R.string.error_required_fields));
            return;
        }
        showAcademicSetupDialog(setup);
    }

    private void showAcademicSetupDialog(@Nullable ClassSetup existing) {
        DialogSetupDepartmentBinding dialogBinding =
                DialogSetupDepartmentBinding.inflate(getLayoutInflater());
        android.widget.ArrayAdapter<CharSequence> semesterAdapter = android.widget.ArrayAdapter.createFromResource(
                requireContext(), R.array.semester_numbers, R.layout.item_dropdown);
        dialogBinding.dropdownSemester.setAdapter(semesterAdapter);
        android.widget.ArrayAdapter<CharSequence> shiftAdapter = android.widget.ArrayAdapter.createFromResource(
                requireContext(), R.array.department_shift_labels, R.layout.item_dropdown);
        dialogBinding.dropdownShift.setAdapter(shiftAdapter);

        if (existing != null) {
            dialogBinding.editDepartmentName.setText(existing.getDepartment().getName());
            dialogBinding.dropdownShift.setText(
                    com.example.studentattendance.model.DepartmentShift.label(
                            existing.getDepartment().getShift()), false);
            dialogBinding.dropdownSemester.setText(
                    String.valueOf(existing.getSemester().getSemesterNumber()), false);
            String sectionName = existing.getSection().getName();
            dialogBinding.editSectionName.setText(sectionName != null ? sectionName : "");
            dialogBinding.editCourseName.setText(existing.getSubject().getName());
            dialogBinding.editCourseCode.setText(existing.getSubject().getCode());
        } else {
            dialogBinding.dropdownShift.setText(
                    com.example.studentattendance.model.DepartmentShift.label(
                            com.example.studentattendance.model.DepartmentShift.MORNING), false);
            dialogBinding.dropdownSemester.setText("1", false);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing != null ? R.string.edit_department_title : R.string.setup_department_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogBinding.editDepartmentName.getText() != null
                    ? dialogBinding.editDepartmentName.getText().toString().trim() : "";
            String shiftLabel = dialogBinding.dropdownShift.getText() != null
                    ? dialogBinding.dropdownShift.getText().toString().trim() : "";
            String shift = com.example.studentattendance.model.DepartmentShift.normalize(shiftLabel);
            String semesterText = dialogBinding.dropdownSemester.getText() != null
                    ? dialogBinding.dropdownSemester.getText().toString().trim() : "";
            String section = dialogBinding.editSectionName.getText() != null
                    ? dialogBinding.editSectionName.getText().toString().trim() : "";
            String courseName = dialogBinding.editCourseName.getText() != null
                    ? dialogBinding.editCourseName.getText().toString().trim() : "";
            String courseCode = dialogBinding.editCourseCode.getText() != null
                    ? dialogBinding.editCourseCode.getText().toString().trim() : "";
            int semesterNumber;
            try {
                semesterNumber = Integer.parseInt(semesterText);
            } catch (NumberFormatException ignored) {
                ViewUtils.toastShort(requireContext(), getString(R.string.error_semester_range));
                return;
            }
            if (semesterNumber < 1 || semesterNumber > 8) {
                ViewUtils.toastShort(requireContext(), getString(R.string.error_semester_range));
                return;
            }
            if (name.isEmpty() || courseName.isEmpty() || courseCode.isEmpty()) {
                ViewUtils.toastShort(requireContext(), getString(R.string.error_required_fields));
                return;
            }
            dialog.dismiss();
            if (existing == null) {
                viewModel.createAcademicSetup(name, shift, semesterNumber, section, courseName, courseCode);
            } else {
                viewModel.updateAcademicSetup(existing.getSubject().getId(), name, shift, semesterNumber,
                        section, courseName, courseCode);
            }
        }));
        dialog.show();
    }

    private void render(NetworkResult<List<ClassSetup>> result) {
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
                List<ClassSetup> data = result.getData();
                if (data == null || data.isEmpty()) {
                    binding.includeEmpty.textEmptyTitle.setText(R.string.departments_empty_title);
                    binding.includeEmpty.textEmptySubtitle.setText(R.string.departments_empty_subtitle);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
