package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Daily task log entry — records work done by a user on a specific date")
public class DailyTaskDTO {

    @Schema(description = "System-generated task ID", example = "200",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long dailyTaskId;

    @Schema(description = "ID of the user who logged the task", example = "42",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @NotNull(message = "Task date is required")
    @Schema(description = "Date for which this task is logged (yyyy-MM-dd)", example = "2026-04-11",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dailyTaskDate;

    @Schema(description = "Day name (auto-derived from dailyTaskDate)", example = "Friday",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String dailyTaskDay;

    @Schema(description = "Sequential task number within the day", example = "1")
    private String taskNumber;

    @Schema(description = "Project or client code", example = "PROJ-001")
    private String projectCode;

    @Schema(description = "Full project or client name", example = "Workplace Tracker")
    private String projectName;

    @Schema(description = "Story / Task / Bug ticket identifier", example = "WTS-4321")
    private String storyTaskBugNumber;

    @Schema(description = "Detailed description of the work done",
            example = "Implemented JWT refresh token rotation with cookie support")
    private String taskDetails;

    @Schema(description = "Optional remarks or status note", example = "Ready for code review")
    private String remarks;

    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdDate;

    @Schema(description = "Record last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private String modifiedDate;
}
