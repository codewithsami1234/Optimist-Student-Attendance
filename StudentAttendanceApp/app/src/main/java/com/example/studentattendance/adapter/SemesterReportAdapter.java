package com.example.studentattendance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ItemCourseGroupHeaderBinding;
import com.example.studentattendance.databinding.ItemReportStudentBinding;
import com.example.studentattendance.model.AttendanceRecord;
import com.example.studentattendance.utils.SemesterReport;

/**
 * Course-grouped semester report rows with an expandable real-date history.
 */
public class SemesterReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_COURSE = 0;
    private static final int TYPE_STUDENT = 1;

    private final List<Row> rows = new ArrayList<>();
    private final Set<String> expandedKeys = new HashSet<>();

    public void submit(SemesterReport.Snapshot snapshot) {
        rows.clear();
        if (snapshot == null) {
            notifyDataSetChanged();
            return;
        }
        boolean showHeaders = snapshot.courses.size() > 1;
        for (SemesterReport.CourseGroup group : snapshot.courses) {
            if (group == null || group.students.isEmpty()) {
                continue;
            }
            if (showHeaders) {
                rows.add(Row.course(group));
            }
            for (SemesterReport.StudentRow student : group.students) {
                rows.add(Row.student(student));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).course != null ? TYPE_COURSE : TYPE_STUDENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_COURSE) {
            return new CourseViewHolder(ItemCourseGroupHeaderBinding.inflate(inflater, parent, false));
        }
        return new StudentViewHolder(ItemReportStudentBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof CourseViewHolder) {
            ((CourseViewHolder) holder).bind(row.course);
        } else if (holder instanceof StudentViewHolder) {
            ((StudentViewHolder) holder).bind(row.student, expandedKeys.contains(key(row.student)), v -> {
                String id = key(row.student);
                if (expandedKeys.contains(id)) {
                    expandedKeys.remove(id);
                } else {
                    expandedKeys.add(id);
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static String key(SemesterReport.StudentRow student) {
        String studentId = student.studentId == null
                ? student.rollNumber + "|" + student.studentName
                : student.studentId;
        return studentId + "|" + (student.subjectId == null ? "" : student.subjectId);
    }

    private static final class Row {
        final SemesterReport.CourseGroup course;
        final SemesterReport.StudentRow student;

        private Row(SemesterReport.CourseGroup course, SemesterReport.StudentRow student) {
            this.course = course;
            this.student = student;
        }

        static Row course(SemesterReport.CourseGroup group) {
            return new Row(group, null);
        }

        static Row student(SemesterReport.StudentRow student) {
            return new Row(null, student);
        }
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseGroupHeaderBinding binding;

        CourseViewHolder(ItemCourseGroupHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SemesterReport.CourseGroup group) {
            android.content.Context context = itemView.getContext();
            String department = blankTo(group.departmentName, context.getString(R.string.reports_filter_all));
            String semester = blankTo(group.semesterName, context.getString(R.string.reports_filter_all));
            String section = blankTo(group.sectionName, context.getString(R.string.label_no_section));
            binding.textCourseClass.setText(context.getString(
                    R.string.reports_course_class, department, semester, section));
            binding.textCourseTitle.setText(blankTo(group.subjectName, context.getString(R.string.reports_filter_all)));
            String code = group.subjectCode == null || group.subjectCode.trim().isEmpty()
                    ? context.getString(R.string.reports_filter_all)
                    : group.subjectCode;
            binding.textCourseCode.setText(context.getString(R.string.reports_course_code, code));
            binding.textCourseStats.setText(context.getString(
                    R.string.reports_course_stats, group.studentCount, group.sessionCount, group.averageRate));
            binding.textCourseBelow.setText(context.getString(R.string.reports_course_below, group.belowCount));
        }

        private static String blankTo(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value.trim();
        }
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportStudentBinding binding;
        private final AttendanceByDateAdapter datesAdapter = new AttendanceByDateAdapter();

        StudentViewHolder(ItemReportStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.recyclerStudentDates.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            binding.recyclerStudentDates.setNestedScrollingEnabled(false);
            binding.recyclerStudentDates.setAdapter(datesAdapter);
        }

        void bind(SemesterReport.StudentRow student, boolean expanded, View.OnClickListener toggle) {
            android.content.Context context = itemView.getContext();
            binding.textStudentName.setText(student.studentName);
            binding.textRollNumber.setText(student.rollNumber);
            binding.textStatusCounts.setText(context.getString(
                    R.string.reports_student_counts,
                    student.presentCount,
                    student.lateCount,
                    student.absentCount,
                    student.leaveCount,
                    student.recordedSessions));
            int percentage = (int) Math.round(student.attendanceRate);
            binding.textPercentage.setText(String.format(Locale.getDefault(), "%.1f%%",
                    student.attendanceRate));
            binding.progressAttendance.setProgress(Math.max(0, Math.min(100, percentage)));

            boolean below = student.belowThreshold;
            int color = ContextCompat.getColor(context, below ? R.color.error : R.color.status_present);
            binding.textPercentage.setTextColor(color);
            binding.progressAttendance.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(color));
            binding.textAttendanceStatus.setText(student.statusLabel());
            binding.textAttendanceStatus.setTextColor(color);
            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) binding.getRoot();
            card.setStrokeColor(below ? color : ContextCompat.getColor(context, R.color.divider));
            card.setStrokeWidth(below ? 2 : 0);

            List<AttendanceRecord> records = new ArrayList<>();
            for (SemesterReport.DateMark mark : student.dates) {
                if (mark == null || mark.date == null || mark.status == null) {
                    continue;
                }
                AttendanceRecord record = new AttendanceRecord();
                record.setDate(mark.date);
                record.setStatus(mark.status);
                records.add(record);
            }
            datesAdapter.submitList(records);
            binding.layoutDateDetail.setVisibility(expanded ? View.VISIBLE : View.GONE);
            binding.getRoot().setOnClickListener(toggle);
        }
    }
}
