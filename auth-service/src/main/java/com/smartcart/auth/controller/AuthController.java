package com.smartcart.auth.controller;

import com.smartcart.auth.dto.request.*;
import com.smartcart.auth.dto.response.AuthResponse;
import com.smartcart.auth.dto.response.BaseResponse;
import com.smartcart.auth.dto.response.UserProfileResponse;
import com.smartcart.auth.entity.Users;
import com.smartcart.auth.service.AuthService;
import com.smartcart.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Register, Login and Role-based access")
public class AuthController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    // ── Public endpoints ──────────────────────────────────────────────────────

    @PostMapping("/register")
    public BaseResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return BaseResponse.success(
                authService.register(request, httpRequest.getHeader("User-Agent")),
                "User registered successfully"
        );
    }

    @PostMapping("/login")
    public BaseResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return BaseResponse.success(
                authService.login(request, httpRequest.getHeader("User-Agent")),
                "Login successful"
        );
    }

    @PostMapping("/refresh")
    public BaseResponse<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {
        return BaseResponse.success(
                authService.refresh(request, httpRequest.getHeader("User-Agent")),
                "Token refreshed successfully"
        );
    }

    // ── Authenticated endpoints ───────────────────────────────────────────────

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<Void> logout(@AuthenticationPrincipal Users currentUser) {
        authService.logout(currentUser);
        return BaseResponse.success(null, "Logged out successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<UserProfileResponse> getProfile(
            @AuthenticationPrincipal Users currentUser) {
        return BaseResponse.success(authService.getProfile(currentUser));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BaseResponse<List<UserProfileResponse>> getAllUsers() {
        return BaseResponse.success(authService.getAllUsers());
    }

    @PostMapping("/admin/users/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BaseResponse<UserProfileResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request) {
        return BaseResponse.success(
                authService.changeRole(id, request),
                "Role updated successfully"
        );
    }

    @PostMapping("/forgot-password")
    public BaseResponse<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetEmail(request.getEmail());
        return BaseResponse.success(null, "If that email is registered, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public BaseResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return BaseResponse.success(null, "Password reset successfully. Please log in.");
    }
}