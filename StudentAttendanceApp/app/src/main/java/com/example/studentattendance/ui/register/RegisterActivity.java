package com.example.studentattendance.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ActivityRegisterBinding;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.login.LoginActivity;
import com.example.studentattendance.utils.ValidationUtils;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Email registration against the local Room database. After the account is
 * created the teacher is sent to Sign-In.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.buttonRegister.setOnClickListener(v -> attemptRegister());
        binding.textGoToLogin.setOnClickListener(v -> finish());
        viewModel.getRegisterResult().observe(this, this::handleRegisterResult);
    }

    private void attemptRegister() {
        binding.inputLayoutName.setError(null);
        binding.inputLayoutEmail.setError(null);
        binding.inputLayoutPassword.setError(null);
        binding.inputLayoutConfirmPassword.setError(null);
        binding.textRegisterError.setVisibility(View.GONE);

        String name = textOf(binding.editName);
        String email = textOf(binding.editEmail);
        String designation = textOf(binding.editDesignation);
        String password = binding.editPassword.getText() == null ? "" : binding.editPassword.getText().toString();
        String confirmPassword = binding.editConfirmPassword.getText() == null
                ? "" : binding.editConfirmPassword.getText().toString();

        boolean valid = true;
        if (!ValidationUtils.isNotEmpty(name) || name.length() < 2) {
            binding.inputLayoutName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.inputLayoutEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        if (!ValidationUtils.isValidNewPassword(password)) {
            binding.inputLayoutPassword.setError(getString(R.string.error_password_too_short));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            valid = false;
        }
        if (!valid) {
            return;
        }

        viewModel.register(name, email, password, designation.isEmpty() ? null : designation);
    }

    private void goToSignIn(User user) {
        RepositoryProvider.getInstance(this).getAuthRepository().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_ACCOUNT_CREATED, true);
        if (user != null && user.getEmail() != null) {
            intent.putExtra(LoginActivity.EXTRA_REGISTERED_EMAIL, user.getEmail());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void handleRegisterResult(NetworkResult<User> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                setBusy(true);
                binding.textRegisterError.setVisibility(View.GONE);
                break;
            case SUCCESS:
                setBusy(false);
                goToSignIn(result.getData());
                break;
            case ERROR:
                setBusy(false);
                binding.textRegisterError.setVisibility(View.VISIBLE);
                binding.textRegisterError.setText(result.getMessage());
                ViewUtils.toast(this, result.getMessage());
                break;
        }
    }

    private void setBusy(boolean busy) {
        binding.progressRegister.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!busy);
        binding.textGoToLogin.setEnabled(!busy);
        binding.editName.setEnabled(!busy);
        binding.editEmail.setEnabled(!busy);
        binding.editDesignation.setEnabled(!busy);
        binding.editPassword.setEnabled(!busy);
        binding.editConfirmPassword.setEnabled(!busy);
    }
}
