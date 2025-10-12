package com.sid.app.service.impl;

import com.sid.app.entity.UserTasks;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import com.sid.app.model.*;
import com.sid.app.repository.UserTasksRepository;
import com.sid.app.service.UserTasksService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserTasksServiceImpl implements UserTasksService {

    private final UserTasksRepository userTasksRepository;

    @Override
    public UserTasksDTO createTask(Long userId, UserTasksDTO taskDTO) {
        log.info("Creating task for user: {}", userId);

        UserTasks task = UserTasks.builder()
                .userId(userId)
                .taskTitle(taskDTO.getTaskTitle())
                .taskDescription(taskDTO.getTaskDescription())
                .taskDate(taskDTO.getTaskDate())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.NOT_STARTED)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.MEDIUM)
                .category(taskDTO.getCategory() != null ? taskDTO.getCategory() : TaskCategory.WORK)
                .taskType(taskDTO.getTaskType() != null ? taskDTO.getTaskType() : TaskType.TASK)
                .dueDate(taskDTO.getDueDate())
                .reminderDate(taskDTO.getReminderDate())
                .tags(taskDTO.getTags())
                .parentTaskId(taskDTO.getParentTaskId())
                .createdBy(userId)
                .remarks(taskDTO.getRemarks())
                .isRecurring(taskDTO.getIsRecurring() != null ? taskDTO.getIsRecurring() : false)
                .recurringPattern(taskDTO.getRecurringPattern())
                .version(1)
                .accessCount(0)
                .build();

        UserTasks savedTask = userTasksRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getUserTaskId());
        return convertToDTO(savedTask);
    }

    @Override
    @Transactional
    public UserTasksDTO getTaskById(Long userId, Long userTaskId) {
        log.info("Fetching task {} for user {}", userTaskId, userId);

        UserTasks task = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        // Update access count and last accessed date
        userTasksRepository.updateAccessInfo(userTaskId, userId, LocalDateTime.now());

        return convertToDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getAllUserTasks(Long userId, Pageable pageable) {
        log.info("Fetching all tasks for user: {} with pagination: {}", userId, pageable);

        Page<UserTasks> tasksPage = userTasksRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    public UserTasksDTO updateTask(Long userId, Long userTaskId, UserTasksDTO taskDTO) {
        log.info("Updating task {} for user {}", userTaskId, userId);

        UserTasks existingTask = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        // Update fields
        existingTask.setTaskTitle(taskDTO.getTaskTitle());
        existingTask.setTaskDescription(taskDTO.getTaskDescription());
        existingTask.setTaskDate(taskDTO.getTaskDate());
        if (taskDTO.getStatus() != null) existingTask.setStatus(taskDTO.getStatus());
        if (taskDTO.getPriority() != null) existingTask.setPriority(taskDTO.getPriority());
        if (taskDTO.getCategory() != null) existingTask.setCategory(taskDTO.getCategory());
        if (taskDTO.getTaskType() != null) existingTask.setTaskType(taskDTO.getTaskType());
        existingTask.setDueDate(taskDTO.getDueDate());
        existingTask.setReminderDate(taskDTO.getReminderDate());
        existingTask.setTags(taskDTO.getTags());
        existingTask.setParentTaskId(taskDTO.getParentTaskId());
        existingTask.setRemarks(taskDTO.getRemarks());
        if (taskDTO.getIsRecurring() != null) existingTask.setIsRecurring(taskDTO.getIsRecurring());
        existingTask.setRecurringPattern(taskDTO.getRecurringPattern());
        existingTask.setVersion(existingTask.getVersion() + 1);

        UserTasks updatedTask = userTasksRepository.save(existingTask);
        log.info("Task updated successfully: {}", userTaskId);
        return convertToDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long userId, Long userTaskId) {
        log.info("Deleting task {} for user {}", userTaskId, userId);

        UserTasks task = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        // Check if task has subtasks
        Long subtaskCount = userTasksRepository.countSubtasks(userTaskId);
        if (subtaskCount > 0) {
            throw new IllegalStateException("Cannot delete task with active subtasks. Delete subtasks first.");
        }

        userTasksRepository.delete(task);
        log.info("Task deleted successfully: {}", userTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getTasksWithFilters(Long userId, TaskStatus status, TaskPriority priority,
                                                        TaskCategory category, TaskType taskType,
                                                        LocalDate startDate, LocalDate endDate,
                                                        LocalDate dueDateStart, LocalDate dueDateEnd,
                                                        Boolean isRecurring, String searchTerm,
                                                        String[] tags, Pageable pageable) {
        log.info("Fetching filtered tasks for user: {}", userId);

        Page<UserTasks> tasksPage;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            tasksPage = userTasksRepository.searchTasks(userId, searchTerm.trim(), pageable);
        } else if (tags != null && tags.length > 0) {
            tasksPage = userTasksRepository.findByUserIdAndTagsContaining(userId, tags, pageable);
        } else {
            tasksPage = userTasksRepository.findTasksWithFilters(userId, status, priority, category,
                    taskType, startDate, endDate, dueDateStart, dueDateEnd, isRecurring, pageable);
        }

        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getTasksByStatus(Long userId, TaskStatus status, Pageable pageable) {
        log.info("Fetching tasks by status {} for user: {}", status, userId);

        Page<UserTasks> tasksPage = userTasksRepository.findByUserIdAndStatus(userId, status, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getTasksByPriority(Long userId, TaskPriority priority, Pageable pageable) {
        log.info("Fetching tasks by priority {} for user: {}", priority, userId);

        Page<UserTasks> tasksPage = userTasksRepository.findByUserIdAndPriority(userId, priority, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getTasksByCategory(Long userId, TaskCategory category, Pageable pageable) {
        log.info("Fetching tasks by category {} for user: {}", category, userId);

        Page<UserTasks> tasksPage = userTasksRepository.findByUserIdAndCategory(userId, category, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getTasksByType(Long userId, TaskType taskType, Pageable pageable) {
        log.info("Fetching tasks by type {} for user: {}", taskType, userId);

        Page<UserTasks> tasksPage = userTasksRepository.findByUserIdAndTaskType(userId, taskType, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO searchTasks(Long userId, String searchTerm, Pageable pageable) {
        log.info("Searching tasks for user: {} with term: {}", userId, searchTerm);

        Page<UserTasks> tasksPage = userTasksRepository.searchTasks(userId, searchTerm, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksListResponseDTO getOverdueTasks(Long userId, Pageable pageable) {
        log.info("Fetching overdue tasks for user: {}", userId);

        List<TaskStatus> excludeStatuses = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED);
        Page<UserTasks> tasksPage = userTasksRepository.findOverdueTasks(userId, LocalDate.now(), excludeStatuses, pageable);
        return buildTaskListResponse(tasksPage, pageable);
    }

    @Override
    public UserTasksDTO updateTaskStatus(Long userId, Long userTaskId, TaskStatus status) {
        log.info("Updating task status {} for task {} and user {}", status, userTaskId, userId);

        UserTasks task = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        task.setStatus(status);
        task.setVersion(task.getVersion() + 1);
        UserTasks updatedTask = userTasksRepository.save(task);

        return convertToDTO(updatedTask);
    }

    @Override
    public UserTasksDTO updateTaskPriority(Long userId, Long userTaskId, TaskPriority priority) {
        log.info("Updating task priority {} for task {} and user {}", priority, userTaskId, userId);

        UserTasks task = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        task.setPriority(priority);
        task.setVersion(task.getVersion() + 1);
        UserTasks updatedTask = userTasksRepository.save(task);

        return convertToDTO(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTasksStatsDTO getTaskStats(Long userId) {
        log.info("Fetching task statistics for user: {}", userId);

        Long totalTasks = userTasksRepository.countByUserId(userId);
        Long completedTasks = userTasksRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        Long inProgressTasks = userTasksRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long notStartedTasks = userTasksRepository.countByUserIdAndStatus(userId, TaskStatus.NOT_STARTED);
        Long onHoldTasks = userTasksRepository.countByUserIdAndStatus(userId, TaskStatus.ON_HOLD);
        Long cancelledTasks = userTasksRepository.countByUserIdAndStatus(userId, TaskStatus.CANCELLED);

        List<TaskStatus> excludeStatuses = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED);
        List<UserTasks> overdueTasks = userTasksRepository.findOverdueTasks(userId, LocalDate.now(), excludeStatuses);
        Long overdueTasksCount = (long) overdueTasks.size();

        Double completionRateDouble = userTasksRepository.calculateCompletionRate(userId);
        BigDecimal completionRate = completionRateDouble != null ?
            BigDecimal.valueOf(completionRateDouble).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Double avgCompletionTimeDouble = userTasksRepository.calculateAverageCompletionTime(userId);
        BigDecimal averageCompletionTime = avgCompletionTimeDouble != null ?
            BigDecimal.valueOf(avgCompletionTimeDouble).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Map<String, Long> tasksByStatus = convertToMap(userTasksRepository.countByStatus(userId));
        Map<String, Long> tasksByPriority = convertToMap(userTasksRepository.countByPriority(userId));
        Map<String, Long> tasksByCategory = convertToMap(userTasksRepository.countByCategory(userId));
        Map<String, Long> tasksByType = convertToMap(userTasksRepository.countByTaskType(userId));

        List<UserTasks> recentlyModifiedTasks = userTasksRepository.findRecentlyModified(userId, PageRequest.of(0, 5));
        List<UserTasksDTO> recentlyModified = recentlyModifiedTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<UserTasks> upcomingDeadlinesTasks = userTasksRepository.findUpcomingDeadlines(userId, today, nextWeek,
                excludeStatuses, PageRequest.of(0, 5));
        List<UserTasksDTO> upcomingDeadlines = upcomingDeadlinesTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return UserTasksStatsDTO.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .notStartedTasks(notStartedTasks)
                .onHoldTasks(onHoldTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTasksCount)
                .completionRate(completionRate)
                .averageCompletionTime(averageCompletionTime)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .tasksByCategory(tasksByCategory)
                .tasksByType(tasksByType)
                .recentlyModified(recentlyModified)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    @Override
    public List<UserTasksDTO> bulkUpdateTasks(Long userId, UserTasksBulkUpdateRequest request) {
        log.info("Bulk updating {} tasks for user {}", request.getUserTaskIds().size(), userId);

        List<UserTasks> tasks = userTasksRepository.findByUserTaskIdsAndUserId(request.getUserTaskIds(), userId);

        if (tasks.size() != request.getUserTaskIds().size()) {
            throw new EntityNotFoundException("Some tasks not found for the user");
        }

        tasks.forEach(task -> {
            if (request.getStatus() != null) task.setStatus(request.getStatus());
            if (request.getPriority() != null) task.setPriority(request.getPriority());
            if (request.getCategory() != null) task.setCategory(request.getCategory());
            if (request.getTaskType() != null) task.setTaskType(request.getTaskType());
            if (request.getRemarks() != null) task.setRemarks(request.getRemarks());
            task.setVersion(task.getVersion() + 1);
        });

        List<UserTasks> updatedTasks = userTasksRepository.saveAll(tasks);

        return updatedTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkDeleteTasks(Long userId, UserTasksBulkDeleteRequest request) {
        log.info("Bulk deleting {} tasks for user {}", request.getUserTaskIds().size(), userId);

        List<UserTasks> tasks = userTasksRepository.findByUserTaskIdsAndUserId(request.getUserTaskIds(), userId);

        if (tasks.size() != request.getUserTaskIds().size()) {
            throw new EntityNotFoundException("Some tasks not found for the user");
        }

        // Check for subtasks if cascadeDelete is false
        if (!Boolean.TRUE.equals(request.getCascadeDelete())) {
            for (UserTasks task : tasks) {
                Long subtaskCount = userTasksRepository.countSubtasks(task.getUserTaskId());
                if (subtaskCount > 0) {
                    throw new IllegalStateException("Cannot delete task " + task.getUserTaskId() + " with active subtasks. Enable cascade delete or remove subtasks first.");
                }
            }
        }

        userTasksRepository.deleteAll(tasks);
    }

    @Override
    public UserTasksDTO duplicateTask(Long userId, Long userTaskId) {
        log.info("Duplicating task {} for user {}", userTaskId, userId);

        UserTasks originalTask = userTasksRepository.findByUserTaskIdAndUserId(userTaskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + userTaskId));

        UserTasks duplicatedTask = UserTasks.builder()
                .userId(userId)
                .taskTitle("[Copy] " + originalTask.getTaskTitle())
                .taskDescription(originalTask.getTaskDescription())
                .taskDate(LocalDate.now())
                .status(TaskStatus.NOT_STARTED)
                .priority(originalTask.getPriority())
                .category(originalTask.getCategory())
                .taskType(originalTask.getTaskType())
                .dueDate(originalTask.getDueDate())
                .reminderDate(originalTask.getReminderDate())
                .tags(originalTask.getTags())
                .parentTaskId(originalTask.getParentTaskId())
                .createdBy(userId)
                .remarks(originalTask.getRemarks())
                .isRecurring(originalTask.getIsRecurring())
                .recurringPattern(originalTask.getRecurringPattern())
                .version(1)
                .accessCount(0)
                .build();

        UserTasks savedTask = userTasksRepository.save(duplicatedTask);
        return convertToDTO(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTasksDTO> getSubtasks(Long userId, Long parentTaskId) {
        log.info("Fetching subtasks for parent task {} and user {}", parentTaskId, userId);

        List<UserTasks> subtasks = userTasksRepository.findByUserIdAndParentTaskId(userId, parentTaskId);
        return subtasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserTasksDTO convertToDTO(UserTasks task) {
        return UserTasksDTO.builder()
                .userTaskId(task.getUserTaskId())
                .userId(task.getUserId())
                .taskTitle(task.getTaskTitle())
                .taskDescription(task.getTaskDescription())
                .taskDate(task.getTaskDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .category(task.getCategory())
                .taskType(task.getTaskType())
                .dueDate(task.getDueDate())
                .reminderDate(task.getReminderDate())
                .tags(task.getTags())
                .parentTaskId(task.getParentTaskId())
                .createdBy(task.getCreatedBy())
                .remarks(task.getRemarks())
                .isRecurring(task.getIsRecurring())
                .recurringPattern(task.getRecurringPattern())
                .version(task.getVersion())
                .accessCount(task.getAccessCount())
                .lastAccessedDate(task.getLastAccessedDate())
                .createdDate(task.getCreatedDate())
                .modifiedDate(task.getModifiedDate())
                .build();
    }

    private UserTasksListResponseDTO buildTaskListResponse(Page<UserTasks> tasksPage, Pageable pageable) {
        List<UserTasksDTO> taskDTOs = tasksPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = PaginationDTO.builder()
                .currentPage(pageable.getPageNumber())
                .itemsPerPage(pageable.getPageSize())
                .totalItems(tasksPage.getTotalElements())
                .totalPages(tasksPage.getTotalPages())
                .hasPreviousPage(pageable.getPageNumber() > 0)
                .hasNextPage(pageable.getPageNumber() + 1 < tasksPage.getTotalPages())
                .build();

        return UserTasksListResponseDTO.builder()
                .data(taskDTOs)
                .pagination(pagination)
                .totalElements(tasksPage.getTotalElements())
                .totalPages(tasksPage.getTotalPages())
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .hasNext(tasksPage.hasNext())
                .hasPrevious(tasksPage.hasPrevious())
                .build();
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
    }
}
