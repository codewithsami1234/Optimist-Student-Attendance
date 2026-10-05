package com.example.studentattendance.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Reusable form-validation helpers shared across Login, Register, Add Student,
 * and Edit Profile screens.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private ValidationUtils() {
    }

    public static boolean isValidEmail(CharSequence email) {
        if (email == null) {
            return false;
        }
        String value = email.toString().trim();
        if (value.isEmpty() || value.length() > 254 || value.contains("..")) {
            return false;
        }
        int at = value.indexOf('@');
        if (at <= 0 || at != value.lastIndexOf('@')) {
            return false;
        }
        String domain = value.substring(at + 1);
        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        return EMAIL_PATTERN.matcher(value).matches();
    }

    public static boolean isNotEmpty(CharSequence value) {
        return value != null && value.toString().trim().length() > 0;
    }

    public static boolean isValidPassword(CharSequence password) {
        return password != null && password.length() >= 8;
    }

    /** Registration requires a letter and a number, matching the backend. */
    public static boolean isValidNewPassword(CharSequence password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPhone(CharSequence phone) {
        if (TextUtils.isEmpty(phone)) {
            return true; // phone is optional in this app
        }
        return phone.toString().matches("^[0-9+()\\-\\s]{7,15}$");
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return true;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                || caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
    }

    public static boolean isEmulator() {
        String fingerprint = Build.FINGERPRINT == null ? "" : Build.FINGERPRINT;
        String model = Build.MODEL == null ? "" : Build.MODEL;
        String product = Build.PRODUCT == null ? "" : Build.PRODUCT;
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
        return fingerprint.startsWith("generic")
                || fingerprint.contains("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for")
                || manufacturer.contains("Genymotion")
                || product.contains("sdk_gphone")
                || product.equals("google_sdk")
                || product.contains("sdk_phone");
    }
}
