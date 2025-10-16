package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing tenant
 * @author Siddhant Patni
 */
@Data
public class TenantUpdateRequest {

    @JsonProperty("tenantName")
    private String tenantName;

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
