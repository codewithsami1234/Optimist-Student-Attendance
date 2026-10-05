package com.studentattendance.backend.controller;

import com.studentattendance.backend.dto.ProfileUpdateRequest;
import com.studentattendance.backend.dto.UserDto;
import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.getProfile(currentUser.getId()));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateProfile(@AuthenticationPrincipal User currentUser,
                                                  @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(currentUser.getId(), request));
    }
}
