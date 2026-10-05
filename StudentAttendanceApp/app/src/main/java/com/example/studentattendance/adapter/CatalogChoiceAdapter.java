package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemCatalogChoiceBinding;

/**
 * Compact selectable list for teacher-created courses and lectures.
 */
public class CatalogChoiceAdapter<T> extends RecyclerView.Adapter<CatalogChoiceAdapter.ChoiceViewHolder> {

    public interface Labeler<T> {
        String label(T item);
    }

    public interface Listener<T> {
        void onSelected(T item);

        void onDelete(T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Labeler<T> labeler;
    private final Listener<T> listener;
    private String selectedId;

    public CatalogChoiceAdapter(Labeler<T> labeler, Listener<T> listener) {
        this.labeler = labeler;
        this.listener = listener;
    }

    public void submit(List<T> next, String selectedId) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCatalogChoiceBinding binding = ItemCatalogChoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder holder, int position) {
        T item = items.get(position);
        String id = entityId(item);
        boolean selected = id != null && id.equals(selectedId);
        holder.binding.textChoiceLabel.setText(labeler.label(item));
        holder.binding.textChoiceLabel.setTypeface(null,
                selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        holder.binding.getRoot().setStrokeWidth(selected ? 2 : 1);
        holder.binding.getRoot().setStrokeColor(holder.itemView.getContext().getColor(
                selected ? R.color.brand_primary : R.color.divider));
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelected(item);
            }
        });
        holder.binding.buttonDeleteChoice.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String entityId(T item) {
        if (item instanceof com.example.studentattendance.model.Subject) {
            return ((com.example.studentattendance.model.Subject) item).getId();
        }
        if (item instanceof com.example.studentattendance.model.Lecture) {
            return ((com.example.studentattendance.model.Lecture) item).getId();
        }
        if (item instanceof com.example.studentattendance.model.Semester) {
            return ((com.example.studentattendance.model.Semester) item).getId();
        }
        if (item instanceof com.example.studentattendance.model.Section) {
            return ((com.example.studentattendance.model.Section) item).getId();
        }
        if (item instanceof com.example.studentattendance.model.Department) {
            return ((com.example.studentattendance.model.Department) item).getId();
        }
        return item == null ? null : item.toString();
    }

    static class ChoiceViewHolder extends RecyclerView.ViewHolder {
        final ItemCatalogChoiceBinding binding;

        ChoiceViewHolder(ItemCatalogChoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
