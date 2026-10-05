package com.example.studentattendance.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.DialogEditProfileBinding;
import com.example.studentattendance.databinding.FragmentProfileBinding;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.importexport.ImportExportActivity;
import com.example.studentattendance.ui.login.LoginActivity;
import com.example.studentattendance.utils.ValidationUtils;
import com.example.studentattendance.utils.ViewUtils;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        bindUser(viewModel.getCurrentUser());
        binding.textApiUrlValue.setText(R.string.profile_local_storage);

        binding.switchDarkMode.setChecked(viewModel.isDarkModeEnabled());
        binding.switchNotifications.setChecked(viewModel.areNotificationsEnabled());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkModeEnabled(isChecked);
            requireActivity().recreate();
        });
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotificationsEnabled(isChecked);
            if (isChecked && android.os.Build.VERSION.SDK_INT >= 33) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    androidx.core.app.ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 47);
                }
            }
        });

        binding.buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.rowThreshold.setOnClickListener(v -> showThresholdDialog());
        binding.rowApiUrl.setClickable(false);
        binding.rowApiUrl.setFocusable(false);
        binding.rowImportExport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ImportExportActivity.class)));
        binding.rowAbout.setOnClickListener(v -> showAboutDialog());
        binding.rowDeleteAllData.setOnClickListener(v -> confirmDeleteAllData());
        binding.buttonLogout.setOnClickListener(v -> confirmLogout());

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), this::handleUpdateResult);
        viewModel.getResetResult().observe(getViewLifecycleOwner(), this::handleResetResult);
    }

    private void bindUser(User user) {
        if (user == null) {
            return;
        }
        binding.textAvatarInitials.setText(user.getAvatarInitials());
        binding.textUserName.setText(user.getName());
        binding.textUserEmail.setText(user.getEmail());
        binding.textUserDesignation.setText(user.getDesignation());
        binding.textThresholdValue.setText(String.format(Locale.getDefault(),
                "%.0f%%", user.getLowAttendanceThreshold()));
    }

    private void showEditProfileDialog() {
        User user = viewModel.getCurrentUser();
        if (user == null) {
            return;
        }
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.editName.setText(user.getName());
        dialogBinding.editDesignation.setText(user.getDesignation());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            dialogBinding.inputLayoutName.setError(null);
            CharSequence name = dialogBinding.editName.getText();
            if (!ValidationUtils.isNotEmpty(name)) {
                dialogBinding.inputLayoutName.setError(getString(R.string.error_name_required));
                return;
            }
            String designation = dialogBinding.editDesignation.getText() == null
                    ? "" : dialogBinding.editDesignation.getText().toString().trim();
            viewModel.updateProfile(name.toString().trim(), designation);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void showThresholdDialog() {
        User user = viewModel.getCurrentUser();
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("75");
        input.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary));
        input.setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_hint));
        if (user != null) {
            input.setText(String.format(Locale.US, "%.0f", user.getLowAttendanceThreshold()));
        }
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_threshold)
                .setView(input)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    try {
                        double value = Double.parseDouble(input.getText().toString().trim());
                        viewModel.updateThreshold(value);
                    } catch (NumberFormatException e) {
                        ViewUtils.toast(requireContext(), getString(R.string.error_required_fields));
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void handleUpdateResult(NetworkResult<User> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isSuccess()) {
            bindUser(result.getData());
            ViewUtils.toast(requireContext(), "Profile updated");
        } else if (result.isError()) {
            ViewUtils.toastLong(requireContext(), result.getMessage());
        }
    }

    private void confirmDeleteAllData() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_delete_all_title)
                .setMessage(R.string.profile_delete_all_message)
                .setPositiveButton(R.string.profile_delete_all_continue, (dialog, which) -> confirmDeleteAllDataAgain())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDeleteAllDataAgain() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_delete_all_confirm_title)
                .setMessage(R.string.profile_delete_all_confirm_message)
                .setPositiveButton(R.string.profile_delete_all_data, (dialog, which) -> viewModel.resetAcademicData())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void handleResetResult(NetworkResult<Boolean> result) {
        if (result == null || result.isLoading()) {
            return;
        }
        if (result.isSuccess()) {
            ViewUtils.toastLong(requireContext(), getString(R.string.profile_delete_all_success));
        } else if (result.isError()) {
            ViewUtils.toastLong(requireContext(), result.getMessage());
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_about)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performLogout() {
        RepositoryProvider.getInstance(requireContext()).getAuthRepository().logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
