package com.studentattendance.backend.security;

import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Reads the "Authorization: Bearer <token>" header on every request, and if
 * it is a valid JWT for a known user, populates the Spring Security context
 * so @AuthenticationPrincipal / SecurityContextHolder works in controllers.
 * Requests without a token simply proceed unauthenticated; SecurityConfig
 * decides which endpoints require authentication.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenBlacklist.isRevoked(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            String userId = jwtUtil.validateAndGetUserId(token);
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> user = userRepository.findById(userId);
                user.ifPresent(u -> {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            u, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
