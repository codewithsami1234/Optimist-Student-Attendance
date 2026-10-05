package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemAttendanceByDateBinding;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceStatus;
import com.example.studentattendance.utils.DateUtils;

/**
 * Phase 3: a single student's full attendance history (one row per date),
 * used on the Student Attendance Report detail screen.
 */
public class AttendanceByDateAdapter extends RecyclerView.Adapter<AttendanceByDateAdapter.DateViewHolder> {

    private final List<AttendanceRecord> records = new ArrayList<>();

    public void submitList(List<AttendanceRecord> newRecords) {
        records.clear();
        if (newRecords != null) {
            records.addAll(newRecords);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceByDateBinding binding = ItemAttendanceByDateBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceByDateBinding binding;

        DateViewHolder(ItemAttendanceByDateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttendanceRecord record) {
            binding.textDate.setText(DateUtils.toDisplayFormat(record.getDate()));
            binding.textStatus.setText(record.getStatus().getLabel());

            int bgRes;
            int colorRes;
            AttendanceStatus status = record.getStatus();
            if (status == AttendanceStatus.PRESENT) {
                bgRes = R.drawable.bg_status_present;
                colorRes = R.color.status_present_on;
            } else if (status == AttendanceStatus.ABSENT) {
                bgRes = R.drawable.bg_status_absent;
                colorRes = R.color.status_absent_on;
            } else if (status == AttendanceStatus.LATE) {
                bgRes = R.drawable.bg_status_late;
                colorRes = R.color.status_late_on;
            } else if (status == AttendanceStatus.LEAVE) {
                bgRes = R.drawable.bg_status_leave;
                colorRes = R.color.status_leave_on;
            } else {
                bgRes = R.drawable.bg_status_not_marked;
                colorRes = R.color.status_not_marked_on;
            }
            binding.textStatus.setBackgroundResource(bgRes);
            binding.textStatus.setTextColor(binding.getRoot().getContext().getResources().getColor(colorRes, null));
        }
    }
}
