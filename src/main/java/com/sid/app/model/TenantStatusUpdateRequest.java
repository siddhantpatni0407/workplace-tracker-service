package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating tenant status (activate/deactivate)
 * @author Siddhant Patni
 */
@Data
public class TenantStatusUpdateRequest {

    @NotNull(message = "Tenant ID is required")
    @JsonProperty("tenantId")
    private Long tenantId;

    @NotNull(message = "Status is required")
    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("reason")
    private String reason; // Optional reason for status change
}
