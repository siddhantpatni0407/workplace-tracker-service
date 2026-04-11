package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(description = "User personal task record")
public class UserTasksDTO {

    @JsonProperty("userTaskId")
    @Schema(description = "System-generated task ID", example = "30",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userTaskId;

    @JsonProperty("userId")
    @Schema(description = "Owner user ID — set from JWT, do not supply in create requests",
            example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @JsonProperty("taskTitle")
    @NotBlank(message = "Task title is required")
    @Size(max = 500, message = "Task title must not exceed 500 characters")
    @Schema(description = "Short title of the task (max 500 chars)",
            example = "Write unit tests for AuthService",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskTitle;

    @JsonProperty("taskDescription")
    @Schema(description = "Detailed description of the task",
            example = "Cover register, login and refresh flows with JUnit 5")
    private String taskDescription;

    @JsonProperty("taskDate")
    @NotNull(message = "Task date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date the task is assigned to (yyyy-MM-dd)", example = "2026-04-11",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate taskDate;

    @JsonProperty("status")
    @Builder.Default
    @Schema(description = "Current task status", example = "NOT_STARTED",
            allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD"})
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @JsonProperty("priority")
    @Builder.Default
    @Schema(description = "Task priority level", example = "MEDIUM",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private TaskPriority priority = TaskPriority.MEDIUM;

    @JsonProperty("category")
    @Builder.Default
    @Schema(description = "Task category", example = "WORK",
            allowableValues = {"WORK", "PERSONAL", "STUDY", "OTHER"})
    private TaskCategory category = TaskCategory.WORK;

    @JsonProperty("taskType")
    @Builder.Default
    @Schema(description = "Task type", example = "TASK",
            allowableValues = {"TASK", "BUG", "STORY", "EPIC", "SUBTASK"})
    private TaskType taskType = TaskType.TASK;

    @JsonProperty("dueDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Optional due date (yyyy-MM-dd)", example = "2026-04-18")
    private LocalDate dueDate;

    @JsonProperty("reminderDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Optional reminder datetime", example = "2026-04-17T09:00:00")
    private LocalDateTime reminderDate;

    @JsonProperty("tags")
    @Schema(description = "Array of tags for categorisation", example = "[\"spring\",\"testing\"]")
    private String[] tags;

    @JsonProperty("parentTaskId")
    @Schema(description = "Parent task ID for sub-task relationships", example = "25")
    private Long parentTaskId;

    @JsonProperty("createdBy")
    @Schema(description = "ID of the user who created this task", example = "42",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long createdBy;

    @JsonProperty("remarks")
    @Schema(description = "Optional remarks or comments", example = "Blocked by missing test fixtures")
    private String remarks;

    @JsonProperty("isRecurring")
    @Builder.Default
    @Schema(description = "Whether this is a recurring task", example = "false")
    private Boolean isRecurring = false;

    @JsonProperty("recurringPattern")
    @Schema(description = "Recurrence pattern (DAILY | WEEKLY | MONTHLY)", example = "WEEKLY",
            allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
    private String recurringPattern;

    @JsonProperty("version")
    @Builder.Default
    @Schema(description = "Optimistic-locking version counter", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer version = 1;

    @JsonProperty("accessCount")
    @Builder.Default
    @Schema(description = "Number of times this task has been accessed", example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer accessCount = 0;

    @JsonProperty("lastAccessedDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp of last access", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastAccessedDate;

    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Record last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedDate;
}
