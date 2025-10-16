package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for subscription response data
 * @author Siddhant Patni
 */
@Data
public class SubscriptionDTO {

    @JsonProperty("appSubscriptionId")
    private Long appSubscriptionId;

    @JsonProperty("subscriptionCode")
    private String subscriptionCode;

    @JsonProperty("subscriptionName")
    private String subscriptionName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;
}
