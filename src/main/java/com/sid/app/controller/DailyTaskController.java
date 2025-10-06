package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
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
 * Controller for handling daily task-related operations.
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

    /**
     * Creates a new daily task.
     *
     * @param dailyTaskDTO the daily task information
     * @return ResponseEntity with a ResponseDTO containing the created daily task
     */
    @PostMapping(AppConstants.DAILY_TASKS_ENDPOINT)
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> createDailyTask(@RequestBody @Valid DailyTaskDTO dailyTaskDTO) {
        log.info("createDailyTask() : Creating daily task for user ID: {}", dailyTaskDTO.getUserId());

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
     *
     * @param taskId       the ID of the task to update
     * @param dailyTaskDTO the updated task information
     * @return ResponseEntity with a ResponseDTO containing the updated daily task
     */
    @PutMapping(AppConstants.DAILY_TASKS_ENDPOINT)
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> updateDailyTask(@RequestParam("taskId") Long taskId,
                                                                     @RequestBody @Valid DailyTaskDTO dailyTaskDTO) {

        log.info("updateDailyTask() : Updating daily task with ID: {}", taskId);

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
     *
     * @param taskId the ID of the task to delete
     * @return ResponseEntity with a ResponseDTO indicating the result of the operation
     */
    @DeleteMapping(AppConstants.DAILY_TASKS_ENDPOINT)
    public ResponseEntity<ResponseDTO<Void>> deleteDailyTask(@RequestParam("taskId") Long taskId) {
        log.info("deleteDailyTask() : Deleting daily task with ID: {}", taskId);

        try {
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
     *
     * @param taskId the ID of the task to retrieve
     * @return ResponseEntity with a ResponseDTO containing the retrieved daily task
     */
    @GetMapping(AppConstants.DAILY_TASKS_ENDPOINT)
    public ResponseEntity<ResponseDTO<DailyTaskDTO>> getDailyTaskById(@RequestParam("taskId") Long taskId) {
        log.info("getDailyTaskById() : Retrieving daily task with ID: {}", taskId);

        try {
            DailyTaskDTO task = dailyTaskService.getDailyTaskById(taskId);
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
     *
     * @param userId the ID of the user
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(AppConstants.USER_DAILY_TASKS_ENDPOINT)
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getDailyTasksByUserId(@RequestParam("userId") Long userId) {
        log.info("getDailyTasksByUserId() : Retrieving daily tasks for user ID: {}", userId);

        try {
            List<DailyTaskDTO> tasks = dailyTaskService.getDailyTasksByUserId(userId);

            if (tasks.isEmpty()) {
                log.warn("getDailyTasksByUserId() : No daily tasks found for user ID: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "No daily tasks found for the user", Collections.emptyList()));
            }

            log.info("getDailyTasksByUserId() : Retrieved {} daily tasks for user ID: {}", tasks.size(), userId);
            log.debug("getDailyTasksByUserId() : Tasks: {}", ApplicationUtils.getJSONString(tasks));

            return ResponseEntity.ok()
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Daily tasks retrieved successfully", tasks));
        } catch (EntityNotFoundException e) {
            log.warn("getDailyTasksByUserId() : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, e.getMessage(), Collections.emptyList()));
        } catch (Exception e) {
            log.error("getDailyTasksByUserId() : Failed to retrieve daily tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve daily tasks: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * Retrieves daily tasks for a user within a date range.
     *
     * @param userId    the ID of the user
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(AppConstants.USER_DAILY_TASKS_DATE_RANGE_ENDPOINT)
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getDailyTasksByUserIdAndDateRange(
            @RequestParam("userId") Long userId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("getDailyTasksByUserIdAndDateRange() : Retrieving daily tasks for user ID: {} between {} and {}",
                userId, startDate, endDate);

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
     *
     * @param userId the ID of the user
     * @param date   the date to filter by
     * @return ResponseEntity with a ResponseDTO containing the list of daily tasks
     */
    @GetMapping(AppConstants.USER_DAILY_TASKS_DATE_ENDPOINT)
    public ResponseEntity<ResponseDTO<List<DailyTaskDTO>>> getDailyTasksByUserIdAndDate(
            @RequestParam("userId") Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("getDailyTasksByUserIdAndDate() : Retrieving daily tasks for user ID: {} on date: {}", userId, date);

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
