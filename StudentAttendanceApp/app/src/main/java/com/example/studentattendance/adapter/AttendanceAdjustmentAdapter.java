package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemAdjustmentBinding;
import com.example.studentattendance.model.AttendanceAdjustment;

/**
 * Phase 4: the permanent audit trail of teacher-controlled attendance
 * relief for one student - who changed it, by how much, when, and why.
 */
public class AttendanceAdjustmentAdapter extends RecyclerView.Adapter<AttendanceAdjustmentAdapter.AdjustmentViewHolder> {

    private final List<AttendanceAdjustment> adjustments = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault());

    public void submitList(List<AttendanceAdjustment> newAdjustments) {
        adjustments.clear();
        if (newAdjustments != null) {
            adjustments.addAll(newAdjustments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdjustmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdjustmentBinding binding = ItemAdjustmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdjustmentViewHolder(binding, dateFormat);
    }

    @Override
    public void onBindViewHolder(@NonNull AdjustmentViewHolder holder, int position) {
        holder.bind(adjustments.get(position));
    }

    @Override
    public int getItemCount() {
        return adjustments.size();
    }

    static class AdjustmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdjustmentBinding binding;
        private final SimpleDateFormat dateFormat;

        AdjustmentViewHolder(ItemAdjustmentBinding binding, SimpleDateFormat dateFormat) {
            super(binding.getRoot());
            this.binding = binding;
            this.dateFormat = dateFormat;
        }

        void bind(AttendanceAdjustment adjustment) {
            boolean credit = adjustment.getDelta() > 0;
            String sign = credit ? "+" : "";
            binding.textDelta.setText(sign + adjustment.getDelta() + " day" + (Math.abs(adjustment.getDelta()) == 1 ? "" : "s"));
            binding.textDelta.setTextColor(binding.getRoot().getResources().getColor(
                    credit ? R.color.status_present : R.color.status_absent, null));
            binding.textReason.setText(adjustment.getReason());
            binding.textAdjustedBy.setText(binding.getRoot().getContext()
                    .getString(R.string.adjustment_by_format, adjustment.getAdjustedByName()));
            binding.textAdjustmentDate.setText(dateFormat.format(new Date(adjustment.getCreatedAtMillis())));
        }
    }
}
