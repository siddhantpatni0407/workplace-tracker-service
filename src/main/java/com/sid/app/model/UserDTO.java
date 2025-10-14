package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("tenantUserId")
    private Long tenantUserId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("role")
    private String role;

    @JsonProperty("roleId")
    private Long roleId;

    @JsonProperty("lastLoginTime")
    private LocalDateTime lastLoginTime;

    @JsonProperty("loginAttempts")
    private Integer loginAttempts;

    @JsonProperty("isAccountLocked")
    private Boolean isAccountLocked;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;

    // Multi-tenant related fields (from relationships)
    @JsonProperty("tenantId")
    private Long tenantId;

    @JsonProperty("tenantName")
    private String tenantName;

    @JsonProperty("platformUserId")
    private Long platformUserId;

    // Deprecated field - keeping for backward compatibility
    @JsonProperty("username")
    @Deprecated
    private String username;
}