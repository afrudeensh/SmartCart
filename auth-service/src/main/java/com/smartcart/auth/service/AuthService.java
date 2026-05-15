package com.smartcart.auth.service;


import com.smartcart.auth.dto.request.*;
import com.smartcart.auth.dto.response.*;
import com.smartcart.auth.entity.Role;
import com.smartcart.auth.entity.Users;
import com.smartcart.auth.repository.*;
import com.smartcart.auth.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService   refreshTokenService;

    // ── Register ──────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceInfo) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(Constants.ERR_EMAIL_EXISTS + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException(Constants.ERR_ROLE_NOT_FOUND + request.getRole()));

        Users user = Users.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        return buildAuthResponse(user,
                jwtService.generateAccessToken(user),
                refreshTokenService.createRefreshToken(user, deviceInfo));
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(Constants.ERR_USER_NOT_FOUND));

        return buildAuthResponse(user,
                jwtService.generateAccessToken(user),
                refreshTokenService.createRefreshToken(user, deviceInfo));
    }

    // ── Refresh ───────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse refresh(RefreshRequest request, String deviceInfo) {

        Users user = refreshTokenService.validateAndGetUser(request.getRefreshToken());

        return buildAuthResponse(user,
                jwtService.generateAccessToken(user),
                refreshTokenService.createRefreshToken(user, deviceInfo));
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @Transactional
    public void logout(Users currentUser) {
        refreshTokenService.revokeAll(currentUser);
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    public UserProfileResponse getProfile(Users currentUser) {
        return toProfileResponse(currentUser);
    }

    // ── Admin: all users ──────────────────────────────────────────────────────
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }

    // ── Admin: change role ────────────────────────────────────────────────────
    @Transactional
    public UserProfileResponse changeRole(Long userId, ChangeRoleRequest request) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.ERR_USER_NOT_FOUND + userId));

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException(Constants.ERR_ROLE_NOT_FOUND + request.getRole()));

        user.setRole(role);
        userRepository.save(user);

        return toProfileResponse(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(Users user,
                                           String accessToken,
                                           String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .active(user.isActive())
                .build();
    }

    private UserProfileResponse toProfileResponse(Users user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}