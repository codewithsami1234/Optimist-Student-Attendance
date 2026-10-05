package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.AuthResponse;
import com.studentattendance.backend.dto.GoogleAuthRequest;
import com.studentattendance.backend.dto.LoginRequest;
import com.studentattendance.backend.dto.RegisterRequest;
import com.studentattendance.backend.security.JwtUtil;
import com.studentattendance.backend.security.TokenBlacklist;
import com.studentattendance.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklist tokenBlacklist;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, TokenBlacklist tokenBlacklist, JwtUtil jwtUtil) {
        this.authService = authService;
        this.tokenBlacklist = tokenBlacklist;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request.getIdToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            tokenBlacklist.revoke(token, jwtUtil.getExpirationMillis(token));
        }
        return ResponseEntity.noContent().build();
    }
}
