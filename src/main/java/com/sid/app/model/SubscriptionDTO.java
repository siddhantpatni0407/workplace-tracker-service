package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for subscription response data
 * @author Siddhant Patni
 */
@Data
@Schema(description = "Application subscription plan")
public class SubscriptionDTO {

    @JsonProperty("appSubscriptionId")
    @Schema(description = "System-generated subscription ID", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long appSubscriptionId;

    @JsonProperty("subscriptionCode")
    @Schema(description = "Short unique subscription code", example = "PLAN-BASIC")
    private String subscriptionCode;

    @JsonProperty("subscriptionName")
    @Schema(description = "Human-readable plan name", example = "Basic Plan")
    private String subscriptionName;

    @JsonProperty("description")
    @Schema(description = "Plan description", example = "Suitable for small teams up to 10 users")
    private String description;

    @JsonProperty("isActive")
    @Schema(description = "Whether this subscription plan is currently active", example = "true")
    private Boolean isActive;

    @JsonProperty("createdDate")
    @Schema(description = "Plan creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @Schema(description = "Plan last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedDate;
}
