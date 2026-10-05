package com.studentattendance.backend.service;

import com.studentattendance.backend.domain.AttendancePolicy;
import com.studentattendance.backend.dto.AuthResponse;
import com.studentattendance.backend.dto.LoginRequest;
import com.studentattendance.backend.dto.ProfileUpdateRequest;
import com.studentattendance.backend.dto.RegisterRequest;
import com.studentattendance.backend.service.GoogleTokenService.GoogleProfile;
import com.studentattendance.backend.dto.UserDto;
import com.studentattendance.backend.entity.User;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.exception.ResourceNotFoundException;
import com.studentattendance.backend.exception.UnauthorizedException;
import com.studentattendance.backend.repository.UserRepository;
import com.studentattendance.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Login, registration, and profile business logic. Mirrors the validation messages of MockAuthRepository exactly. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleTokenService googleTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.googleTokenService = googleTokenService;
    }

    /**
     * Every professor gets their own private account and, from here on,
     * their own private set of departments/classes - see AuthContext and
     * the ownerId columns added throughout the catalog entities.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("An account with this email already exists.");
        }
        User user = new User(
                "user_" + UUID.randomUUID(),
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                "TEACHER",
                request.getDesignation() != null ? request.getDesignation().trim() : "Professor");
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, new UserDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new UnauthorizedException("Please enter both email and password.");
        }
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            if (user.getGoogleSubject() != null && !user.getGoogleSubject().isBlank()) {
                throw new UnauthorizedException("This account uses Google Sign-In. Tap Continue with Google.");
            }
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, new UserDto(user));
    }

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleProfile profile = googleTokenService.verify(idToken);
        User user = userRepository.findByGoogleSubject(profile.subject())
                .or(() -> userRepository.findByEmailIgnoreCase(profile.email()))
                .orElseGet(() -> createGoogleUser(profile));
        if (user.getGoogleSubject() == null || user.getGoogleSubject().isBlank()) {
            user.setGoogleSubject(profile.subject());
            userRepository.save(user);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, new UserDto(user));
    }

    private User createGoogleUser(GoogleProfile profile) {
        User user = new User(
                "user_" + UUID.randomUUID(),
                profile.name().trim(),
                profile.email(),
                passwordEncoder.encode(UUID.randomUUID().toString()),
                "TEACHER",
                "Professor");
        user.setGoogleSubject(profile.subject());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDto getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return new UserDto(user);
    }

    @Transactional
    public UserDto updateProfile(String userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Name cannot be empty.");
        }
        user.setName(request.getName().trim());
        if (request.getDesignation() != null) {
            user.setDesignation(request.getDesignation());
        }
        if (request.getLowAttendanceThreshold() != null) {
            user.setLowAttendanceThreshold(
                    AttendancePolicy.sanitizeThreshold(request.getLowAttendanceThreshold()));
        }
        userRepository.save(user);
        return new UserDto(user);
    }
}
