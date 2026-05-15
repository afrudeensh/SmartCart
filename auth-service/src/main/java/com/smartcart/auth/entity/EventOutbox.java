package com.smartcart.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;        // e.g. userId
    private String eventType;          // e.g. USER_REGISTERED

    @Column(columnDefinition = "TEXT")
    private String payload;            // JSON string

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}