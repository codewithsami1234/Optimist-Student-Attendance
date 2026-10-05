package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.studentattendance.databinding.ItemAttendanceSessionBinding;
import com.example.studentattendance.model.AttendanceSession;
import com.example.studentattendance.utils.DateUtils;

/**
 * Displays saved attendance sessions on the Attendance History screen (and,
 * in a smaller capacity, as "recent activity" on the Dashboard).
 */
public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(AttendanceSession session);
    }

    private final List<AttendanceSession> sessions = new ArrayList<>();
    private final OnSessionClickListener listener;

    public AttendanceHistoryAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AttendanceSession> newSessions) {
        sessions.clear();
        if (newSessions != null) {
            sessions.addAll(newSessions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceSessionBinding binding = ItemAttendanceSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SessionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.bind(sessions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceSessionBinding binding;

        SessionViewHolder(ItemAttendanceSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttendanceSession session, OnSessionClickListener listener) {
            binding.textSessionDate.setText(DateUtils.toDisplayFormat(session.getDate()));
            binding.textSessionClass.setText(com.example.studentattendance.utils.HistoryFilter.formatClassLine(session));
            binding.textSessionSummary.setText(String.format(Locale.getDefault(),
                    "P: %d · A: %d · L: %d · Lv: %d",
                    session.getPresentCount(), session.getAbsentCount(),
                    session.getLateCount(), session.getLeaveCount()));
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });
        }
    }
}
