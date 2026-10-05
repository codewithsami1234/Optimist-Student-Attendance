package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemSemesterReviewBinding;
import com.example.studentattendance.utils.SemesterAttendance;

public class SemesterReviewAdapter extends RecyclerView.Adapter<SemesterReviewAdapter.Holder> {

    private final List<SemesterAttendance.Review> reviews = new ArrayList<>();

    public void submitList(List<SemesterAttendance.Review> items) {
        reviews.clear();
        if (items != null) {
            reviews.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSemesterReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemSemesterReviewBinding binding;

        Holder(ItemSemesterReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SemesterAttendance.Review review) {
            binding.textReviewName.setText(review.studentName);
            binding.textReviewMeta.setText(String.format(Locale.getDefault(), "%s · %s",
                    review.rollNumber == null ? "" : review.rollNumber,
                    itemView.getContext().getString(R.string.semester_review_sessions,
                            review.countableSessions)));
            binding.textReviewPercent.setText(String.format(Locale.US, "%.1f%%", review.attendanceRate));
            if (review.belowThreshold) {
                binding.textReviewStatus.setText(R.string.semester_review_below);
                int color = itemView.getContext().getResources().getColor(R.color.status_absent, null);
                binding.textReviewStatus.setTextColor(color);
                binding.textReviewPercent.setTextColor(color);
            } else {
                binding.textReviewStatus.setText(R.string.semester_review_ok);
                int color = itemView.getContext().getResources().getColor(R.color.status_present, null);
                binding.textReviewStatus.setTextColor(color);
                binding.textReviewPercent.setTextColor(color);
            }
        }
    }
}
