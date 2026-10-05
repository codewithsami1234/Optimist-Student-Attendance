package com.studentattendance.backend.security;

import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Every authenticated request has its {@link User} entity set as the Spring
 * Security principal by {@link JwtAuthenticationFilter}. This is the single
 * place the rest of the app reads "who is making this request" from, so
 * ownership checks are consistent everywhere instead of each service
 * reaching into {@link SecurityContextHolder} directly.
 */
@Component
public class AuthContext {

    /** The authenticated professor making the current request. */
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new UnauthorizedException("You must be logged in to do that.");
        }
        return (User) authentication.getPrincipal();
    }

    public String currentUserId() {
        return currentUser().getId();
    }
}
