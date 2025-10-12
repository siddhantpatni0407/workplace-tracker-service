package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTasksStatsDTO {

    @JsonProperty("totalTasks")
    private Long totalTasks;

    @JsonProperty("completedTasks")
    private Long completedTasks;

    @JsonProperty("inProgressTasks")
    private Long inProgressTasks;

    @JsonProperty("notStartedTasks")
    private Long notStartedTasks;

    @JsonProperty("onHoldTasks")
    private Long onHoldTasks;

    @JsonProperty("cancelledTasks")
    private Long cancelledTasks;

    @JsonProperty("overdueTasks")
    private Long overdueTasks;

    @JsonProperty("completionRate")
    private BigDecimal completionRate;

    @JsonProperty("averageCompletionTime")
    private BigDecimal averageCompletionTime;

    @JsonProperty("tasksByPriority")
    private Map<String, Long> tasksByPriority;

    @JsonProperty("tasksByCategory")
    private Map<String, Long> tasksByCategory;

    @JsonProperty("tasksByStatus")
    private Map<String, Long> tasksByStatus;

    @JsonProperty("tasksByType")
    private Map<String, Long> tasksByType;

    @JsonProperty("recentlyModified")
    private List<UserTasksDTO> recentlyModified;

    @JsonProperty("upcomingDeadlines")
    private List<UserTasksDTO> upcomingDeadlines;
}
