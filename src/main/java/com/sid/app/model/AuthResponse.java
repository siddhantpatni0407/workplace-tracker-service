package com.sid.app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Siddhant Patni
 */
@Data
@AllArgsConstructor
@Schema(description = "Authentication response returned on login or register")
public class AuthResponse {

    @Schema(description = "JWT Bearer access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String token;

    @Schema(description = "Assigned user role", example = "USER",
            allowableValues = {"PLATFORM_USER", "SUPER_ADMIN", "ADMIN", "MANAGER", "USER"})
    private String role;

    @Schema(description = "Unique user identifier", example = "42")
    private Long userId;

    @Schema(description = "Display name of the authenticated user", example = "John Doe")
    private String name;

    @Schema(description = "Operation result: SUCCESS or FAILED", example = "SUCCESS",
            allowableValues = {"SUCCESS", "FAILED"})
    private String status;

    @Schema(description = "Human-readable result message", example = "Login successful")
    private String message;

    @Schema(description = "Timestamp of the previous successful login", example = "2026-04-10T14:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    @Schema(description = "Whether the account is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "Number of consecutive failed login attempts", example = "0")
    private Integer loginAttempts;

    @Schema(description = "Whether the account is locked due to too many failed attempts", example = "false")
    private Boolean accountLocked;

}