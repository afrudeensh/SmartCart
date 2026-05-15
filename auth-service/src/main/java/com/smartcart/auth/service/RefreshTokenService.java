package com.smartcart.auth.service;

import com.smartcart.auth.entity.RefreshToken;
import com.smartcart.auth.entity.Users;
import com.smartcart.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;   // 604800000ms = 7 days

    // ── Create and save a new refresh token ───────────────────────────────────
    @Transactional
    public String createRefreshToken(Users user, String deviceInfo) {
        String rawToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000))
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(token);
        return rawToken;    // return raw — only hash is stored in DB
    }

    // ── Validate refresh token and return the associated user ─────────────────
    @Transactional
    public Users validateAndGetUser(String rawToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        if (token.isExpired()) {
            throw new RuntimeException("Refresh token has expired");
        }

        // rotate — revoke old, caller will create new
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return token.getUser();
    }

    // ── Revoke all tokens for user (logout) ───────────────────────────────────
    @Transactional
    public void revokeAll(Users user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    // ── SHA-256 hash ──────────────────────────────────────────────────────────
    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}