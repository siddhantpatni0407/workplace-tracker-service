package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for tenant response data
 * @author Siddhant Patni
 */
@Data
public class TenantDTO {

    @JsonProperty("tenantId")
    private Long tenantId;

    @JsonProperty("tenantName")
    private String tenantName;

    @JsonProperty("tenantCode")
    private String tenantCode;

    @JsonProperty("appSubscriptionId")
    private Long appSubscriptionId;

    @JsonProperty("subscriptionCode")
    private String subscriptionCode;

    @JsonProperty("subscriptionName")
    private String subscriptionName;

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactPhone")
    private String contactPhone;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("subscriptionStartDate")
    private LocalDateTime subscriptionStartDate;

    @JsonProperty("subscriptionEndDate")
    private LocalDateTime subscriptionEndDate;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;

    @JsonProperty("totalUsers")
    private Long totalUsers;

    @JsonProperty("activeUsers")
    private Long activeUsers;
}
