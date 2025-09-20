package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for current month special days (dashboard endpoint)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentMonthSpecialDaysDTO {

    @JsonProperty("birthdays")
    private List<SpecialDayDTO> birthdays;

    @JsonProperty("anniversaries")
    private List<SpecialDayDTO> anniversaries;

    @JsonProperty("counts")
    private CountsDTO counts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CountsDTO {

        @JsonProperty("birthdays")
        private Long birthdays;

        @JsonProperty("anniversaries")
        private Long anniversaries;

        @JsonProperty("total")
        private Long total;
    }
}
