package com.sid.app.repository;

import com.sid.app.entity.DailyTask;
import com.sid.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for daily tasks data access operations.
 *
 * <p>Author: Siddhant Patni</p>
 */
@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {

    /**
     * Finds all daily tasks by user.
     *
     * @param user the user whose tasks to retrieve
     * @return list of daily tasks for the specified user
     */
    List<DailyTask> findByUser(User user);

    /**
     * Finds all daily tasks by user and date.
     *
     * @param user the user whose tasks to retrieve
     * @param dailyTaskDate the date to filter tasks by
     * @return list of daily tasks for the specified user and date
     */
    List<DailyTask> findByUserAndDailyTaskDate(User user, LocalDate dailyTaskDate);

    /**
     * Finds all daily tasks by user within a date range.
     *
     * @param user the user whose tasks to retrieve
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of daily tasks for the specified user within the date range
     */
    List<DailyTask> findByUserAndDailyTaskDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Finds all daily tasks by project code.
     *
     * @param projectCode the project code to filter tasks by
     * @return list of daily tasks for the specified project code
     */
    List<DailyTask> findByProjectCode(String projectCode);

    /**
     * Finds all daily tasks by project name.
     *
     * @param projectName the project name to filter tasks by
     * @return list of daily tasks for the specified project name
     */
    List<DailyTask> findByProjectName(String projectName);

    /**
     * Finds all daily tasks by task number pattern (used for generating unique task numbers).
     *
     * @param pattern the task number pattern to search for
     * @return list of daily tasks matching the pattern
     */
    List<DailyTask> findByTaskNumberLike(String pattern);

    /**
     * Finds all daily tasks by story/task/bug number.
     *
     * @param storyTaskBugNumber the story/task/bug number to filter by
     * @return list of daily tasks for the specified story/task/bug number
     */
    List<DailyTask> findByStoryTaskBugNumber(String storyTaskBugNumber);

    /**
     * Finds all daily tasks by date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of daily tasks within the specified date range
     */
    List<DailyTask> findByDailyTaskDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Custom query to find daily tasks by user ID and date range with ordering.
     *
     * @param userId the user ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of daily tasks for the user within the date range, ordered by date
     */
    @Query("SELECT dt FROM DailyTask dt WHERE dt.user.userId = :userId AND dt.dailyTaskDate BETWEEN :startDate AND :endDate ORDER BY dt.dailyTaskDate DESC")
    List<DailyTask> findByUserIdAndDateRangeOrdered(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Custom query to find daily tasks by user ID ordered by date.
     *
     * @param userId the user ID
     * @return list of daily tasks for the user ordered by date (most recent first)
     */
    @Query("SELECT dt FROM DailyTask dt WHERE dt.user.userId = :userId ORDER BY dt.dailyTaskDate DESC, dt.createdDate DESC")
    List<DailyTask> findByUserIdOrderedByDate(@Param("userId") Long userId);

    /**
     * Count daily tasks for a user.
     *
     * @param user the user
     * @return count of daily tasks for the user
     */
    long countByUser(User user);

    /**
     * Count daily tasks for a user within a date range.
     *
     * @param user the user
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return count of daily tasks for the user within the date range
     */
    long countByUserAndDailyTaskDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
