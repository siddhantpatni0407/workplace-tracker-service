package com.sid.app.repository;

import com.sid.app.entity.UserLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserLeaveBalanceRepository extends JpaRepository<UserLeaveBalance, Long> {
    Optional<UserLeaveBalance> findByUserIdAndPolicyIdAndYear(Long userId, Long policyId, Integer year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserLeaveBalance u WHERE u.userId = :userId AND u.policyId = :policyId AND u.year = :year")
    Optional<UserLeaveBalance> findByUserIdAndPolicyIdAndYearForUpdate(@Param("userId") Long userId,
                                                                       @Param("policyId") Long policyId,
                                                                       @Param("year") Integer year);
}
