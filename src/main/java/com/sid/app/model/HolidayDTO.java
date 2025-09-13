package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class HolidayDTO {

    @JsonProperty("holidayId")
    private Long holidayId;

    @JsonProperty("holidayDate")
    @NotNull(message = "holidayDate is required")
    private LocalDate holidayDate;

    @JsonProperty("name")
    @NotNull(message = "name is required")
    @Size(max = 100, message = "name can be at most 100 characters")
    private String name;

    @JsonProperty("holidayType")
    @NotNull(message = "holidayType is required")
    @Size(max = 16, message = "holidayType can be at most 16 characters")
    private String holidayType; // MANDATORY / OPTIONAL

    @JsonProperty("description")
    private String description;
}
