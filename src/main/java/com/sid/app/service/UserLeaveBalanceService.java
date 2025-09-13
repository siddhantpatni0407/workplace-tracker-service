package com.sid.app.service;

import com.sid.app.entity.LeavePolicy;
import com.sid.app.entity.UserLeave;
import com.sid.app.entity.UserLeaveBalance;
import com.sid.app.model.UserLeaveBalanceDTO;
import com.sid.app.repository.UserLeaveBalanceRepository;
import com.sid.app.repository.LeavePolicyRepository;
import com.sid.app.repository.UserLeaveRepository;
import com.sid.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for user leave balance operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLeaveBalanceService {

    private final UserLeaveBalanceRepository balanceRepo;
    private final LeavePolicyRepository policyRepo;
    private final UserLeaveRepository userLeaveRepo;
    private final UserRepository userRepo; // used to validate existence

    private static final int DIVIDE_SCALE = 8;
    private static final RoundingMode DIVIDE_ROUNDING = RoundingMode.HALF_UP;

    public UserLeaveBalanceDTO getBalance(Long userId, Long policyId, Integer year) {
        log.debug("getBalance() userId={} policyId={} year={}", userId, policyId, year);
        validateIdsExist(userId, policyId);
        if (year == null) throw new IllegalArgumentException("year is required");

        return balanceRepo.findByUserIdAndPolicyIdAndYear(userId, policyId, year)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Balance not found for user/policy/year"));
    }

    /**
     * Admin upsert. Use for manual overrides only.
     */
    @Transactional
    public UserLeaveBalanceDTO upsertBalance(UserLeaveBalanceDTO dto) {
        log.info("upsertBalance() (ADMIN) userId={} policyId={} year={}", dto.getUserId(), dto.getPolicyId(), dto.getYear());
        validateIdsExist(dto.getUserId(), dto.getPolicyId());
        if (dto.getYear() == null) throw new IllegalArgumentException("year is required");

        return balanceRepo.findByUserIdAndPolicyIdAndYear(dto.getUserId(), dto.getPolicyId(), dto.getYear())
                .map(existing -> {
                    existing.setAllocatedDays(dto.getAllocatedDays() == null ? BigDecimal.ZERO : dto.getAllocatedDays());
                    existing.setUsedDays(dto.getUsedDays() == null ? BigDecimal.ZERO : dto.getUsedDays());
                    existing.setRemainingDays(dto.getRemainingDays() == null ? BigDecimal.ZERO : dto.getRemainingDays());
                    balanceRepo.save(existing);
                    return toDto(existing);
                })
                .orElseGet(() -> {
                    Integer defaultDays = policyRepo.findById(dto.getPolicyId())
                            .map(LeavePolicy::getDefaultAnnualDays)
                            .orElse(0);
                    BigDecimal allocated = dto.getAllocatedDays() == null ? BigDecimal.valueOf(defaultDays) : dto.getAllocatedDays();
                    BigDecimal used = dto.getUsedDays() == null ? BigDecimal.ZERO : dto.getUsedDays();
                    BigDecimal remaining = dto.getRemainingDays() == null ? allocated.subtract(used) : dto.getRemainingDays();
                    UserLeaveBalance ulb = UserLeaveBalance.builder()
                            .userId(dto.getUserId())
                            .policyId(dto.getPolicyId())
                            .year(dto.getYear())
                            .allocatedDays(allocated)
                            .usedDays(used)
                            .remainingDays(remaining)
                            .build();
                    balanceRepo.save(ulb);
                    return toDto(ulb);
                });
    }

    /**
     * Adjust usedDays by deltaDays (positive to increase used, negative to decrease).
     * This method locks the balance row (pessimistic) to avoid concurrent races.
     */
    @Transactional
    public UserLeaveBalanceDTO adjustBalance(Long userId, Long policyId, Integer year, BigDecimal deltaDays) {
        log.info("adjustBalance() userId={} policyId={} year={} delta={}", userId, policyId, year, deltaDays);
        validateIdsExist(userId, policyId);
        if (year == null || deltaDays == null) {
            throw new IllegalArgumentException("year and deltaDays are required");
        }
        if (BigDecimal.ZERO.compareTo(deltaDays) == 0) {
            return getOrCreateBalanceDto(userId, policyId, year);
        }

        // find row with pessimistic lock (repository must implement this)
        Optional<UserLeaveBalance> opt = balanceRepo.findByUserIdAndPolicyIdAndYearForUpdate(userId, policyId, year);

        UserLeaveBalance balance = opt.orElseGet(() -> {
            Integer defaultDays = policyRepo.findById(policyId).map(LeavePolicy::getDefaultAnnualDays).orElse(0);
            BigDecimal allocated = BigDecimal.valueOf(defaultDays);
            UserLeaveBalance b = UserLeaveBalance.builder()
                    .userId(userId)
                    .policyId(policyId)
                    .year(year)
                    .allocatedDays(allocated)
                    .usedDays(BigDecimal.ZERO)
                    .remainingDays(allocated)
                    .build();
            balanceRepo.save(b);
            // fetch again with lock
            return balanceRepo.findByUserIdAndPolicyIdAndYearForUpdate(userId, policyId, year)
                    .orElseThrow(() -> new IllegalStateException("Failed to create balance row"));
        });

        BigDecimal newUsed = balance.getUsedDays().add(deltaDays);
        if (newUsed.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Used days cannot become negative");
        }

        BigDecimal newRemaining = balance.getAllocatedDays().subtract(newUsed);
        if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
            // Prevent overdraft by default. Change if your org allows negative balances.
            throw new IllegalArgumentException("Insufficient remaining days");
        }

        balance.setUsedDays(newUsed);
        balance.setRemainingDays(newRemaining);
        balanceRepo.save(balance);
        return toDto(balance);
    }

    /**
     * Recalculate the balance for a given user/policy/year by summing leaves overlapping that year.
     */
    @Transactional
    public UserLeaveBalanceDTO recalculateBalanceFromLeaves(Long userId, Long policyId, Integer year) {
        log.info("recalculateBalanceFromLeaves() userId={} policyId={} year={}", userId, policyId, year);
        validateIdsExist(userId, policyId);
        if (year == null) throw new IllegalArgumentException("year is required");

        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<UserLeave> leaves = userLeaveRepo.findOverlappingLeaves(userId, yearStart, yearEnd);

        BigDecimal usedSum = leaves.stream()
                .map(l -> {
                    LocalDate s = l.getStartDate().isBefore(yearStart) ? yearStart : l.getStartDate();
                    LocalDate e = l.getEndDate().isAfter(yearEnd) ? yearEnd : l.getEndDate();

                    long span = ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
                    long overlapSpan = ChronoUnit.DAYS.between(s, e) + 1;

                    if (span <= 0 || overlapSpan <= 0) {
                        return BigDecimal.ZERO;
                    }

                    BigDecimal totalDays = l.getDays() == null ? BigDecimal.valueOf(span) : l.getDays();
                    BigDecimal perDay = totalDays.divide(BigDecimal.valueOf(span), DIVIDE_SCALE, DIVIDE_ROUNDING);
                    return perDay.multiply(BigDecimal.valueOf(overlapSpan));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer defaultDays = policyRepo.findById(policyId)
                .map(LeavePolicy::getDefaultAnnualDays)
                .orElse(0);
        BigDecimal allocated = BigDecimal.valueOf(defaultDays);

        final BigDecimal remainingFinal = allocated.subtract(usedSum).compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : allocated.subtract(usedSum);

        UserLeaveBalance ulb = balanceRepo.findByUserIdAndPolicyIdAndYear(userId, policyId, year)
                .map(existing -> {
                    existing.setAllocatedDays(allocated);
                    existing.setUsedDays(usedSum);
                    existing.setRemainingDays(remainingFinal);
                    return balanceRepo.save(existing);
                })
                .orElseGet(() -> {
                    UserLeaveBalance b = UserLeaveBalance.builder()
                            .userId(userId)
                            .policyId(policyId)
                            .year(year)
                            .allocatedDays(allocated)
                            .usedDays(usedSum)
                            .remainingDays(remainingFinal)
                            .build();
                    return balanceRepo.save(b);
                });

        return toDto(ulb);
    }

    /**
     * Return DTO for existing or default balance (read-only).
     */
    @Transactional(readOnly = true)
    protected UserLeaveBalanceDTO getOrCreateBalanceDto(Long userId, Long policyId, Integer year) {
        validateIdsExist(userId, policyId);
        return balanceRepo.findByUserIdAndPolicyIdAndYear(userId, policyId, year)
                .map(this::toDto)
                .orElseGet(() -> {
                    Integer defaultDays = policyRepo.findById(policyId).map(LeavePolicy::getDefaultAnnualDays).orElse(0);
                    BigDecimal allocated = BigDecimal.valueOf(defaultDays);
                    return UserLeaveBalanceDTO.builder()
                            .userId(userId)
                            .policyId(policyId)
                            .year(year)
                            .allocatedDays(allocated)
                            .usedDays(BigDecimal.ZERO)
                            .remainingDays(allocated)
                            .build();
                });
    }

    private void validateIdsExist(Long userId, Long policyId) {
        if (userId == null || policyId == null) {
            throw new IllegalArgumentException("userId and policyId are required");
        }
        if (!userRepo.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        if (!policyRepo.existsById(policyId)) {
            throw new EntityNotFoundException("Policy not found with id: " + policyId);
        }
    }

    private UserLeaveBalanceDTO toDto(UserLeaveBalance ulb) {
        return UserLeaveBalanceDTO.builder()
                .userLeaveBalanceId(ulb.getUserLeaveBalanceId())
                .userId(ulb.getUserId())
                .policyId(ulb.getPolicyId())
                .year(ulb.getYear())
                .allocatedDays(ulb.getAllocatedDays())
                .usedDays(ulb.getUsedDays())
                .remainingDays(ulb.getRemainingDays())
                .build();
    }
}
