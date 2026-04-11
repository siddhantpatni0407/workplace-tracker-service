package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for tenant response data
 * @author Siddhant Patni
 */
@Data
@Schema(description = "Tenant / organisation record")
public class TenantDTO {

    @JsonProperty("tenantId")
    @Schema(description = "System-generated tenant ID", example = "5",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long tenantId;

    @JsonProperty("tenantName")
    @Schema(description = "Full organisation name", example = "Acme Corporation")
    private String tenantName;

    @JsonProperty("tenantCode")
    @Schema(description = "Short unique tenant code (auto-generated)", example = "ACME-001",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String tenantCode;

    @JsonProperty("appSubscriptionId")
    @Schema(description = "Linked subscription plan ID", example = "2")
    private Long appSubscriptionId;

    @JsonProperty("subscriptionCode")
    @Schema(description = "Linked subscription code", example = "PLAN-BASIC",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String subscriptionCode;

    @JsonProperty("subscriptionName")
    @Schema(description = "Linked subscription plan name", example = "Basic Plan",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String subscriptionName;

    @JsonProperty("contactEmail")
    @Schema(description = "Primary contact email for the tenant", example = "admin@acme.com")
    private String contactEmail;

    @JsonProperty("contactPhone")
    @Schema(description = "Primary contact phone number", example = "+1-800-555-0100")
    private String contactPhone;

    @JsonProperty("isActive")
    @Schema(description = "Whether this tenant is currently active", example = "true")
    private Boolean isActive;

    @JsonProperty("subscriptionStartDate")
    @Schema(description = "Subscription start date", example = "2026-01-01T00:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime subscriptionStartDate;

    @JsonProperty("subscriptionEndDate")
    @Schema(description = "Subscription expiry date", example = "2027-01-01T00:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime subscriptionEndDate;

    @JsonProperty("createdDate")
    @Schema(description = "Tenant record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @Schema(description = "Tenant record last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedDate;

    @JsonProperty("totalUsers")
    @Schema(description = "Total number of users in this tenant", example = "50",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalUsers;

    @JsonProperty("activeUsers")
    @Schema(description = "Number of currently active users in this tenant", example = "45",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long activeUsers;
}
