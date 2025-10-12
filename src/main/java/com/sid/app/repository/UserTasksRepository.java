package com.sid.app.repository;

import com.sid.app.entity.UserTasks;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTasksRepository extends JpaRepository<UserTasks, Long> {

    // Basic CRUD operations
    Optional<UserTasks> findByUserTaskIdAndUserId(Long userTaskId, Long userId);

    Page<UserTasks> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    // Status-based queries
    Page<UserTasks> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    List<UserTasks> findByUserIdAndStatus(Long userId, TaskStatus status);

    // Priority-based queries
    Page<UserTasks> findByUserIdAndPriority(Long userId, TaskPriority priority, Pageable pageable);

    // Category-based queries
    Page<UserTasks> findByUserIdAndCategory(Long userId, TaskCategory category, Pageable pageable);

    // Type-based queries
    Page<UserTasks> findByUserIdAndTaskType(Long userId, TaskType taskType, Pageable pageable);

    // Date-based queries
    Page<UserTasks> findByUserIdAndTaskDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<UserTasks> findByUserIdAndDueDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Overdue tasks
    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId AND t.dueDate < :currentDate AND t.status NOT IN (:excludeStatuses)")
    Page<UserTasks> findOverdueTasks(@Param("userId") Long userId,
                                    @Param("currentDate") LocalDate currentDate,
                                    @Param("excludeStatuses") List<TaskStatus> excludeStatuses,
                                    Pageable pageable);

    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId AND t.dueDate < :currentDate AND t.status NOT IN (:excludeStatuses)")
    List<UserTasks> findOverdueTasks(@Param("userId") Long userId,
                                   @Param("currentDate") LocalDate currentDate,
                                   @Param("excludeStatuses") List<TaskStatus> excludeStatuses);

    // Search functionality
    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId AND " +
           "(LOWER(t.taskTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.taskDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.remarks) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserTasks> searchTasks(@Param("userId") Long userId,
                               @Param("searchTerm") String searchTerm,
                               Pageable pageable);

    // Complex filtering with multiple criteria
    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:taskType IS NULL OR t.taskType = :taskType) " +
           "AND (:startDate IS NULL OR t.taskDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.taskDate <= :endDate) " +
           "AND (:dueDateStart IS NULL OR t.dueDate >= :dueDateStart) " +
           "AND (:dueDateEnd IS NULL OR t.dueDate <= :dueDateEnd) " +
           "AND (:isRecurring IS NULL OR t.isRecurring = :isRecurring)")
    Page<UserTasks> findTasksWithFilters(@Param("userId") Long userId,
                                        @Param("status") TaskStatus status,
                                        @Param("priority") TaskPriority priority,
                                        @Param("category") TaskCategory category,
                                        @Param("taskType") TaskType taskType,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("dueDateStart") LocalDate dueDateStart,
                                        @Param("dueDateEnd") LocalDate dueDateEnd,
                                        @Param("isRecurring") Boolean isRecurring,
                                        Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(t) FROM UserTasks t WHERE t.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM UserTasks t WHERE t.userId = :userId AND t.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM UserTasks t WHERE t.userId = :userId GROUP BY t.status")
    List<Object[]> countByStatus(@Param("userId") Long userId);

    @Query("SELECT t.priority, COUNT(t) FROM UserTasks t WHERE t.userId = :userId GROUP BY t.priority")
    List<Object[]> countByPriority(@Param("userId") Long userId);

    @Query("SELECT t.category, COUNT(t) FROM UserTasks t WHERE t.userId = :userId GROUP BY t.category")
    List<Object[]> countByCategory(@Param("userId") Long userId);

    @Query("SELECT t.taskType, COUNT(t) FROM UserTasks t WHERE t.userId = :userId GROUP BY t.taskType")
    List<Object[]> countByTaskType(@Param("userId") Long userId);

    // Recently modified tasks
    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId ORDER BY t.modifiedDate DESC")
    List<UserTasks> findRecentlyModified(@Param("userId") Long userId, Pageable pageable);

    // Upcoming deadlines
    @Query("SELECT t FROM UserTasks t WHERE t.userId = :userId AND t.dueDate BETWEEN :startDate AND :endDate " +
           "AND t.status NOT IN (:excludeStatuses) ORDER BY t.dueDate ASC")
    List<UserTasks> findUpcomingDeadlines(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("excludeStatuses") List<TaskStatus> excludeStatuses,
                                         Pageable pageable);

    // Bulk operations
    @Query("SELECT t FROM UserTasks t WHERE t.userTaskId IN :userTaskIds AND t.userId = :userId")
    List<UserTasks> findByUserTaskIdsAndUserId(@Param("userTaskIds") List<Long> userTaskIds, @Param("userId") Long userId);

    // Parent-child relationship queries
    List<UserTasks> findByUserIdAndParentTaskId(Long userId, Long parentTaskId);

    @Query("SELECT COUNT(t) FROM UserTasks t WHERE t.parentTaskId = :parentTaskId")
    Long countSubtasks(@Param("parentTaskId") Long parentTaskId);

    // Update access info
    @Modifying
    @Query("UPDATE UserTasks t SET t.accessCount = t.accessCount + 1, t.lastAccessedDate = :accessTime " +
           "WHERE t.userTaskId = :userTaskId AND t.userId = :userId")
    void updateAccessInfo(@Param("userTaskId") Long userTaskId, @Param("userId") Long userId, @Param("accessTime") LocalDateTime accessTime);

    // Tag-based search (PostgreSQL array operations)
    @Query(value = "SELECT * FROM user_tasks WHERE user_id = :userId AND tags && CAST(:tags AS TEXT[])", nativeQuery = true)
    Page<UserTasks> findByUserIdAndTagsContaining(@Param("userId") Long userId, @Param("tags") String[] tags, Pageable pageable);

    // Completion rate calculation
    @Query("SELECT " +
           "CASE WHEN COUNT(t) = 0 THEN 0.0 " +
           "ELSE (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(t)) END " +
           "FROM UserTasks t WHERE t.userId = :userId AND t.status != 'CANCELLED'")
    Double calculateCompletionRate(@Param("userId") Long userId);

    // Average completion time in days
    @Query(value = "SELECT AVG(EXTRACT(DAY FROM (modified_date - created_date))) " +
           "FROM user_tasks WHERE user_id = :userId AND status = 'COMPLETED'",
           nativeQuery = true)
    Double calculateAverageCompletionTime(@Param("userId") Long userId);
}
