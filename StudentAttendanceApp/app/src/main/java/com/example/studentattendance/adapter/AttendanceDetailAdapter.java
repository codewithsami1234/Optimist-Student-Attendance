package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemAttendanceDetailBinding;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.model.AttendanceStatus;

/**
 * Displays the individual student records within a single saved attendance
 * session, on the Session Detail screen.
 */
public class AttendanceDetailAdapter extends RecyclerView.Adapter<AttendanceDetailAdapter.DetailViewHolder> {

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
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceDetailBinding binding = ItemAttendanceDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceDetailBinding binding;

        DetailViewHolder(ItemAttendanceDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttendanceRecord record) {
            binding.textStudentName.setText(record.getStudentName());
            binding.textRollNumber.setText(record.getRollNumber());
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
