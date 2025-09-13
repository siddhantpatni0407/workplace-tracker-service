package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatePeriodDTO {

    @JsonProperty("period")
    private String period; // e.g. "2025-09" (month) or "2025" (year) or "2025-W36" (iso week)

    @JsonProperty("wfo")
    private Long wfo;

    @JsonProperty("wfh")
    private Long wfh;

    @JsonProperty("hybrid")
    private Long hybrid;

    @JsonProperty("others")
    private Long others;

    @JsonProperty("leave")
    private Long leave;

    @JsonProperty("holiday")
    private Long holiday;
}
