package com.smartcart.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.auth.entity.UserActivityLog;
import com.smartcart.auth.repository.UserActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final UserActivityLogRepository logRepository;
    private final ObjectMapper              objectMapper;
    /**
     * @Async — fire-and-forget: never blocks the main request thread.
     * Failures are logged but never thrown back to the caller.
     */
    @Async
    public void log(Long userId,
                    String action,
                    String entityType,
                    String entityId,
                    Map<String, Object> metadata,
                    HttpServletRequest request) {
        try {
            String metadataJson = (metadata != null)
                    ? objectMapper.writeValueAsString(metadata)
                    : null;

            UserActivityLog entry = UserActivityLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadataJson)
                    .ipAddress(resolveClientIp(request))
                    .sessionId(resolveSessionId(request))
                    .build();

            logRepository.save(entry);

        } catch (Exception e) {
            // Never fail the main flow because of logging
            log.error("ActivityLog save failed [user={} action={}]: {}", userId, action, e.getMessage());
        }
    }

    // ── Extract real IP (handles reverse proxies / Render / Netlify) ──────────
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();  // first IP = original client
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveSessionId(HttpServletRequest request) {
        var session = request.getSession(false);
        return (session != null) ? session.getId() : null;
    }
}