package com.studentattendance.backend.security;

import com.studentattendance.backend.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for login and register. Protects the public
 * auth endpoints from brute-force and spam without adding a new dependency.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final int maxAttempts;
    private final long windowMs;
    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${app.auth.max-attempts:8}") int maxAttempts,
            @Value("${app.auth.window-ms:900000}") long windowMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.endsWith("/api/auth/login")
                || path.endsWith("/api/auth/register")
                || path.endsWith("/api/auth/google"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String key = clientKey(request);
        long now = Instant.now().toEpochMilli();
        Deque<Long> times = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() > windowMs) {
                times.pollFirst();
            }
            if (times.size() >= maxAttempts) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\","
                                + "\"message\":\"Too many login attempts. Please try again later.\"}");
                return;
            }
            times.addLast(now);
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
