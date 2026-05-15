package com.smartcart.auth.service;

import com.smartcart.auth.entity.EventOutbox;
import com.smartcart.auth.repository.EventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls the event_outbox table every 5 seconds and publishes
 * unpublished events to Kafka topics.
 *
 * Enabled only when kafka is configured:
 *   app.kafka.enabled=true  (set in application.yml)
 *
 * For Week 1 (no Kafka yet) → set app.kafka.enabled=false
 * For Week 3+ (Kafka ready) → set app.kafka.enabled=true
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OutboxPublisherService {

    private final EventOutboxRepository        outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ── Runs every 5 seconds ──────────────────────────────────────────────────
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {

        List<EventOutbox> pending =
                outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();

        if (pending.isEmpty()) return;

        log.info("Outbox: publishing {} event(s)", pending.size());

        for (EventOutbox event : pending) {
            try {
                String topic = resolveTopic(event.getEventType());

                kafkaTemplate
                        .send(topic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Kafka send failed for outbox id={}: {}", event.getId(), ex.getMessage());
                            }
                        });

                event.setPublished(true);
                outboxRepository.save(event);

                log.debug("Published outbox event id={} type={} topic={}",
                        event.getId(), event.getEventType(), topic);

            } catch (Exception e) {
                // Do NOT mark as published — will retry on next poll cycle
                log.error("Failed to publish outbox id={}: {}", event.getId(), e.getMessage());
            }
        }
    }

    // ── Event type → Kafka topic mapping ─────────────────────────────────────
    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "USER_REGISTERED"          -> "auth.user.registered";
            case "USER_LOGGED_IN"           -> "auth.user.login";
            case "USER_LOGGED_OUT"          -> "auth.user.logout";
            case "ROLE_CHANGED"             -> "auth.role.changed";
            case "PASSWORD_RESET_REQUESTED" -> "auth.password.reset-requested";
            case "PASSWORD_RESET_SUCCESS"   -> "auth.password.reset-success";
            default                         -> "auth.events";
        };
    }
}