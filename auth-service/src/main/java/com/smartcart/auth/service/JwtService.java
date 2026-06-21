package com.smartcart.auth.service;

import com.smartcart.auth.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")   // 900000 = 15 min
    private long accessTokenExpiration;

    // ── Generate ACCESS token (15 min) ────────────────────────────────────────
    public String generateAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role",  user.getRole().getName().name());
        claims.put("permissions", resolvePermissions(user));   // from doc Section 5.3
        return buildToken(claims, String.valueOf(user.getId()), accessTokenExpiration);
    }

    // ── Generate raw REFRESH token (stored hashed in DB) ─────────────────────
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();   // random UUID — stored hashed
    }

    // ── Extract userId (sub) ──────────────────────────────────────────────────
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ── Internals ─────────────────────────────────────────────────────────────
    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private List<String> resolvePermissions(Users user) {
        return switch (user.getRole().getName()) {
            case ROLE_SUPER_ADMIN -> List.of("READ_ALL", "WRITE_ALL", "ASSIGN_ROLE");
            case ROLE_ADMIN       -> List.of("READ_ALL", "WRITE_PRODUCT", "WRITE_ORDER", "MANAGE_USER");
            case ROLE_SELLER      -> List.of("READ_PRODUCT", "WRITE_OWN_PRODUCT", "READ_OWN_ORDER");
            case ROLE_USER        -> List.of("READ_PRODUCT", "WRITE_ORDER", "WRITE_REVIEW");
            default               -> List.of("READ_PRODUCT");
        };
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser()
                .verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}