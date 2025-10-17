package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.model.DailyTaskDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.DailyTaskService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controller for handling daily task-related operations with role-based authorization.
 * Provides endpoints for creating, updating, deleting, and retrieving daily tasks.
 *
 * <p>Author: Siddhant Patni</p>
 */
@RestController
@Slf4j
@CrossOrigin
public class DailyTaskController {

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private JwtAuthenticationContext authContext;

    /**
     * Creates a new daily task.
     * Users can only create tasks for themselves unless they are admin.
     *
     * @param dailyTaskDTO the daily task information
     * @return ResponseEntity with a ResponseDTO containing the created daily task
     */
    @PostMapping(EndpointConstants.DAILY_TASKS_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> createDailyTask(@RequestBody @Valid DailyTaskDTO dailyTaskDTO) {
        log.info("createDailyTask() : Creating daily task for user ID: {}", dailyTaskDTO.getUserId());

        // Validate user can create task for the specified userId
        if (!authContext.isOwnerOrAdmin(dailyTaskDTO.getUserId())) {
            log.warn("createDailyTask() : User {} attempted to create task for user {}",
                    authContext.getCurrentUserId(), dailyTaskDTO.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only create tasks for yourself", null));
        }

        try {
            DailyTaskDTO createdTask = dailyTaskService.createDailyTask(dailyTaskDTO);
            log.info("createDailyTask() : Daily task created successfully with ID: {}", createdTask.getDailyTaskId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily task created successfully", createdTask));
        } catch (EntityNotFoundException e) {
            log.warn("createDailyTask() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), null));
        } catch (Exception e) {
            log.error("createDailyTask() : Failed to create daily task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to create daily task: " + e.getMessage(), null));
        }
    }

    /**
     * Updates an existing daily task.
     * Users can only update their own tasks unless they are admin.
     *
     * @param taskId       the ID of the task to update
     * @param dailyTaskDTO the updated task information
     * @return ResponseEntity with a ResponseDTO containing the updated daily task
     */
    @PutMapping(EndpointConstants.DAILY_TASKS_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> updateDailyTask(@RequestParam("taskId") Long taskId,
                                                                     @RequestBody @Valid DailyTaskDTO dailyTaskDTO) {

        log.info("updateDailyTask() : Updating daily task with ID: {}", taskId);

        // Validate user can update task for the specified userId
        if (!authContext.isOwnerOrAdmin(dailyTaskDTO.getUserId())) {
            log.warn("updateDailyTask() : User {} attempted to update task for user {}",
                    authContext.getCurrentUserId(), dailyTaskDTO.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only update your own tasks", null));
        }

        try {
            DailyTaskDTO updatedTask = dailyTaskService.updateDailyTask(taskId, dailyTaskDTO);
            log.info("updateDailyTask() : Daily task updated successfully with ID: {}", updatedTask.getDailyTaskId());

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily task updated successfully", updatedTask));
        } catch (EntityNotFoundException e) {
            log.warn("updateDailyTask() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), null));
        } catch (Exception e) {
            log.error("updateDailyTask() : Failed to update daily task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update daily task: " + e.getMessage(), null));
        }
    }

    /**
     * Deletes a daily task by its ID.
     * Users can only delete their own tasks unless they are admin.
     *
     * @param taskId the ID of the task to delete
     * @return ResponseEntity with a ResponseDTO indicating the result of the operation
     */
    @DeleteMapping(EndpointConstants.DAILY_TASKS_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<Void>> deleteDailyTask(@RequestParam("taskId") Long taskId) {
        log.info("deleteDailyTask() : Deleting daily task with ID: {}", taskId);

        try {
            // First get the task to validate ownership
            DailyTaskDTO task = dailyTaskService.getDailyTaskById(taskId);

            // Validate user can delete this task (owner or admin)
            if (!authContext.isOwnerOrAdmin(task.getUserId())) {
                log.warn("deleteDailyTask() : User {} attempted to delete task belonging to user {}",
                        authContext.getCurrentUserId(), task.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only delete your own tasks", null));
            }

            dailyTaskService.deleteDailyTask(taskId);
            log.info("deleteDailyTask() : Daily task deleted successfully with ID: {}", taskId);

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily task deleted successfully", null));
        } catch (EntityNotFoundException e) {
            log.warn("deleteDailyTask() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), null));
        } catch (Exception e) {
            log.error("deleteDailyTask() : Failed to delete daily task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to delete daily task: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a daily task by its ID.
     * Users can only view their own tasks unless they are admin.
     *
     * @param taskId the ID of the task to retrieve
     * @return ResponseEntity with a ResponseDTO containing the retrieved daily task
     */
    @GetMapping(EndpointConstants.DAILY_TASKS_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> getDailyTaskById(@RequestParam("taskId") Long taskId) {
        log.info("getDailyTaskById() : Retrieving daily task with ID: {}", taskId);

        try {
            DailyTaskDTO task = dailyTaskService.getDailyTaskById(taskId);

            // Validate user can view this task (owner or admin)
            if (!authContext.isOwnerOrAdmin(task.getUserId())) {
                log.warn("getDailyTaskById() : User {} attempted to view task belonging to user {}",
                        authContext.getCurrentUserId(), task.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only view your own tasks", null));
            }

            log.info("getDailyTaskById() : Daily task retrieved successfully with ID: {}", taskId);

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily task retrieved successfully", task));
        } catch (EntityNotFoundException e) {
            log.warn("getDailyTaskById() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), null));
        } catch (Exception e) {
            log.error("getDailyTaskById() : Failed to retrieve daily task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve daily task: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all daily tasks for a specific user.
     * Users can only view their own tasks unless they are admin.
     *
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(EndpointConstants.USER_DAILY_TASKS_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getUserDailyTasks() {

        Long userId = authContext.getCurrentUserId();
        log.info("getUserDailyTasks() : Fetching daily tasks for user ID: {}", userId);

        // Validate user can view tasks for the specified userId
        if (!authContext.isOwnerOrAdmin(userId)) {
            log.warn("getUserDailyTasks() : User {} attempted to view tasks for user {}",
                    authContext.getCurrentUserId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only view your own tasks", null));
        }

        try {
            List<DailyTaskDTO> tasks = dailyTaskService.getUserDailyTasks(userId);
            log.info("getUserDailyTasks() : Retrieved {} daily tasks for user ID: {}", tasks.size(), userId);

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily tasks retrieved successfully", tasks));
        } catch (Exception e) {
            log.error("getUserDailyTasks() : Failed to fetch daily tasks for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to fetch daily tasks: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * Retrieves daily tasks for a user within a date range.
     * Users can only view their own tasks unless they are admin.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(EndpointConstants.USER_DAILY_TASKS_DATE_RANGE_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getDailyTasksByUserIdAndDateRange(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                             @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = authContext.getCurrentUserId();
        log.info("getDailyTasksByUserIdAndDateRange() : Retrieving daily tasks for user ID: {} between {} and {}",
                userId, startDate, endDate);

        // Validate user can view tasks for the specified userId
        if (!authContext.isOwnerOrAdmin(userId)) {
            log.warn("getDailyTasksByUserIdAndDateRange() : User {} attempted to view tasks for user {}",
                    authContext.getCurrentUserId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only view your own tasks", Collections.emptyList()));
        }

        try {
            List<DailyTaskDTO> tasks = dailyTaskService.getDailyTasksByUserIdAndDateRange(userId, startDate, endDate);

            if (tasks.isEmpty()) {
                log.warn("getDailyTasksByUserIdAndDateRange() : No daily tasks found for user ID: {} in date range", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "No daily tasks found for the user in the specified date range", Collections.emptyList()));
            }

            log.info("getDailyTasksByUserIdAndDateRange() : Retrieved {} daily tasks for user ID: {} in date range",
                    tasks.size(), userId);

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily tasks retrieved successfully", tasks));
        } catch (EntityNotFoundException e) {
            log.warn("getDailyTasksByUserIdAndDateRange() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), Collections.emptyList()));
        } catch (Exception e) {
            log.error("getDailyTasksByUserIdAndDateRange() : Failed to retrieve daily tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve daily tasks: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * Retrieves daily tasks for a specific date.
     * Users can only view their own tasks unless they are admin.
     *
     * @param date the date to filter by
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(EndpointConstants.USER_DAILY_TASKS_DATE_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getDailyTasksByUserIdAndDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = authContext.getCurrentUserId();
        log.info("getDailyTasksByUserIdAndDate() : Retrieving daily tasks for user ID: {} on date: {}", userId, date);

        // Validate user can view tasks for the specified userId
        if (!authContext.isOwnerOrAdmin(userId)) {
            log.warn("getDailyTasksByUserIdAndDate() : User {} attempted to view tasks for user {}",
                    authContext.getCurrentUserId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only view your own tasks", Collections.emptyList()));
        }

        try {
            List<DailyTaskDTO> tasks = dailyTaskService.getDailyTasksByUserIdAndDate(userId, date);

            if (tasks.isEmpty()) {
                log.warn("getDailyTasksByUserIdAndDate() : No daily tasks found for user ID: {} on date: {}", userId, date);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "No daily tasks found for the user on the specified date", Collections.emptyList()));
            }

            log.info("getDailyTasksByUserIdAndDate() : Retrieved {} daily tasks for user ID: {} on date: {}",
                    tasks.size(), userId, date);

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily tasks retrieved successfully", tasks));
        } catch (EntityNotFoundException e) {
            log.warn("getDailyTasksByUserIdAndDate() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), Collections.emptyList()));
        } catch (Exception e) {
            log.error("getDailyTasksByUserIdAndDate() : Failed to retrieve daily tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve daily tasks: " + e.getMessage(), Collections.emptyList()));
        }
    }
}
