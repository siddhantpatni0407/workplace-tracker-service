package com.sid.app.repository;

import com.sid.app.entity.UserLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserLeaveRepository extends JpaRepository<UserLeave, Long> {

    List<UserLeave> findByUserIdAndStartDateBetween(Long userId, LocalDate from, LocalDate to);

    List<UserLeave> findByUserId(Long userId);

    List<UserLeave> findByPolicyIdAndStartDateBetween(Long policyId, LocalDate from, LocalDate to);

    // add org-wide overlap if you need aggregate across all users
    @Query("SELECT u FROM UserLeave u WHERE NOT (u.endDate < :from OR u.startDate > :to)")
    List<UserLeave> findByDateRangeOverlapAllUsers(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT u FROM UserLeave u WHERE u.userId = :userId AND NOT (u.endDate < :from OR u.startDate > :to)")
    List<UserLeave> findOverlappingLeaves(@Param("userId") Long userId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to);

}
