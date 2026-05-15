package com.smartcart.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log",
        indexes = {
                @Index(name = "idx_activity_user_id",  columnList = "userId"),
                @Index(name = "idx_activity_created",  columnList = "createdAt")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who did it
    private Long   userId;

    // What did they do  (LOGIN, LOGOUT, REGISTER, PASSWORD_RESET_REQUEST,
    //                    PASSWORD_RESET_SUCCESS, ROLE_CHANGED, TOKEN_REFRESHED)
    @Column(nullable = false, length = 100)
    private String action;

    // On which entity (always "User" in auth-service)
    @Column(length = 50)
    private String entityType;

    // Which entity's ID was affected
    @Column(length = 100)
    private String entityId;

    // Extra context as JSON  {"device":"Chrome/MacOS", "role":"ROLE_ADMIN", ...}
    @Column(columnDefinition = "TEXT")
    private String metadata;

    private String sessionId;

    @Column(length = 45)  // supports IPv6
    private String ipAddress;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}