package com.smartcart.auth.dto.response;

import com.smartcart.auth.enums.RoleName;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private RoleName role;
    private boolean active;
    private LocalDateTime createdAt;
}