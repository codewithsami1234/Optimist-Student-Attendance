package com.example.studentattendance.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.studentattendance.databinding.ItemNotificationBinding;
import com.example.studentattendance.model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    public interface Listener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> items = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Notification> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        Holder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification, Listener listener) {
            binding.textMessage.setText(notification.getMessage());
            String type = notification.getType() == null ? "" : notification.getType().replace('_', ' ');
            binding.textType.setText(type);
            binding.textTime.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(new Date(notification.getCreatedAtMillis())));
            binding.textMessage.setTypeface(null, notification.isRead() ? Typeface.NORMAL : Typeface.BOLD);
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}
