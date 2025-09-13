package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for LeavePolicy entity.
 * <p>
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicyDTO {

    @JsonProperty("policyId")
    private Long policyId;

    @JsonProperty("policyCode")
    @NotNull(message = "policyCode is required")
    @Size(max = 50, message = "policyCode can be at most 50 characters")
    private String policyCode;

    @JsonProperty("policyName")
    @NotNull(message = "policyName is required")
    @Size(max = 100, message = "policyName can be at most 100 characters")
    private String policyName;

    @JsonProperty("defaultAnnualDays")
    @NotNull(message = "defaultAnnualDays is required")
    @Min(value = 0, message = "defaultAnnualDays must be greater than or equal to 0")
    private Integer defaultAnnualDays;

    @JsonProperty("description")
    private String description;
}
