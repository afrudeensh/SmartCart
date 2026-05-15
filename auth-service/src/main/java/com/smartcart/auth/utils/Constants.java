package com.smartcart.auth.utils;

public class Constants {

    private Constants() {}   // prevent instantiation

    // ── API Paths ─────────────────────────────────────────────────────────────
    public static final String AUTH_BASE        = "/api/auth";
    public static final String ADMIN_BASE       = "/api/auth/admin";

    // ── JWT ───────────────────────────────────────────────────────────────────
    public static final String BEARER_PREFIX    = "Bearer ";
    public static final String AUTH_HEADER      = "Authorization";
    public static final String CLAIM_EMAIL      = "email";
    public static final String CLAIM_ROLE       = "role";
    public static final String CLAIM_PERMISSIONS= "permissions";

    // ── User Status ───────────────────────────────────────────────────────────
    public static final boolean STATUS_ACTIVE   = true;
    public static final boolean STATUS_INACTIVE = false;

    // ── Error Messages ────────────────────────────────────────────────────────
    public static final String ERR_EMAIL_EXISTS   = "Email already registered: ";
    public static final String ERR_USER_NOT_FOUND = "User not found";
    public static final String ERR_ROLE_NOT_FOUND = "Role not found: ";
    public static final String ERR_INVALID_TOKEN  = "Invalid refresh token";
    public static final String ERR_TOKEN_REVOKED  = "Refresh token has been revoked";
    public static final String ERR_TOKEN_EXPIRED  = "Refresh token has expired";
    public static final String ERR_INVALID_CREDS  = "Invalid email or password";
    public static final String ERR_ACCESS_DENIED  = "Access denied: insufficient permissions";

    // ── Success Messages ──────────────────────────────────────────────────────
    public static final String MSG_REGISTER_SUCCESS = "User registered successfully";
    public static final String MSG_LOGIN_SUCCESS    = "Login successful";
    public static final String MSG_REFRESH_SUCCESS  = "Token refreshed successfully";
    public static final String MSG_LOGOUT_SUCCESS   = "Logged out successfully";
    public static final String MSG_ROLE_UPDATED     = "Role updated successfully";
}