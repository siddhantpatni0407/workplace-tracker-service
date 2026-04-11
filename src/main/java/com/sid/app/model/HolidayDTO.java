package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for Holiday entity.
 * <p>
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Public holiday record")
public class HolidayDTO {

    @JsonProperty("holidayId")
    @Schema(description = "System-generated holiday ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long holidayId;

    @JsonProperty("holidayDate")
    @NotNull(message = "holidayDate is required")
    @Schema(description = "Date of the holiday (yyyy-MM-dd)", example = "2026-01-26",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate holidayDate;

    @JsonProperty("name")
    @NotNull(message = "name is required")
    @Size(max = 100, message = "name can be at most 100 characters")
    @Schema(description = "Name of the holiday", example = "Republic Day",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonProperty("holidayType")
    @NotNull(message = "holidayType is required")
    @Size(max = 16, message = "holidayType can be at most 16 characters")
    @Schema(description = "Holiday type", example = "MANDATORY",
            allowableValues = {"MANDATORY", "OPTIONAL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String holidayType;

    @JsonProperty("description")
    @Schema(description = "Optional description or notes about the holiday",
            example = "National holiday — offices closed")
    private String description;
}
