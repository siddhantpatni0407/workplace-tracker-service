package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User leave application record")
public class UserLeaveDTO {

    @JsonProperty("userLeaveId")
    @Schema(description = "System-generated leave ID", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userLeaveId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    @Schema(description = "ID of the user applying for leave", example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @JsonProperty("policyId")
    @NotNull(message = "policyId is required")
    @Schema(description = "ID of the applicable leave policy", example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long policyId;

    @JsonProperty("startDate")
    @NotNull(message = "startDate is required")
    @Schema(description = "Leave start date — inclusive (yyyy-MM-dd)", example = "2026-04-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @JsonProperty("endDate")
    @NotNull(message = "endDate is required")
    @Schema(description = "Leave end date — inclusive (yyyy-MM-dd)", example = "2026-04-17",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    /**
     * Number of days taken for this leave record. Supports fractional values (0.5, 1.0, ...).
     */
    @JsonProperty("days")
    @NotNull(message = "days is required")
    @DecimalMin(value = "0.5", message = "days must be at least 0.5")
    @Schema(description = "Number of leave days (supports half-days: 0.5, 1.0 ...)", example = "3.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal days;

    @JsonProperty("dayPart")
    @Size(max = 16, message = "dayPart can be at most 16 characters")
    @Schema(description = "Which part of the day", example = "FULL",
            allowableValues = {"FULL", "MORNING", "AFTERNOON", "CUSTOM"})
    private String dayPart; // FULL, MORNING, AFTERNOON, CUSTOM

    @JsonProperty("notes")
    @Schema(description = "Optional reason or notes for the leave", example = "Medical appointment")
    private String notes;
}
