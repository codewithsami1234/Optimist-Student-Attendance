package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.databinding.ItemDepartmentBinding;
import com.example.studentattendance.model.ClassSetup;
import com.example.studentattendance.model.Subject;

/**
 * Displays saved department/course cards on the Departments screen.
 */
public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    public interface OnDepartmentClickListener {
        void onDepartmentClick(ClassSetup setup);
        default void onDepartmentLongClick(ClassSetup setup) {}
    }

    private final List<ClassSetup> setups = new ArrayList<>();
    private final OnDepartmentClickListener listener;

    public DepartmentAdapter(OnDepartmentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClassSetup> newSetups) {
        setups.clear();
        if (newSetups != null) {
            setups.addAll(newSetups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepartmentBinding binding = ItemDepartmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DepartmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        holder.bind(setups.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return setups.size();
    }

    static class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepartmentBinding binding;

        DepartmentViewHolder(ItemDepartmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassSetup setup, OnDepartmentClickListener listener) {
            String departmentName = setup.getDepartment() != null
                    ? setup.getDepartment().displayLabel() : "";
            binding.textDepartmentName.setText(departmentName);
            binding.textClassSemester.setText(setup.semesterLabel());
            if (setup.hasNamedSection()) {
                binding.textClassSection.setVisibility(View.VISIBLE);
                binding.textClassSection.setText(setup.getSection().getName().trim());
            } else {
                binding.textClassSection.setVisibility(View.GONE);
            }
            Subject subject = setup.getSubject();
            String courseName = subject != null && subject.getName() != null ? subject.getName() : "";
            String courseCode = subject != null && subject.getCode() != null ? subject.getCode() : "";
            binding.textClassCourse.setText(courseName);
            binding.textClassCourseCode.setText(courseCode.toUpperCase(Locale.getDefault()));
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDepartmentClick(setup);
                }
            });
            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDepartmentLongClick(setup);
                    return true;
                }
                return false;
            });
        }
    }
}
