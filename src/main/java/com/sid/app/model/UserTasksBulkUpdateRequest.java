package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTasksBulkUpdateRequest {

    @JsonProperty("userTaskIds")
    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<Long> userTaskIds;

    @JsonProperty("status")
    private TaskStatus status;

    @JsonProperty("priority")
    private TaskPriority priority;

    @JsonProperty("category")
    private TaskCategory category;

    @JsonProperty("taskType")
    private TaskType taskType;

    @JsonProperty("remarks")
    private String remarks;
}
