package com.example.studentattendance.network;

import com.example.studentattendance.BuildConfig;
import com.example.studentattendance.utils.ValidationUtils;

/**
 * Production devices speak only to the configured HTTPS cloud API.
 * The emulator loopback {@code 10.0.2.2} is allowed solely in debug builds
 * running on an emulator, never on a physical device or a release APK.
 */
public final class ApiUrlPolicy {

    private ApiUrlPolicy() {
    }

    public static String defaultCloudUrl() {
        String configured = BuildConfig.API_BASE_URL;
        if (isHttpsCloudUrl(configured)) {
            return normalize(configured);
        }
        return "https://student-attendance-api.onrender.com/";
    }

    public static String normalize(String url) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    public static boolean isEmulatorLoopback(String url) {
        if (url == null) {
            return false;
        }
        String value = url.toLowerCase();
        return value.contains("10.0.2.2")
                || value.contains("10.0.3.2")
                || value.contains("127.0.0.1")
                || value.contains("localhost");
    }

    public static boolean isHttpsCloudUrl(String url) {
        String normalized = normalize(url);
        return normalized.startsWith("https://") && !isEmulatorLoopback(normalized);
    }

    /**
     * Physical devices and release builds: HTTPS cloud only.
     * Debug emulator: HTTPS, or local HTTP to the host machine.
     */
    public static boolean isAllowed(String url) {
        String normalized = normalize(url);
        if (normalized.isEmpty()) {
            return false;
        }
        if (isHttpsCloudUrl(normalized)) {
            return true;
        }
        return BuildConfig.DEBUG
                && ValidationUtils.isEmulator()
                && normalized.startsWith("http://")
                && isEmulatorLoopback(normalized);
    }

    public static String resolve(String candidate) {
        if (isAllowed(candidate)) {
            return normalize(candidate);
        }
        return defaultCloudUrl();
    }
}
