package com.smartcart.auth.service;

import com.smartcart.auth.entity.PasswordResetToken;
import com.smartcart.auth.entity.Users;
import com.smartcart.auth.repository.PasswordResetTokenRepository;
import com.smartcart.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder              passwordEncoder;
    @Autowired(required = false)          // ← add this instead of final
    private JavaMailSender mailSender;

    @Value("${app.password-reset.expiry-minutes:15}")
    private long expiryMinutes;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Step 1: User requests a reset ─────────────────────────────────────────
    @Transactional
    public void sendResetEmail(String email) {

        // Security: silently return if email not found — don't reveal existence
        Users user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        // Invalidate any existing unused tokens for this user
        tokenRepository.invalidateAllByUserId(user.getId());

        // Generate raw token → hash → store hash in DB
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        tokenRepository.save(token);

        sendEmail(user.getEmail(), user.getFullName(), rawToken);
        log.info("Password reset email sent to: {}", email);
    }

    // ── Step 2: User submits new password ─────────────────────────────────────
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        PasswordResetToken token = tokenRepository
                .findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (token.isUsed())    throw new RuntimeException("Reset token has already been used");
        if (token.isExpired()) throw new RuntimeException("Reset token has expired");

        Users user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used — one-time only
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Password reset successful for userId: {}", user.getId());
    }

    // ── Email builder ─────────────────────────────────────────────────────────
    private void sendEmail(String toEmail, String fullName, String rawToken) {
        if (mailSender == null) {
            log.warn("Mail not configured. Reset link for {}: {}/reset-password?token={}",
                    toEmail, frontendUrl, rawToken);  // console la link print aagum
            return;
        }
        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("SmartCart — Reset Your Password");
        message.setText(
                "Hi " + fullName + ",\n\n"
                        + "We received a request to reset your SmartCart password.\n\n"
                        + "Click the link below (valid for " + expiryMinutes + " minutes):\n\n"
                        + resetLink + "\n\n"
                        + "If you did not request this, you can safely ignore this email.\n\n"
                        + "— SmartCart Team"
        );

        mailSender.send(message);
    }

    // ── SHA-256 hash (same pattern as RefreshTokenService) ────────────────────
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