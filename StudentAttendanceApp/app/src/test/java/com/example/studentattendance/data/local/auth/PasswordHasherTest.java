package com.example.studentattendance.data.local.auth;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordHasherTest {

    @Test
    public void hashesAreNotThePlainPasswordAndVerifyRoundTrips() {
        PasswordHasher hasher = new PasswordHasher();
        String encoded = hasher.hash("password123".toCharArray());
        assertNotEquals("password123", encoded);
        assertTrue(encoded.startsWith("pbkdf2-sha1$"));
        assertTrue(hasher.verify("password123".toCharArray(), encoded));
        assertFalse(hasher.verify("wrong-pass1".toCharArray(), encoded));
        assertFalse(hasher.verify("password123".toCharArray(), "not-a-hash"));
    }

    @Test
    public void samePasswordProducesDifferentSalts() {
        PasswordHasher hasher = new PasswordHasher();
        String first = hasher.hash("password123".toCharArray());
        String second = hasher.hash("password123".toCharArray());
        assertNotEquals(first, second);
        assertTrue(hasher.verify("password123".toCharArray(), first));
        assertTrue(hasher.verify("password123".toCharArray(), second));
    }
}
