package com.sid.app.service;

import com.sid.app.entity.DailyTask;
import com.sid.app.entity.User;
import com.sid.app.model.DailyTaskDTO;
import com.sid.app.repository.DailyTaskRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.utils.ApplicationUtils;
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
        log.info("createDailyTask: Creating daily task for user ID: {}", dailyTaskDTO.getUserId());

        User user = userRepository.findById(dailyTaskDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dailyTaskDTO.getUserId()));

        DailyTask dailyTask = new DailyTask();
        mapDtoToEntity(dailyTaskDTO, dailyTask, user);

        // If day is not provided, determine it from the date
        if (dailyTask.getDailyTaskDay() == null && dailyTask.getDailyTaskDate() != null) {
            DayOfWeek dayOfWeek = dailyTask.getDailyTaskDate().getDayOfWeek();
            dailyTask.setDailyTaskDay(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        // Generate and set task number
        String taskNumber = generateTaskNumber(user.getUserId());
        dailyTask.setTaskNumber(taskNumber);
        log.info("createDailyTask: Generated task number: {} for user ID: {}", taskNumber, user.getUserId());

        DailyTask savedTask = dailyTaskRepository.save(dailyTask);
        log.info("createDailyTask: Daily task created successfully with ID: {}", savedTask.getDailyTaskId());

        return mapEntityToDto(savedTask);
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
        log.info("updateDailyTask: Updating daily task with ID: {}", taskId);

        DailyTask existingTask = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Daily task not found with ID: " + taskId));

        User user = userRepository.findById(dailyTaskDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dailyTaskDTO.getUserId()));

        mapDtoToEntity(dailyTaskDTO, existingTask, user);

        // If day is not provided, determine it from the date
        if (existingTask.getDailyTaskDay() == null && existingTask.getDailyTaskDate() != null) {
            DayOfWeek dayOfWeek = existingTask.getDailyTaskDate().getDayOfWeek();
            existingTask.setDailyTaskDay(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        // Preserve the existing task number if it matches the pattern, otherwise generate a new one
        if (existingTask.getTaskNumber() == null || !existingTask.getTaskNumber().matches("TSK-" + user.getUserId() + "-\\d+")) {
            String taskNumber = generateTaskNumber(user.getUserId());
            existingTask.setTaskNumber(taskNumber);
            log.info("updateDailyTask: Generated new task number: {} for user ID: {}", taskNumber, user.getUserId());
        }

        DailyTask updatedTask = dailyTaskRepository.save(existingTask);
        log.info("updateDailyTask: Daily task updated successfully with ID: {}", updatedTask.getDailyTaskId());

        return mapEntityToDto(updatedTask);
    }

    /**
     * Generates a task number in the format TSK-userId-number.
     * The number is incremented based on the latest task number for the user.
     *
     * @param userId the user ID
     * @return the generated task number
     */
    private String generateTaskNumber(Long userId) {
        List<String> latestTaskNumbers = dailyTaskRepository.findLatestTaskNumbersByUserId(userId);

        int nextNumber = INITIAL_TASK_NUMBER; // Default starting number

        if (latestTaskNumbers != null && !latestTaskNumbers.isEmpty()) {
            // Try to find the highest task number for this user
            for (String taskNumber : latestTaskNumbers) {
                if (taskNumber != null) {
                    Matcher matcher = TASK_NUMBER_PATTERN.matcher(taskNumber);
                    if (matcher.matches() && matcher.group(1).equals(userId.toString())) {
                        try {
                            int currentNumber = Integer.parseInt(matcher.group(2));
                            nextNumber = Math.max(nextNumber, currentNumber + 1);
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse task number from {}: {}", taskNumber, e.getMessage());
                        }
                    }
                }
            }
        }

        return TASK_NUMBER_PREFIX + userId + "-" + nextNumber;
    }

    /**
     * Deletes a daily task by its ID.
     *
     * @param taskId the ID of the task to delete
     * @throws EntityNotFoundException if the task is not found
     */
    @Transactional
    public void deleteDailyTask(Long taskId) {
        log.info("deleteDailyTask: Deleting daily task with ID: {}", taskId);

        if (!dailyTaskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Daily task not found with ID: " + taskId);
        }

        dailyTaskRepository.deleteById(taskId);
        log.info("deleteDailyTask: Daily task deleted successfully with ID: {}", taskId);
    }

    /**
     * Retrieves a daily task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the task DTO
     * @throws EntityNotFoundException if the task is not found
     */
    public DailyTaskDTO getDailyTaskById(Long taskId) {
        log.info("getDailyTaskById: Retrieving daily task with ID: {}", taskId);

        DailyTask dailyTask = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Daily task not found with ID: " + taskId));

        log.info("getDailyTaskById: Daily task retrieved successfully with ID: {}", taskId);
        return mapEntityToDto(dailyTask);
    }

    /**
     * Retrieves all daily tasks for a specific user.
     *
     * @param userId the ID of the user
     * @return list of daily task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getDailyTasksByUserId(Long userId) {
        log.info("getDailyTasksByUserId: Retrieving daily tasks for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUser(user);
        log.info("getDailyTasksByUserId: Retrieved {} daily tasks for user ID: {}", tasks.size(), userId);

        return tasks.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves daily tasks for a user within a date range.
     *
     * @param userId    the ID of the user
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return list of daily task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getDailyTasksByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("getDailyTasksByUserIdAndDateRange: Retrieving daily tasks for user ID: {} between {} and {}",
                userId, startDate, endDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUserAndDailyTaskDateBetween(user, startDate, endDate);
        log.info("getDailyTasksByUserIdAndDateRange: Retrieved {} daily tasks for user ID: {} in date range",
                tasks.size(), userId);

        return tasks.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves daily tasks for a specific date.
     *
     * @param userId the ID of the user
     * @param date   the date to filter by
     * @return list of daily task DTOs
     * @throws EntityNotFoundException if the user is not found
     */
    public List<DailyTaskDTO> getDailyTasksByUserIdAndDate(Long userId, LocalDate date) {
        log.info("getDailyTasksByUserIdAndDate: Retrieving daily tasks for user ID: {} on date: {}", userId, date);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<DailyTask> tasks = dailyTaskRepository.findByUserAndDailyTaskDate(user, date);
        log.info("getDailyTasksByUserIdAndDate: Retrieved {} daily tasks for user ID: {} on date: {}",
                tasks.size(), userId, date);

        return tasks.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    /**
     * Maps a DTO to an entity.
     *
     * @param dto    the source DTO
     * @param entity the target entity
     * @param user   the associated user
     */
    private void mapDtoToEntity(DailyTaskDTO dto, DailyTask entity, User user) {
        entity.setUser(user);
        entity.setDailyTaskDate(dto.getDailyTaskDate());
        entity.setDailyTaskDay(dto.getDailyTaskDay());
        // taskNumber is intentionally not mapped from DTO as it's auto-generated
        entity.setProjectCode(dto.getProjectCode());
        entity.setProjectName(dto.getProjectName());
        entity.setStoryTaskBugNumber(dto.getStoryTaskBugNumber());
        entity.setTaskDetails(dto.getTaskDetails());
        entity.setRemarks(dto.getRemarks());
    }

    /**
     * Maps an entity to a DTO.
     *
     * @param entity the source entity
     * @return the mapped DTO
     */
    private DailyTaskDTO mapEntityToDto(DailyTask entity) {
        DailyTaskDTO dto = new DailyTaskDTO();
        dto.setDailyTaskId(entity.getDailyTaskId());
        dto.setUserId(entity.getUser().getUserId());
        dto.setDailyTaskDate(entity.getDailyTaskDate());
        dto.setDailyTaskDay(entity.getDailyTaskDay());
        dto.setTaskNumber(entity.getTaskNumber());
        dto.setProjectCode(entity.getProjectCode());
        dto.setProjectName(entity.getProjectName());
        dto.setStoryTaskBugNumber(entity.getStoryTaskBugNumber());
        dto.setTaskDetails(entity.getTaskDetails());
        dto.setRemarks(entity.getRemarks());

        return dto;
    }
}
