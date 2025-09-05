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

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("role")
    private String role;

    @JsonProperty("lastLoginTime")
    private LocalDateTime lastLoginTime;

    @JsonProperty("loginAttempts")
    private Integer loginAttempts;

    @JsonProperty("isAccountLocked")
    private Boolean isAccountLocked;

    @JsonProperty("isActive")
    private Boolean isActive;

}