package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserLeaveBalanceDTO {

    @JsonProperty("userLeaveBalanceId")
    private Long userLeaveBalanceId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    private Long userId;

    @JsonProperty("policyId")
    @NotNull(message = "policyId is required")
    private Long policyId;

    @JsonProperty("year")
    @NotNull(message = "year is required")
    @Min(value = 1900, message = "year must be a valid year")
    private Integer year;

    @JsonProperty("allocatedDays")
    @NotNull(message = "allocatedDays is required")
    private BigDecimal allocatedDays;

    @JsonProperty("usedDays")
    @NotNull(message = "usedDays is required")
    private BigDecimal usedDays;

    @JsonProperty("remainingDays")
    @NotNull(message = "remainingDays is required")
    private BigDecimal remainingDays;
}
