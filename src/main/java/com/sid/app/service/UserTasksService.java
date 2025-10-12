package com.sid.app.service;

import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import com.sid.app.model.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface UserTasksService {

    // Core CRUD operations
    UserTasksDTO createTask(Long userId, UserTasksDTO taskDTO);
    UserTasksDTO getTaskById(Long userId, Long userTaskId);
    UserTasksListResponseDTO getAllUserTasks(Long userId, Pageable pageable);
    UserTasksDTO updateTask(Long userId, Long userTaskId, UserTasksDTO taskDTO);
    void deleteTask(Long userId, Long userTaskId);

    // Filtering and search operations
    UserTasksListResponseDTO getTasksWithFilters(Long userId, TaskStatus status, TaskPriority priority,
                                                 TaskCategory category, TaskType taskType,
                                                 LocalDate startDate, LocalDate endDate,
                                                 LocalDate dueDateStart, LocalDate dueDateEnd,
                                                 Boolean isRecurring, String searchTerm,
                                                 String[] tags, Pageable pageable);

    UserTasksListResponseDTO getTasksByStatus(Long userId, TaskStatus status, Pageable pageable);
    UserTasksListResponseDTO getTasksByPriority(Long userId, TaskPriority priority, Pageable pageable);
    UserTasksListResponseDTO getTasksByCategory(Long userId, TaskCategory category, Pageable pageable);
    UserTasksListResponseDTO getTasksByType(Long userId, TaskType taskType, Pageable pageable);
    UserTasksListResponseDTO searchTasks(Long userId, String searchTerm, Pageable pageable);
    UserTasksListResponseDTO getOverdueTasks(Long userId, Pageable pageable);

    // Status and property update operations
    UserTasksDTO updateTaskStatus(Long userId, Long userTaskId, TaskStatus status);
    UserTasksDTO updateTaskPriority(Long userId, Long userTaskId, TaskPriority priority);

    // Statistics
    UserTasksStatsDTO getTaskStats(Long userId);

    // Bulk operations
    List<UserTasksDTO> bulkUpdateTasks(Long userId, UserTasksBulkUpdateRequest request);
    void bulkDeleteTasks(Long userId, UserTasksBulkDeleteRequest request);

    // Additional features
    UserTasksDTO duplicateTask(Long userId, Long userTaskId);
    List<UserTasksDTO> getSubtasks(Long userId, Long parentTaskId);
}
