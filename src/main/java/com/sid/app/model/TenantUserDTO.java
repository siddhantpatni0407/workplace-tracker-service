package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for TenantUser entities (SUPER_ADMIN, ADMIN)
 * Used for Platform User to manage Super Admins and Super Admin to manage Admins
 *
 * @author Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantUserDTO {

    @JsonProperty("tenantUserId")
    private Long tenantUserId;

    @JsonProperty("tenantId")
    private Long tenantId;

    @JsonProperty("tenantName")
    private String tenantName;

    @JsonProperty("tenantCode")
    private String tenantCode;

    @JsonProperty("platformUserId")
    private Long platformUserId;

    @JsonProperty("platformUserName")
    private String platformUserName;

    @JsonProperty("platformUserCode")
    private String platformUserCode;

    @JsonProperty("roleId")
    private Long roleId;

    @JsonProperty("role")
    private String role;

    @JsonProperty("tenantUserCode")
    private String tenantUserCode;

    @JsonProperty("managerTenantUserId")
    private Long managerTenantUserId;

    @JsonProperty("managerName")
    private String managerName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("loginAttempts")
    private Integer loginAttempts;

    @JsonProperty("accountLocked")
    private Boolean accountLocked;

    @JsonProperty("lastLoginTime")
    private LocalDateTime lastLoginTime;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
