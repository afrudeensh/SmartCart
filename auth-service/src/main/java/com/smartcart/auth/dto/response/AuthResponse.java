package com.smartcart.auth.dto.response;

import com.smartcart.auth.enums.RoleName;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String fullName;
    private RoleName role;
    private boolean active;
}