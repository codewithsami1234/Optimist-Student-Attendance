package com.example.studentattendance.network.dto;

import com.example.studentattendance.model.User;

/**
 * Response body for POST /api/auth/login. The nested "user" object
 * deserializes directly into the existing {@link User} model since the
 * backends UserDto mirrors its fields exactly (id, name, email, role,
 * designation, avatarInitials).
 */
public class AuthResponseDto {
    private String token;
    private User user;

    public String getToken() { return token; }
    public User getUser() { return user; }
}
