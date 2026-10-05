package com.example.studentattendance.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ActivityMainBinding;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.login.LoginActivity;
import com.example.studentattendance.ui.notifications.NotificationsActivity;
import com.example.studentattendance.utils.Constants;
import com.example.studentattendance.utils.PreferenceManager;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Hosts the five bottom-navigation destinations plus toolbar actions for
 * notifications, profile, theme, about, and logout.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TextView notificationBadge;
    private long unreadCount;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RepositoryProvider.getInstance(this).getAuthRepository().isLoggedIn()) {
            Intent login = new Intent(this, LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
            finish();
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
        handleOpenTab(getIntent());
        requestNotificationPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOpenTab(intent);
    }

    private void handleOpenTab(Intent intent) {
        if (intent == null || navController == null) {
            return;
        }
        int tab = intent.getIntExtra(Constants.EXTRA_OPEN_TAB, 0);
        if (tab == R.id.nav_dashboard || tab == R.id.nav_departments
                || tab == R.id.nav_history || tab == R.id.nav_reports
                || tab == R.id.nav_profile) {
            binding.bottomNavigation.setSelectedItemId(tab);
            intent.removeExtra(Constants.EXTRA_OPEN_TAB);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUnreadBadge();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 47);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notifications);
        if (item != null) {
            item.setActionView(R.layout.view_notification_badge);
            View actionView = item.getActionView();
            if (actionView != null) {
                notificationBadge = actionView.findViewById(R.id.textBadge);
                actionView.setOnClickListener(v ->
                        startActivity(new Intent(this, NotificationsActivity.class)));
                updateBadgeView();
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
            return true;
        } else if (id == R.id.action_theme) {
            toggleTheme();
            return true;
        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.profile_about)
                    .setMessage(R.string.about_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return true;
        } else if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshUnreadBadge() {
        PreferenceManager prefs = new PreferenceManager(this);
        if (!prefs.areNotificationsEnabled()) {
            unreadCount = 0;
            updateBadgeView();
            return;
        }
        RepositoryProvider.getInstance(this).getNotificationRepository()
                .getUnreadCount(new ApiCallback<Long>() {
                    @Override
                    public void onSuccess(Long data) {
                        unreadCount = data == null ? 0 : data;
                        updateBadgeView();
                    }

                    @Override
                    public void onError(String message) {
                        unreadCount = 0;
                        updateBadgeView();
                    }
                });
    }

    private void updateBadgeView() {
        if (notificationBadge == null) {
            return;
        }
        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private void toggleTheme() {
        PreferenceManager prefs = new PreferenceManager(this);
        boolean dark = !prefs.isDarkModeEnabled();
        prefs.setDarkModeEnabled(dark);
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
        ViewUtils.toastShort(this, getString(dark ? R.string.theme_switched_dark : R.string.theme_switched_light));
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performLogout() {
        RepositoryProvider.getInstance(this).getAuthRepository().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
