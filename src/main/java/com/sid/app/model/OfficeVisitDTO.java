package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OfficeVisitDTO {

    @JsonProperty("officeVisitId")
    private Long officeVisitId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    private Long userId;

    @JsonProperty("visitDate")
    @NotNull(message = "visitDate is required")
    private LocalDate visitDate;

    @JsonProperty("dayOfWeek")
    @NotNull(message = "dayOfWeek is required")
    @Min(value = 1, message = "dayOfWeek must be between 1 and 7")
    @Max(value = 7, message = "dayOfWeek must be between 1 and 7")
    private Integer dayOfWeek;

    @JsonProperty("visitType")
    @NotNull(message = "visitType is required")
    @Size(max = 32, message = "visitType can be at most 32 characters")
    private String visitType; // WFO, WFH, HYBRID, OTHERS

    @JsonProperty("notes")
    private String notes;
}
