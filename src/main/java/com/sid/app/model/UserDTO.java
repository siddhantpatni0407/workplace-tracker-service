package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Tenant user (end-user) data transfer object")
public class UserDTO {

    @JsonProperty("userId")
    @Schema(description = "System-generated user ID", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @JsonProperty("tenantUserId")
    @Schema(description = "Tenant user linkage ID", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
    private Long tenantUserId;

    @JsonProperty("name")
    @Schema(description = "Full display name", example = "John Doe")
    private String name;

    @JsonProperty("email")
    @Schema(description = "Email address", example = "john.doe@acme.com")
    private String email;

    @JsonProperty("mobileNumber")
    @Schema(description = "Mobile phone number", example = "+1234567890")
    private String mobileNumber;

    @JsonProperty("role")
    @Schema(description = "Assigned role", example = "USER",
            allowableValues = {"PLATFORM_USER", "SUPER_ADMIN", "ADMIN", "MANAGER", "USER"})
    private String role;

    @JsonProperty("roleId")
    @Schema(description = "Numeric role ID", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long roleId;

    @JsonProperty("lastLoginTime")
    @Schema(description = "Timestamp of last successful login", example = "2026-04-10T14:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    @JsonProperty("loginAttempts")
    @Schema(description = "Number of consecutive failed login attempts", example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer loginAttempts;

    @JsonProperty("isAccountLocked")
    @Schema(description = "Whether the account is locked", example = "false",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isAccountLocked;

    @JsonProperty("isActive")
    @Schema(description = "Whether the account is active", example = "true")
    private Boolean isActive;

    @JsonProperty("createdDate")
    @Schema(description = "Record creation timestamp", example = "2026-01-01T09:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @Schema(description = "Record last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedDate;

    // Multi-tenant related fields (from relationships)
    @JsonProperty("tenantId")
    @Schema(description = "Tenant the user belongs to", example = "5")
    private Long tenantId;

    @JsonProperty("tenantName")
    @Schema(description = "Name of the tenant organisation", example = "Acme Corporation",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String tenantName;

    @JsonProperty("platformUserId")
    @Schema(description = "Platform-level user ID linked to this tenant user", example = "1001",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long platformUserId;

    @JsonProperty("username")
    @Deprecated
    @Schema(description = "Deprecated — same as name, kept for backward compatibility",
            example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;
}