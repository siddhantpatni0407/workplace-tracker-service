package com.sid.app.service;

import com.sid.app.entity.DailyTask;
import com.sid.app.entity.User;
import com.sid.app.model.DailyTaskDTO;
import com.sid.app.repository.DailyTaskRepository;
import com.sid.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for handling Daily Task operations.
 *
 * <p>Author: Siddhant Patni</p>
 */
@Service
@Slf4j
public class DailyTaskService {

    private static final String TASK_NUMBER_PREFIX = "TSK-";
    private static final Pattern TASK_NUMBER_PATTERN = Pattern.compile("TSK-(\\d+)-(\\d+)");
    private static final int INITIAL_TASK_NUMBER = 1001;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new daily task.
     *
     * @param dailyTaskDTO the DTO containing task information
     * @return the created task DTO
     * @throws EntityNotFoundException if the user is not found
     */
    @Transactional
    public DailyTaskDTO createDailyTask(DailyTaskDTO dailyTaskDTO) {
        log.info("createDailyTask() : Creating daily task for user ID: {}", dailyTaskDTO.getUserId());

        // Validate user exists
        User user = userRepository.findById(dailyTaskDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dailyTaskDTO.getUserId()));

        // Generate task number if not provided
        if (dailyTaskDTO.getTaskNumber() == null || dailyTaskDTO.getTaskNumber().isEmpty()) {
            dailyTaskDTO.setTaskNumber(generateTaskNumber(dailyTaskDTO.getUserId()));
        }

        // Convert DTO to entity
        DailyTask dailyTask = convertToEntity(dailyTaskDTO, user);

        // Save the entity
        DailyTask savedTask = dailyTaskRepository.save(dailyTask);

