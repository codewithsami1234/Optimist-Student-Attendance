package com.example.studentattendance.ui.splash;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentattendance.databinding.ActivitySplashBinding;
import com.example.studentattendance.service.RepositoryProvider;
import com.example.studentattendance.ui.login.LoginActivity;

/**
 * Animated launch screen. Startup routing is unchanged: a Room account is
 * not a login session, so every cold start continues to Sign-In.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long DISPLAY_MS = 2200;
    private static final long REDUCED_MOTION_MS = 450;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ActivitySplashBinding binding;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (motionEnabled()) {
            prepareHidden(binding.viewAttendanceMark, binding.dotPresent, binding.dotOrganized,
                    binding.dotReady, binding.textBrand, binding.textProduct, binding.textSubtitle);
            playIntro();
            handler.postDelayed(this::continueToApp, DISPLAY_MS);
        } else {
            binding.viewAttendanceMark.showComplete();
            handler.postDelayed(this::continueToApp, REDUCED_MOTION_MS);
        }
    }

    private void playIntro() {
        fadeScaleIn(binding.viewAttendanceMark, 0, 360);
        binding.viewAttendanceMark.play();

        fadeIn(binding.dotPresent, 560, 240);
        fadeIn(binding.dotOrganized, 680, 240);
        fadeIn(binding.dotReady, 800, 240);

        fadeSlideIn(binding.textBrand, 900, 380);
        fadeSlideIn(binding.textProduct, 1080, 340);
        fadeIn(binding.textSubtitle, 1260, 300);
    }

    private static void prepareHidden(View... views) {
        for (View view : views) {
            view.setAlpha(0f);
        }
    }

    private static void fadeScaleIn(View view, long delayMs, long durationMs) {
        view.setScaleX(0.88f);
        view.setScaleY(0.88f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delayMs)
                .setDuration(durationMs)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private static void fadeSlideIn(View view, long delayMs, long durationMs) {
        view.setTranslationY(view.getResources().getDisplayMetrics().density * 10f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(durationMs)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private static void fadeIn(View view, long delayMs, long durationMs) {
        view.animate()
                .alpha(1f)
                .setStartDelay(delayMs)
                .setDuration(durationMs)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private static boolean motionEnabled() {
        return Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled();
    }

    private void continueToApp() {
        if (finished || isFinishing()) {
            return;
        }
        finished = true;
        try {
            RepositoryProvider.getInstance(this).getAuthRepository().logout();
        } catch (RuntimeException ignored) {
            // Continue to Sign-In even if session cleanup fails.
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (binding != null) {
            binding.viewAttendanceMark.cancel();
            cancelViewAnimation(binding.viewAttendanceMark, binding.dotPresent,
                    binding.dotOrganized, binding.dotReady, binding.textBrand,
                    binding.textProduct, binding.textSubtitle);
            binding = null;
        }
        super.onDestroy();
    }

    private static void cancelViewAnimation(View... views) {
        for (View view : views) {
            view.animate().cancel();
        }
    }
}
