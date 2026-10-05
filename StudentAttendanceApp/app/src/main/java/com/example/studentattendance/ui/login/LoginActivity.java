package com.example.studentattendance.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentattendance.R;
import com.example.studentattendance.databinding.ActivityLoginBinding;
import com.example.studentattendance.model.User;
import com.example.studentattendance.network.NetworkResult;
import com.example.studentattendance.ui.main.MainActivity;
import com.example.studentattendance.utils.ValidationUtils;
import com.example.studentattendance.utils.ViewUtils;

/**
 * Email/password against the local Room database. One teacher account per device.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_EXPIRED = "extra_session_expired";
    public static final String EXTRA_REGISTERED_EMAIL = "extra_registered_email";
    public static final String EXTRA_ACCOUNT_CREATED = "extra_account_created";

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        applyRegisteredEmail(getIntent());

        if (getIntent().getBooleanExtra(EXTRA_SESSION_EXPIRED, false)) {
            ViewUtils.toastLong(this, getString(R.string.error_session_expired));
        }

        binding.checkboxRememberMe.setChecked(viewModel.isRememberMe());
        if (binding.editEmail.getText() == null || binding.editEmail.getText().toString().isEmpty()) {
            String remembered = viewModel.getRememberedEmail();
            if (remembered != null && !remembered.isEmpty()) {
                binding.editEmail.setText(remembered);
            }
        }

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.buttonGoogle.setOnClickListener(v -> attemptGoogleSignIn());
        binding.textGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.studentattendance.ui.register.RegisterActivity.class)));
        viewModel.getLoginResult().observe(this, this::handleLoginResult);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyRegisteredEmail(intent);
    }

    private void applyRegisteredEmail(Intent intent) {
        if (intent == null || binding == null) {
            return;
        }
        boolean created = intent.getBooleanExtra(EXTRA_ACCOUNT_CREATED, false);
        String email = intent.getStringExtra(EXTRA_REGISTERED_EMAIL);
        if (!created && (email == null || email.isEmpty())) {
            return;
        }
        if (email != null && !email.isEmpty()) {
            binding.editEmail.setText(email);
        }
        binding.textAccountCreated.setVisibility(View.VISIBLE);
        binding.textAccountCreated.setText(R.string.register_success);
        ViewUtils.toastLong(this, getString(R.string.register_success));
        intent.removeExtra(EXTRA_REGISTERED_EMAIL);
        intent.removeExtra(EXTRA_ACCOUNT_CREATED);
    }

    private void attemptLogin() {
        binding.inputLayoutEmail.setError(null);
        binding.inputLayoutPassword.setError(null);
        binding.textLoginError.setVisibility(View.GONE);

        String email = binding.editEmail.getText() == null ? "" : binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText() == null ? "" : binding.editPassword.getText().toString();

        boolean valid = true;
        if (!ValidationUtils.isValidEmail(email)) {
            binding.inputLayoutEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.inputLayoutPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }
        if (!valid) {
            return;
        }

        viewModel.login(email, password, binding.checkboxRememberMe.isChecked());
    }

    private void attemptGoogleSignIn() {
        binding.textLoginError.setVisibility(View.GONE);
        setBusy(true);
        com.example.studentattendance.utils.GoogleSignInHelper.requestIdToken(this,
                new com.example.studentattendance.utils.GoogleSignInHelper.Callback() {
                    @Override
                    public void onGoogleIdToken(@androidx.annotation.NonNull String idToken) {
                        viewModel.loginWithGoogle(idToken, binding.checkboxRememberMe.isChecked());
                    }

                    @Override
                    public void onGoogleError(@androidx.annotation.NonNull String message) {
                        setBusy(false);
                        binding.textLoginError.setVisibility(View.VISIBLE);
                        binding.textLoginError.setText(message);
                        ViewUtils.toast(LoginActivity.this, message);
                    }
                });
    }

    private void handleLoginResult(NetworkResult<User> result) {
        if (result == null) {
            return;
        }
        switch (result.getStatus()) {
            case LOADING:
                setBusy(true);
                binding.textLoginError.setVisibility(View.GONE);
                break;
            case SUCCESS:
                setBusy(false);
                goToDashboard();
                break;
            case ERROR:
                setBusy(false);
                binding.textLoginError.setVisibility(View.VISIBLE);
                binding.textLoginError.setText(result.getMessage());
                ViewUtils.toast(this, result.getMessage());
                break;
        }
    }

    private void setBusy(boolean busy) {
        binding.progressLogin.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!busy);
        binding.buttonGoogle.setEnabled(!busy);
        binding.textGoToRegister.setEnabled(!busy);
        binding.checkboxRememberMe.setEnabled(!busy);
        binding.editEmail.setEnabled(!busy);
        binding.editPassword.setEnabled(!busy);
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
