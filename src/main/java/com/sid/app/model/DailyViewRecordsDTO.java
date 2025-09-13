package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sid.app.enums.DayLabel;
import lombok.*;

import java.time.LocalDate;

/**
 * Snapshot of a single date with combined info (holiday / leave / visit).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyViewRecordsDTO {

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("dayOfWeek")
    private Integer dayOfWeek; // 1=Mon .. 7=Sun

    /**
     * Primary label for quick UI rendering. Values: NONE | HOLIDAY | LEAVE | VISIT
     * Precedence: HOLIDAY > LEAVE > VISIT
     */
    @JsonProperty("label")
    private DayLabel label;

    // Holiday fields
    @JsonProperty("holidayName")
    private String holidayName;

    @JsonProperty("holidayType")
    private String holidayType; // MANDATORY / OPTIONAL

    // Leave fields
    @JsonProperty("leavePolicyCode")
    private String leavePolicyCode;

    @JsonProperty("leaveDays")
    private String leaveDays;

    @JsonProperty("leaveDayPart")
    private String leaveDayPart;

    @JsonProperty("leaveNotes")
    private String leaveNotes;

    // Visit fields
    @JsonProperty("visitType")
    private String visitType; // WFO / WFH / HYBRID / OTHERS

    @JsonProperty("visitNotes")
    private String visitNotes;
}
