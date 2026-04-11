package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for UserLeaveBalance (reporting cache / upserted by APIs).
 * <p>
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User leave balance for a specific policy and calendar year")
public class UserLeaveBalanceDTO {

    @JsonProperty("userLeaveBalanceId")
    @Schema(description = "System-generated balance record ID", example = "5",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userLeaveBalanceId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    @Schema(description = "ID of the user", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @JsonProperty("policyId")
    @NotNull(message = "policyId is required")
    @Schema(description = "ID of the leave policy", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long policyId;

    @JsonProperty("year")
    @NotNull(message = "year is required")
    @Min(value = 1900, message = "year must be a valid year")
    @Schema(description = "Calendar year this balance applies to", example = "2026",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;

    @JsonProperty("allocatedDays")
    @NotNull(message = "allocatedDays is required")
    @Schema(description = "Total leave days allocated for this policy/year", example = "21.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal allocatedDays;

    @JsonProperty("usedDays")
    @NotNull(message = "usedDays is required")
    @Schema(description = "Leave days already consumed", example = "5.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal usedDays;

    @JsonProperty("remainingDays")
    @NotNull(message = "remainingDays is required")
    @Schema(description = "Remaining leave days (allocatedDays − usedDays)", example = "16.0",
            accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal remainingDays;
}
