package com.sid.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Siddhant Patni
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String role;
    private Long userId;
    private String name;
    private String status;
    private String message;
    private LocalDateTime lastLoginTime;
    private Boolean isActive;
    private Integer loginAttempts;
    private Boolean accountLocked;

}