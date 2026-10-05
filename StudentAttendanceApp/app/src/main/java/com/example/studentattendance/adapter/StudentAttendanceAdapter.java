package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemStudentAttendanceBinding;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.model.Student;

/**
 * Roster row for Mark Attendance. Radio buttons set Present, Absent, Late,
 * or Leave and the live summary updates immediately.
 */
public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChanged(String studentId, AttendanceStatus status);
    }

    private final List<Student> allStudents = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final Map<String, AttendanceStatus> statusMap = new LinkedHashMap<>();
    private final OnStatusChangeListener listener;

    public StudentAttendanceAdapter(OnStatusChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Student> newStudents) {
        submitList(newStudents, null);
    }

    public void submitList(List<Student> newStudents, Map<String, AttendanceStatus> initialStatuses) {
        allStudents.clear();
        students.clear();
        statusMap.clear();
        if (newStudents != null) {
            allStudents.addAll(newStudents);
            students.addAll(newStudents);
            for (Student s : newStudents) {
                AttendanceStatus initial = initialStatuses != null ? initialStatuses.get(s.getId()) : null;
                statusMap.put(s.getId(), initial != null ? initial : AttendanceStatus.NOT_MARKED);
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        students.clear();
        for (Student s : allStudents) {
            if (needle.isEmpty()
                    || (s.getName() != null && s.getName().toLowerCase().contains(needle))
                    || (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(needle))) {
                students.add(s);
            }
        }
        notifyDataSetChanged();
    }

    public void markAllPresent() {
        for (Student s : allStudents) {
            statusMap.put(s.getId(), AttendanceStatus.PRESENT);
        }
        notifyDataSetChanged();
        if (listener != null) {
            for (Student s : allStudents) {
                listener.onStatusChanged(s.getId(), AttendanceStatus.PRESENT);
            }
        }
    }

    public Map<String, AttendanceStatus> getStatusMap() {
        return statusMap;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentAttendanceBinding binding = ItemStudentAttendanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        AttendanceStatus current = statusMap.get(student.getId());
        holder.bind(student, current, (id, status) -> {
            statusMap.put(id, status);
            if (listener != null) {
                listener.onStatusChanged(id, status);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentAttendanceBinding binding;

        StudentViewHolder(ItemStudentAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student, AttendanceStatus currentStatus, OnStatusChangeListener rowListener) {
            binding.textStudentName.setText(student.getName());
            binding.textRollNumber.setText(student.getRollNumber());
            binding.textAvatarInitials.setText(student.getAvatarInitials());

            AttendanceStatus status = currentStatus == null ? AttendanceStatus.NOT_MARKED : currentStatus;
            binding.groupStatus.setOnCheckedChangeListener(null);
            binding.groupStatus.clearCheck();
            if (status == AttendanceStatus.PRESENT) {
                binding.groupStatus.check(R.id.radioPresent);
            } else if (status == AttendanceStatus.ABSENT) {
                binding.groupStatus.check(R.id.radioAbsent);
            } else if (status == AttendanceStatus.LATE) {
                binding.groupStatus.check(R.id.radioLate);
            } else if (status == AttendanceStatus.LEAVE) {
                binding.groupStatus.check(R.id.radioLeave);
            }
            binding.groupStatus.setOnCheckedChangeListener((group, checkedId) -> {
                AttendanceStatus next = AttendanceStatus.NOT_MARKED;
                if (checkedId == R.id.radioPresent) {
                    next = AttendanceStatus.PRESENT;
                } else if (checkedId == R.id.radioAbsent) {
                    next = AttendanceStatus.ABSENT;
                } else if (checkedId == R.id.radioLate) {
                    next = AttendanceStatus.LATE;
                } else if (checkedId == R.id.radioLeave) {
                    next = AttendanceStatus.LEAVE;
                }
                if (next != AttendanceStatus.NOT_MARKED) {
                    rowListener.onStatusChanged(student.getId(), next);
                }
            });
        }
    }
}
