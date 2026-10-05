package com.studentattendance.backend.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory JWT denylist so logout actually invalidates a token until it expires. */
@Component
public class TokenBlacklist {

    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public void revoke(String token, long expiresAtMillis) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokens.put(token, expiresAtMillis);
        purge();
    }

    public boolean isRevoked(String token) {
        Long expiry = tokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry < Instant.now().toEpochMilli()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    private void purge() {
        long now = Instant.now().toEpochMilli();
        tokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
