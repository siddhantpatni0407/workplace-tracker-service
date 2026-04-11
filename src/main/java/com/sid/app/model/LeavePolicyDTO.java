package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Tenant-level leave policy definition")
public class LeavePolicyDTO {

    @JsonProperty("policyId")
    @Schema(description = "System-generated policy ID", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long policyId;

    @JsonProperty("policyCode")
    @NotNull(message = "policyCode is required")
    @Size(max = 50, message = "policyCode can be at most 50 characters")
    @Schema(description = "Short unique policy code", example = "PL-ANNUAL",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String policyCode;

    @JsonProperty("policyName")
    @NotNull(message = "policyName is required")
    @Size(max = 100, message = "policyName can be at most 100 characters")
    @Schema(description = "Human-readable policy name", example = "Annual Paid Leave",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String policyName;

    @JsonProperty("defaultAnnualDays")
    @NotNull(message = "defaultAnnualDays is required")
    @Min(value = 0, message = "defaultAnnualDays must be greater than or equal to 0")
    @Schema(description = "Default leave days granted per calendar year", example = "21",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer defaultAnnualDays;

    @JsonProperty("description")
    @Schema(description = "Optional description of the policy",
            example = "Standard annual leave entitlement for full-time employees")
    private String description;
}
