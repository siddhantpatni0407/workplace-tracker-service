package com.sid.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformUserAuthResponse {

    private String token;
    private String refreshToken;
    private String role;
    private Long platformUserId;
    private String name;
    private String status;
    private String message;
    private LocalDateTime lastLoginTime;
    private Boolean isActive;
    private Integer loginAttempts;
    private Boolean accountLocked;
}
