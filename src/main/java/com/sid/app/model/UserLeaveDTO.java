package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for UserLeave.
 * <p>
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLeaveDTO {

    @JsonProperty("userLeaveId")
    private Long userLeaveId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    private Long userId;

    @JsonProperty("policyId")
    @NotNull(message = "policyId is required")
    private Long policyId;

    @JsonProperty("startDate")
    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @JsonProperty("endDate")
    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    /**
     * Number of days taken for this leave record. Supports fractional values (0.5, 1.0, ...).
     */
    @JsonProperty("days")
    @NotNull(message = "days is required")
    @DecimalMin(value = "0.5", message = "days must be at least 0.5")
    private BigDecimal days;

    @JsonProperty("dayPart")
    @Size(max = 16, message = "dayPart can be at most 16 characters")
    private String dayPart; // FULL, MORNING, AFTERNOON, CUSTOM

    @JsonProperty("notes")
    private String notes;
}
