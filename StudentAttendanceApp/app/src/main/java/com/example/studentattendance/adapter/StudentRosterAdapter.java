package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.databinding.ItemStudentRosterBinding;
import com.example.studentattendance.model.Student;

/**
 * Roster list on Manage Students: name, roll number, and delete.
 */
public class StudentRosterAdapter extends RecyclerView.Adapter<StudentRosterAdapter.RosterViewHolder> {

    public interface OnStudentActionListener {
        void onStudentClicked(Student student);
        default void onStudentDelete(Student student) {
            onStudentClicked(student);
        }
    }

    private final List<Student> students = new ArrayList<>();
    private final OnStudentActionListener listener;

    public StudentRosterAdapter() {
        this(null);
    }

    public StudentRosterAdapter(OnStudentActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Student> newStudents) {
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentRosterBinding binding = ItemStudentRosterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RosterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RosterViewHolder holder, int position) {
        holder.bind(students.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class RosterViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentRosterBinding binding;

        RosterViewHolder(ItemStudentRosterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student, OnStudentActionListener listener) {
            binding.textAvatarInitials.setText(student.getAvatarInitials());
            String name = student.getName();
            if (student.isArchived()) {
                name = name + " (archived)";
            }
            binding.textStudentName.setText(name);
            binding.textStudentMeta.setText(student.getRollNumber());
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClicked(student);
                }
            });
            binding.buttonDeleteStudent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentDelete(student);
                }
            });
        }
    }
}
