package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemReportStudentBinding;
import com.example.studentattendance.model.StudentReport;
import com.example.studentattendance.utils.AttendancePolicy;

/**
 * Student-wise report rows: name, roll, P/A/Late/Leave, and attendance %.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(StudentReport report);
    }

    private final List<StudentReport> reports = new ArrayList<>();
    private final OnStudentClickListener listener;
    private double threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;

    public ReportAdapter() {
        this(null);
    }

    public ReportAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StudentReport> newReports) {
        submitList(newReports, threshold);
    }

    public void submitList(List<StudentReport> newReports, double lowThreshold) {
        reports.clear();
        if (newReports != null) {
            reports.addAll(newReports);
        }
        this.threshold = lowThreshold <= 0 ? AttendancePolicy.DEFAULT_LOW_THRESHOLD : lowThreshold;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportStudentBinding binding = ItemReportStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position), listener, threshold);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportStudentBinding binding;

        ReportViewHolder(ItemReportStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StudentReport report, OnStudentClickListener listener, double threshold) {
            binding.textStudentName.setText(report.getStudentName());
            binding.textRollNumber.setText(report.getRollNumber());
            binding.textStatusCounts.setText(itemView.getContext().getString(
                    R.string.reports_student_counts,
                    report.getPresentDays(),
                    report.getLateDays(),
                    report.getAbsentDays(),
                    report.getLeaveDays(),
                    report.getTotalDays()));
            int percentage = (int) Math.round(report.getAttendancePercentage());
            binding.textPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
            binding.progressAttendance.setProgress(percentage);
            boolean below = report.getAttendancePercentage() < threshold && report.getTotalDays() > 0;
            int color = ContextCompat.getColor(itemView.getContext(),
                    below ? R.color.error : R.color.status_present);
            binding.textPercentage.setTextColor(color);
            binding.progressAttendance.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(color));
            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onStudentClick(report));
            }
        }
    }
}
