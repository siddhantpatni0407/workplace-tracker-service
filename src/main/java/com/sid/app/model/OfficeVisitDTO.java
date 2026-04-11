package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for OfficeVisit entity.
 * <p>
 * Author: Siddhant Patni
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Office visit / attendance check-in record")
public class OfficeVisitDTO {

    @JsonProperty("officeVisitId")
    @Schema(description = "System-generated visit ID", example = "100",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long officeVisitId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    @Schema(description = "ID of the user who visited", example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @JsonProperty("visitDate")
    @NotNull(message = "visitDate is required")
    @Schema(description = "Date of the visit (yyyy-MM-dd)", example = "2026-04-11",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate visitDate;

    @JsonProperty("dayOfWeek")
    @NotNull(message = "dayOfWeek is required")
    @Min(value = 1, message = "dayOfWeek must be between 1 and 7")
    @Max(value = 7, message = "dayOfWeek must be between 1 and 7")
    @Schema(description = "Day of week — 1=Monday ... 7=Sunday", example = "5",
            minimum = "1", maximum = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dayOfWeek;

    @JsonProperty("visitType")
    @NotNull(message = "visitType is required")
    @Size(max = 32, message = "visitType can be at most 32 characters")
    @Schema(description = "Mode of work for this day", example = "WFO",
            allowableValues = {"WFO", "WFH", "HYBRID", "OTHERS"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String visitType;

    @JsonProperty("notes")
    @Schema(description = "Optional notes about this visit", example = "Attended team standup in-person")
    private String notes;
}
