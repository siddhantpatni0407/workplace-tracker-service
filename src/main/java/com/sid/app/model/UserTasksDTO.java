package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
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
public class UserTasksDTO {

    @JsonProperty("userTaskId")
    private Long userTaskId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("taskTitle")
    @NotBlank(message = "Task title is required")
    @Size(max = 500, message = "Task title must not exceed 500 characters")
    private String taskTitle;

    @JsonProperty("taskDescription")
    private String taskDescription;

    @JsonProperty("taskDate")
    @NotNull(message = "Task date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskDate;

    @JsonProperty("status")
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @JsonProperty("priority")
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @JsonProperty("category")
    @Builder.Default
    private TaskCategory category = TaskCategory.WORK;

    @JsonProperty("taskType")
    @Builder.Default
    private TaskType taskType = TaskType.TASK;

    @JsonProperty("dueDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonProperty("reminderDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderDate;

    @JsonProperty("tags")
    private String[] tags;

    @JsonProperty("parentTaskId")
    private Long parentTaskId;

    @JsonProperty("createdBy")
    private Long createdBy;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("isRecurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @JsonProperty("recurringPattern")
    private String recurringPattern;

    @JsonProperty("version")
    @Builder.Default
    private Integer version = 1;

    @JsonProperty("accessCount")
    @Builder.Default
    private Integer accessCount = 0;

    @JsonProperty("lastAccessedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedDate;

    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
}
