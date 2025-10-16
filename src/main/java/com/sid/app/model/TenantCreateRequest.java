package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for creating a new tenant
 * @author Siddhant Patni
 */
@Data
public class TenantCreateRequest {

    @NotBlank(message = "Tenant name is required")
    @JsonProperty("tenantName")
    private String tenantName;

    @NotNull(message = "Subscription ID is required")
    @JsonProperty("subscriptionId")
    private Long subscriptionId;

    @Email(message = "Invalid contact email format")
    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactPhone")
    private String contactPhone;

    @JsonProperty("subscriptionStartDate")
    private LocalDateTime subscriptionStartDate;

    @JsonProperty("subscriptionEndDate")
    private LocalDateTime subscriptionEndDate;
}