        log.info("createDailyTask() : Daily task created successfully with ID: {}", savedTask.getDailyTaskId());
        return convertToDTO(savedTask);
    }

    /**
     * Updates an existing daily task.
     *
     * @param taskId       the ID of the task to update
     * @param dailyTaskDTO the updated task information
     * @return the updated task DTO
     * @throws EntityNotFoundException if the task or user is not found
     */
    @Transactional
    public DailyTaskDTO updateDailyTask(Long taskId, DailyTaskDTO dailyTaskDTO) {
        log.info("updateDailyTask() : Updating daily task with ID: {}", taskId);

        // Find existing task
        DailyTask existingTask = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Daily task not found with ID: " + taskId));

        // Validate user exists
        User user = userRepository.findById(dailyTaskDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dailyTaskDTO.getUserId()));

        // Update task fields
        updateTaskFromDTO(existingTask, dailyTaskDTO, user);

        // Save the updated entity
        DailyTask updatedTask = dailyTaskRepository.save(existingTask);

        log.info("updateDailyTask() : Daily task updated successfully with ID: {}", updatedTask.getDailyTaskId());
        return convertToDTO(updatedTask);
    }

    /**
     * Deletes a daily task by its ID.
     *
     * @param taskId the ID of the task to delete
     * @throws EntityNotFoundException if the task is not found
     */
    @Transactional
    public void deleteDailyTask(Long taskId) {
        log.info("deleteDailyTask() : Deleting daily task with ID: {}", taskId);

        if (!dailyTaskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Daily task not found with ID: " + taskId);
        }

        dailyTaskRepository.deleteById(taskId);
        log.info("deleteDailyTask() : Daily task deleted successfully with ID: {}", taskId);
    }

    /**
     * Retrieves a daily task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the task DTO
     * @throws EntityNotFoundException if the task is not found
     */
    public DailyTaskDTO getDailyTaskById(Long taskId) {
        log.info("getDailyTaskById() : Retrieving daily task with ID: {}", taskId);

        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Daily task not found with ID: " + taskId));

        return convertToDTO(task);
    }

    /**
     * Retrieves all daily tasks for a specific user.
     *
     * @param userId the ID of the user whose tasks to retrieve
     * @return list of task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getUserDailyTasks(Long userId) {
        log.info("getUserDailyTasks() : Fetching daily tasks for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUser(user);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves daily tasks for a user within a date range.
     *
     * @param userId    the ID of the user
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return list of task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getDailyTasksByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("getDailyTasksByUserIdAndDateRange() : Retrieving daily tasks for user ID: {} between {} and {}",
                userId, startDate, endDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUserAndDailyTaskDateBetween(user, startDate, endDate);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves daily tasks for a specific user and date.
     *
     * @param userId the ID of the user
     * @param date   the date to filter by
     * @return list of task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getDailyTasksByUserIdAndDate(Long userId, LocalDate date) {
        log.info("getDailyTasksByUserIdAndDate() : Retrieving daily tasks for user ID: {} on date: {}", userId, date);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUserAndDailyTaskDate(user, date);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique task number for the user.
     *
     * @param userId the user ID
     * @return generated task number
     */
    private String generateTaskNumber(Long userId) {
        // Find the highest task number for the user
        String pattern = TASK_NUMBER_PREFIX + userId + "-%";
        List<DailyTask> userTasks = dailyTaskRepository.findByTaskNumberLike(pattern);

        int maxNumber = INITIAL_TASK_NUMBER;
        for (DailyTask task : userTasks) {
            Matcher matcher = TASK_NUMBER_PATTERN.matcher(task.getTaskNumber());
            if (matcher.matches()) {
                int taskNumber = Integer.parseInt(matcher.group(2));
                if (taskNumber >= maxNumber) {
                    maxNumber = taskNumber + 1;
                }
            }
        }

        return TASK_NUMBER_PREFIX + userId + "-" + maxNumber;
    }

    /**
     * Converts DailyTask entity to DTO.
     *
     * @param dailyTask the entity to convert
     * @return the DTO
     */
    private DailyTaskDTO convertToDTO(DailyTask dailyTask) {
        DailyTaskDTO dto = new DailyTaskDTO();
        dto.setDailyTaskId(dailyTask.getDailyTaskId());
        dto.setUserId(dailyTask.getUser().getUserId());
        dto.setDailyTaskDate(dailyTask.getDailyTaskDate());
        dto.setDailyTaskDay(dailyTask.getDailyTaskDay());
        dto.setTaskNumber(dailyTask.getTaskNumber());
        dto.setProjectCode(dailyTask.getProjectCode());
        dto.setProjectName(dailyTask.getProjectName());
        dto.setStoryTaskBugNumber(dailyTask.getStoryTaskBugNumber());
        dto.setTaskDetails(dailyTask.getTaskDetails());
        dto.setRemarks(dailyTask.getRemarks());

        return dto;
    }

    /**
     * Converts DailyTaskDTO to entity.
     *
     * @param dto  the DTO to convert
     * @param user the user entity
     * @return the entity
     */
    private DailyTask convertToEntity(DailyTaskDTO dto, User user) {
        DailyTask entity = new DailyTask();
        entity.setUser(user);
        entity.setDailyTaskDate(dto.getDailyTaskDate());
        entity.setTaskNumber(dto.getTaskNumber());
        entity.setProjectCode(dto.getProjectCode());
        entity.setProjectName(dto.getProjectName());
        entity.setStoryTaskBugNumber(dto.getStoryTaskBugNumber());
        entity.setTaskDetails(dto.getTaskDetails());
        entity.setRemarks(dto.getRemarks());

        // Set the day of week
        if (dto.getDailyTaskDate() != null) {
            DayOfWeek dayOfWeek = dto.getDailyTaskDate().getDayOfWeek();
            entity.setDailyTaskDay(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        return entity;
    }

    /**
     * Updates an existing task entity from DTO.
     *
     * @param existingTask the existing task entity
     * @param dto          the DTO with updated information
     * @param user         the user entity
     */
    private void updateTaskFromDTO(DailyTask existingTask, DailyTaskDTO dto, User user) {
        existingTask.setUser(user);
        existingTask.setDailyTaskDate(dto.getDailyTaskDate());
        existingTask.setTaskNumber(dto.getTaskNumber());
        existingTask.setProjectCode(dto.getProjectCode());
        existingTask.setProjectName(dto.getProjectName());
        existingTask.setStoryTaskBugNumber(dto.getStoryTaskBugNumber());
        existingTask.setTaskDetails(dto.getTaskDetails());
        existingTask.setRemarks(dto.getRemarks());

        // Update the day of week
        if (dto.getDailyTaskDate() != null) {
            DayOfWeek dayOfWeek = dto.getDailyTaskDate().getDayOfWeek();
            existingTask.setDailyTaskDay(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
    }
}
