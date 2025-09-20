package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for Special Days data with pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecialDaysDataDTO {

    @JsonProperty("records")
    private List<SpecialDayDTO> records;

    @JsonProperty("pagination")
    private PaginationDTO pagination;
}
