package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO class for transferring daily task data between the client and server.
 *
 * <p>Author: Siddhant Patni</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyTaskDTO {
    private Long dailyTaskId;

    private Long userId;

    @NotNull(message = "Task date is required")
    private LocalDate dailyTaskDate;

    private String dailyTaskDay;

    private String taskNumber;

    private String projectCode;

    private String projectName;

    private String storyTaskBugNumber;

    private String taskDetails;

    private String remarks;

    // Audit fields
    private String createdDate;
    private String modifiedDate;
}
