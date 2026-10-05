package com.example.studentattendance.data.local.auth;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2 password hashing for local accounts. The encoded form is
 * {@code pbkdf2-sha1$iterations$saltHex$hashHex} so it can be stored in SQLite
 * without Android-only APIs (minSdk 24).
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final String PREFIX = "pbkdf2-sha1";

    private final SecureRandom random = new SecureRandom();

    public String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password is required.");
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$" + toHex(salt) + "$" + toHex(derived);
    }

    public boolean verify(char[] password, String encoded) {
        if (password == null || encoded == null || encoded.isEmpty()) {
            return false;
        }
        String[] parts = encoded.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        byte[] salt = fromHex(parts[2]);
        byte[] expected = fromHex(parts[3]);
        if (salt.length == 0 || expected.length == 0) {
            return false;
        }
        byte[] actual = derive(password, salt, iterations);
        return constantTimeEquals(expected, actual);
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available on this device.", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            sb.append(String.format("%02x", value & 0xff));
        }
        return sb.toString();
    }

    static byte[] fromHex(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                return new byte[0];
            }
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }
}
