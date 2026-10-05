package com.example.studentattendance.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.adapter.NotificationAdapter;
import com.example.studentattendance.databinding.ActivityNotificationsBinding;
import com.example.studentattendance.model.Notification;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.reports.StudentReportDetailActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ViewUtils;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.example.studentattendance.service.RepositoryProvider.getInstance(this)
                .getAuthRepository().isLoggedIn()) {
            Intent login = new Intent(this, com.example.studentattendance.ui.login.LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
            finish();
            return;
        }
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        adapter = new NotificationAdapter(this::onNotificationClicked);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotifications.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.load());
        binding.includeError.buttonRetry.setOnClickListener(v -> viewModel.load());

        viewModel.getNotifications().observe(this, this::render);
        viewModel.load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            viewModel.markAllRead();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onNotificationClicked(Notification notification) {
        if (!notification.isRead()) {
            viewModel.markRead(notification.getId());
        }
        if (notification.getRelatedStudentId() != null && !notification.getRelatedStudentId().isEmpty()) {
            Intent intent = new Intent(this, StudentReportDetailActivity.class);
            intent.putExtra(Constants.EXTRA_STUDENT_ID, notification.getRelatedStudentId());
            startActivity(intent);
        }
    }

    private void render(NetworkResult<List<Notification>> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                if (!binding.swipeRefresh.isRefreshing()) {
                    showState(State.LOADING);
                }
                break;
            case SUCCESS:
                binding.swipeRefresh.setRefreshing(false);
                List<Notification> data = result.getData();
                PreferenceManager prefs = new PreferenceManager(this);
                if (!prefs.areNotificationsEnabled()) {
                    binding.includeEmpty.textEmptyTitle.setText(R.string.notifications_empty_title);
                    binding.includeEmpty.textEmptySubtitle.setText(R.string.profile_notifications);
                    showState(State.EMPTY);
                    return;
                }
                if (data == null || data.isEmpty()) {
                    binding.includeEmpty.textEmptyTitle.setText(R.string.notifications_empty_title);
                    binding.includeEmpty.textEmptySubtitle.setText(R.string.notifications_empty_subtitle);
                    showState(State.EMPTY);
                } else {
                    adapter.submitList(data);
                    showState(State.CONTENT);
                }
                break;
            case ERROR:
                binding.swipeRefresh.setRefreshing(false);
                binding.includeError.textErrorMessage.setText(result.getMessage());
                showState(State.ERROR);
                ViewUtils.toastLong(this, result.getMessage());
                break;
        }
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.swipeRefresh.setVisibility(state == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.includeLoading.layoutLoading.setVisibility(state == State.LOADING ? View.VISIBLE : View.GONE);
        binding.includeEmpty.layoutEmpty.setVisibility(state == State.EMPTY ? View.VISIBLE : View.GONE);
        binding.includeError.layoutError.setVisibility(state == State.ERROR ? View.VISIBLE : View.GONE);
    }
}
