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
     * Finds the latest task numbers for a specific user.
     * Used for task number auto-generation.
     *
     * @param userId the user ID
     * @return the latest task numbers, ordered by task ID descending
     */
    @Query("SELECT dt.taskNumber FROM DailyTask dt WHERE dt.user.userId = :userId ORDER BY dt.dailyTaskId DESC")
    List<String> findLatestTaskNumbersByUserId(@Param("userId") Long userId);
}
