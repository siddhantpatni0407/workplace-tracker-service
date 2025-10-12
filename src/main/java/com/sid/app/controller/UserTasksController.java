package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.enums.*;
import com.sid.app.model.*;
import com.sid.app.service.UserTasksService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling UserTasks-related operations.
 * Provides comprehensive CRUD operations, filtering, search, statistics, and bulk operations for user tasks.
 * Uses query parameters for resource identification instead of path variables as per API specification.
 *
 * <p>Author: Siddhant Patni</p>
 */
@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
public class UserTasksController {

    private final UserTasksService userTasksService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    // Core CRUD Operations

    /**
     * Create a new task for the authenticated user.
     *
     * @param taskDTO The task data to create
     * @return ResponseEntity with the created task
     */
    @PostMapping(AppConstants.TASKS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> createTask(@RequestBody @Valid UserTasksDTO taskDTO) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("createTask() : Creating task for user: {}", userId);

        try {
            UserTasksDTO createdTask = userTasksService.createTask(userId, taskDTO);
            log.info("createTask() : Task created successfully with ID: {}", createdTask.getUserTaskId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task created successfully.", createdTask));
        } catch (Exception e) {
            log.error("createTask() : Error creating task for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to create task: " + e.getMessage(), null));
        }
    }

    /**
     * Get a specific task by ID for the authenticated user.
     * Endpoint: GET /tasks/details?userTaskId=1
     *
     * @param userTaskId The ID of the task to retrieve
     * @return ResponseEntity with the task data
     */
    @GetMapping(AppConstants.TASKS_DETAILS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> getTaskById(@RequestParam Long userTaskId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getTaskById() : Fetching task {} for user {}", userTaskId, userId);

        try {
            UserTasksDTO task = userTasksService.getTaskById(userId, userTaskId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task retrieved successfully.", task));
        } catch (EntityNotFoundException e) {
            log.warn("getTaskById() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("getTaskById() : Error fetching task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve task: " + e.getMessage(), null));
        }
    }

    /**
     * Get all tasks for the authenticated user with pagination and optional filters.
     * Endpoint: GET /tasks/user?status=IN_PROGRESS&priority=HIGH&page=0&limit=10
     *
     * @param page         Page number (default: 0)
     * @param limit        Page size (default: 50)
     * @param status       Filter by task status
     * @param priority     Filter by priority
     * @param category     Filter by category
     * @param taskType     Filter by task type
     * @param startDate    Filter tasks created after this date
     * @param endDate      Filter tasks created before this date
     * @param dueDateStart Filter tasks with due date after this date
     * @param dueDateEnd   Filter tasks with due date before this date
     * @param isOverdue    Filter overdue tasks
     * @param searchTerm   Search in title, description, and tags
     * @param tags         Filter by tags (comma-separated)
     * @param sortBy       Sort field
     * @param sortOrder    Sort direction
     * @return ResponseEntity with the list of tasks
     */
    @GetMapping(AppConstants.TASKS_USER_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksListResponseDTO>> getUserTasks(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "50") int limit,
                                                                              @RequestParam(required = false) TaskStatus status,
                                                                              @RequestParam(required = false) TaskPriority priority,
                                                                              @RequestParam(required = false) TaskCategory category,
                                                                              @RequestParam(required = false) TaskType taskType,
                                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDateStart,
                                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDateEnd,
                                                                              @RequestParam(required = false) Boolean isOverdue,
                                                                              @RequestParam(required = false) String searchTerm,
                                                                              @RequestParam(required = false) String tags,
                                                                              @RequestParam(defaultValue = "createdDate") String sortBy,
                                                                              @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getUserTasks() : Fetching tasks for user {} with filters", userId);

        try {
            // Create pageable object
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(limit, 100), Sort.by(direction, sortBy));

            // Parse tags if provided
            String[] tagArray = tags != null ? tags.split(",") : null;

            UserTasksListResponseDTO result;
            if (hasFilters(status, priority, category, taskType, startDate, endDate, dueDateStart, dueDateEnd, searchTerm, tagArray)) {
                result = userTasksService.getTasksWithFilters(userId, status, priority, category, taskType,
                        startDate, endDate, dueDateStart, dueDateEnd, null, searchTerm, tagArray, pageable);
            } else {
                result = userTasksService.getAllUserTasks(userId, pageable);
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Tasks retrieved successfully.", result));
        } catch (Exception e) {
            log.error("getUserTasks() : Error fetching tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve tasks: " + e.getMessage(), null));
        }
    }

    /**
     * Update a specific task for the authenticated user.
     * Endpoint: PUT /tasks/update?userTaskId=1
     *
     * @param userTaskId The ID of the task to update
     * @param taskDTO    The updated task data
     * @return ResponseEntity with the updated task
     */
    @PutMapping(AppConstants.TASKS_UPDATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> updateTask(
            @RequestParam Long userTaskId,
            @RequestBody @Valid UserTasksDTO taskDTO) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateTask() : Updating task {} for user {}", userTaskId, userId);

        try {
            UserTasksDTO updatedTask = userTasksService.updateTask(userId, userTaskId, taskDTO);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task updated successfully.", updatedTask));
        } catch (EntityNotFoundException e) {
            log.warn("updateTask() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("updateTask() : Error updating task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update task: " + e.getMessage(), null));
        }
    }

    /**
     * Delete a specific task for the authenticated user.
     * Endpoint: DELETE /tasks/delete?userTaskId=1
     *
     * @param userTaskId The ID of the task to delete
     * @return ResponseEntity with success message
     */
    @DeleteMapping(AppConstants.TASKS_DELETE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<Void>> deleteTask(@RequestParam Long userTaskId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("deleteTask() : Deleting task {} for user {}", userTaskId, userId);

        try {
            userTasksService.deleteTask(userId, userTaskId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task deleted successfully.", null));
        } catch (EntityNotFoundException e) {
            log.warn("deleteTask() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("deleteTask() : Error deleting task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to delete task: " + e.getMessage(), null));
        }
    }

    // Status and Priority Update Operations

    /**
     * Update task status for the authenticated user.
     * Endpoint: PATCH /tasks/status/update?userTaskId=1
     *
     * @param userTaskId    The ID of the task to update
     * @param statusRequest The new status data
     * @return ResponseEntity with updated task status
     */
    @PatchMapping(AppConstants.TASKS_STATUS_UPDATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> updateTaskStatus(
            @RequestParam Long userTaskId,
            @RequestBody Map<String, TaskStatus> statusRequest) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateTaskStatus() : Updating status for task {} for user {}", userTaskId, userId);

        try {
            TaskStatus newStatus = statusRequest.get("status");
            if (newStatus == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Status is required", null));
            }

            UserTasksDTO updatedTask = userTasksService.updateTaskStatus(userId, userTaskId, newStatus);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task status updated successfully.", updatedTask));
        } catch (EntityNotFoundException e) {
            log.warn("updateTaskStatus() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("updateTaskStatus() : Error updating status for task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update task status: " + e.getMessage(), null));
        }
    }

    /**
     * Update task priority for the authenticated user.
     * Endpoint: PATCH /tasks/priority/update?userTaskId=1
     *
     * @param userTaskId      The ID of the task to update
     * @param priorityRequest The new priority data
     * @return ResponseEntity with updated task priority
     */
    @PatchMapping(AppConstants.TASKS_PRIORITY_UPDATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> updateTaskPriority(
            @RequestParam Long userTaskId,
            @RequestBody Map<String, TaskPriority> priorityRequest) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateTaskPriority() : Updating priority for task {} for user {}", userTaskId, userId);

        try {
            TaskPriority newPriority = priorityRequest.get("priority");
            if (newPriority == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Priority is required", null));
            }

            UserTasksDTO updatedTask = userTasksService.updateTaskPriority(userId, userTaskId, newPriority);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task priority updated successfully.", updatedTask));
        } catch (EntityNotFoundException e) {
            log.warn("updateTaskPriority() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("updateTaskPriority() : Error updating priority for task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update task priority: " + e.getMessage(), null));
        }
    }

    // Search and Filter Operations

    /**
     * Search tasks for the authenticated user.
     * Endpoint: GET /tasks/search?searchTerm=authentication&status=IN_PROGRESS
     *
     * @param searchTerm The search term
     * @param page       Page number (default: 0)
     * @param limit      Page size (default: 50)
     * @param status     Filter by status
     * @param priority   Filter by priority
     * @param category   Filter by category
     * @param sortBy     Sort field
     * @param sortOrder  Sort direction
     * @return ResponseEntity with search results
     */
    @GetMapping(AppConstants.TASKS_SEARCH_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksListResponseDTO>> searchTasks(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskCategory category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("searchTasks() : Searching tasks for user {} with term: {}", userId, searchTerm);

        try {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(limit, 100), Sort.by(direction, sortBy));

            UserTasksListResponseDTO result = userTasksService.searchTasks(userId, searchTerm, pageable);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Search completed successfully.", result));
        } catch (Exception e) {
            log.error("searchTasks() : Error searching tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to search tasks: " + e.getMessage(), null));
        }
    }

    /**
     * Get overdue tasks for the authenticated user.
     * Endpoint: GET /tasks/overdue
     *
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 50)
     * @param sortBy    Sort field
     * @param sortOrder Sort direction
     * @return ResponseEntity with overdue tasks
     */
    @GetMapping(AppConstants.TASKS_OVERDUE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksListResponseDTO>> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getOverdueTasks() : Fetching overdue tasks for user {}", userId);

        try {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(limit, 100), Sort.by(direction, sortBy));

            UserTasksListResponseDTO result = userTasksService.getOverdueTasks(userId, pageable);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Overdue tasks retrieved successfully.", result));
        } catch (Exception e) {
            log.error("getOverdueTasks() : Error fetching overdue tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve overdue tasks: " + e.getMessage(), null));
        }
    }

    /**
     * Get tasks by status for the authenticated user.
     * Endpoint: GET /tasks/by-status?status=IN_PROGRESS&page=0&limit=10
     *
     * @param status    The task status to filter by
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 50)
     * @param sortBy    Sort field
     * @param sortOrder Sort direction
     * @return ResponseEntity with tasks filtered by status
     */
    @GetMapping(AppConstants.TASKS_BY_STATUS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksListResponseDTO>> getTasksByStatus(
            @RequestParam TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getTasksByStatus() : Fetching tasks by status {} for user {}", status, userId);

        try {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(limit, 100), Sort.by(direction, sortBy));

            UserTasksListResponseDTO result = userTasksService.getTasksByStatus(userId, status, pageable);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Tasks retrieved by status successfully.", result));
        } catch (Exception e) {
            log.error("getTasksByStatus() : Error fetching tasks by status for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve tasks by status: " + e.getMessage(), null));
        }
    }

    /**
     * Get tasks by priority for the authenticated user.
     * Endpoint: GET /tasks/by-priority?priority=HIGH&page=0&limit=10
     *
     * @param priority  The task priority to filter by
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 50)
     * @param sortBy    Sort field
     * @param sortOrder Sort direction
     * @return ResponseEntity with tasks filtered by priority
     */
    @GetMapping(AppConstants.TASKS_BY_PRIORITY_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksListResponseDTO>> getTasksByPriority(
            @RequestParam TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getTasksByPriority() : Fetching tasks by priority {} for user {}", priority, userId);

        try {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(limit, 100), Sort.by(direction, sortBy));

            UserTasksListResponseDTO result = userTasksService.getTasksByPriority(userId, priority, pageable);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Tasks retrieved by priority successfully.", result));
        } catch (Exception e) {
            log.error("getTasksByPriority() : Error fetching tasks by priority for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve tasks by priority: " + e.getMessage(), null));
        }
    }

    // Statistics Operations

    /**
     * Get task statistics for the authenticated user.
     * Endpoint: GET /tasks/stats
     *
     * @return ResponseEntity with task statistics
     */
    @GetMapping(AppConstants.TASKS_STATS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksStatsDTO>> getTaskStats() {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getTaskStats() : Fetching task statistics for user {}", userId);

        try {
            UserTasksStatsDTO stats = userTasksService.getTaskStats(userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task statistics retrieved successfully.", stats));
        } catch (Exception e) {
            log.error("getTaskStats() : Error fetching task statistics for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve task statistics: " + e.getMessage(), null));
        }
    }

    // Bulk Operations

    /**
     * Bulk update tasks for the authenticated user.
     * Endpoint: PUT /tasks/bulk-update
     *
     * @param bulkUpdateRequest The bulk update request
     * @return ResponseEntity with bulk update result
     */
    @PutMapping(AppConstants.TASKS_BULK_UPDATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<List<UserTasksDTO>>> bulkUpdateTasks(
            @RequestBody @Valid UserTasksBulkUpdateRequest bulkUpdateRequest) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("bulkUpdateTasks() : Bulk updating tasks for user {}", userId);

        try {
            List<UserTasksDTO> updatedTasks = userTasksService.bulkUpdateTasks(userId, bulkUpdateRequest);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Bulk update completed.", updatedTasks));
        } catch (Exception e) {
            log.error("bulkUpdateTasks() : Error bulk updating tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to bulk update tasks: " + e.getMessage(), null));
        }
    }

    /**
     * Bulk delete tasks for the authenticated user.
     * Endpoint: DELETE /tasks/bulk-delete
     *
     * @param bulkDeleteRequest The bulk delete request
     * @return ResponseEntity with bulk delete result
     */
    @DeleteMapping(AppConstants.TASKS_BULK_DELETE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<Void>> bulkDeleteTasks(
            @RequestBody @Valid UserTasksBulkDeleteRequest bulkDeleteRequest) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("bulkDeleteTasks() : Bulk deleting tasks for user {}", userId);

        try {
            userTasksService.bulkDeleteTasks(userId, bulkDeleteRequest);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Bulk deletion completed.", null));
        } catch (Exception e) {
            log.error("bulkDeleteTasks() : Error bulk deleting tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to bulk delete tasks: " + e.getMessage(), null));
        }
    }

    // Additional Operations

    /**
     * Duplicate a task for the authenticated user.
     * Endpoint: POST /tasks/duplicate?userTaskId=1
     *
     * @param userTaskId The ID of the task to duplicate
     * @return ResponseEntity with the duplicated task
     */
    @PostMapping(AppConstants.TASKS_DUPLICATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserTasksDTO>> duplicateTask(@RequestParam Long userTaskId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("duplicateTask() : Duplicating task {} for user {}", userTaskId, userId);

        try {
            UserTasksDTO duplicatedTask = userTasksService.duplicateTask(userId, userTaskId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Task duplicated successfully.", duplicatedTask));
        } catch (EntityNotFoundException e) {
            log.warn("duplicateTask() : Task {} not found for user {}", userTaskId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Task not found", null));
        } catch (Exception e) {
            log.error("duplicateTask() : Error duplicating task {} for user {}: {}", userTaskId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to duplicate task: " + e.getMessage(), null));
        }
    }

    // Helper Methods

    /**
     * Check if any filters are applied.
     */
    private boolean hasFilters(TaskStatus status, TaskPriority priority, TaskCategory category, TaskType taskType,
                               LocalDate startDate, LocalDate endDate, LocalDate dueDateStart, LocalDate dueDateEnd,
                               String searchTerm, String[] tags) {
        return status != null || priority != null || category != null || taskType != null ||
                startDate != null || endDate != null || dueDateStart != null || dueDateEnd != null ||
                searchTerm != null || tags != null;
    }
}
