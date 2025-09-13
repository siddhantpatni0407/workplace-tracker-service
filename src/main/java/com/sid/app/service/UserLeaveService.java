package com.sid.app.service;

import com.sid.app.entity.UserLeave;
import com.sid.app.entity.LeavePolicy;
import com.sid.app.enums.DayPart;
import com.sid.app.model.UserLeaveDTO;
import com.sid.app.repository.UserLeaveRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.LeavePolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that manages UserLeave rows and keeps user leave balances in sync.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLeaveService {

    private final UserLeaveRepository userLeaveRepo;
    private final UserRepository userRepo;
    private final LeavePolicyRepository leavePolicyRepo;
    private final UserLeaveBalanceService userLeaveBalanceService;

    public List<UserLeaveDTO> getLeavesForUser(Long userId) {
        log.debug("getLeavesForUser() userId={}", userId);
        return userLeaveRepo.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Create a leave and adjust balances for all affected years.
     * Transactional: if balance adjustment fails, leave creation rolls back.
     */
    @Transactional
    public UserLeaveDTO createLeave(UserLeaveDTO dto) {
        log.info("createLeave() userId={} policyId={} startDate={} endDate={} dayPart={} days={}",
                dto.getUserId(), dto.getPolicyId(), dto.getStartDate(), dto.getEndDate(), dto.getDayPart(), dto.getDays());

        validateBasicCreateRequest(dto);

        // compute effective days and dayPart
        DayPart dayPart = parseDayPart(dto.getDayPart());
        BigDecimal totalDays = computeTotalDays(dto.getStartDate(), dto.getEndDate(), dto.getDays(), dayPart);

        // verify user and policy exist
        if (!userRepo.existsById(dto.getUserId())) {
            throw new EntityNotFoundException("User not found with id: " + dto.getUserId());
        }
        if (!leavePolicyRepo.existsById(dto.getPolicyId())) {
            throw new EntityNotFoundException("Leave policy not found with id: " + dto.getPolicyId());
        }

        // persist leave
        UserLeave ul = UserLeave.builder()
                .userId(dto.getUserId())
                .policyId(dto.getPolicyId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .days(totalDays)
                .dayPart(dayPart)
                .notes(dto.getNotes())
                .build();

        userLeaveRepo.save(ul);

        // distribute days across years and adjust balances
        Map<Integer, BigDecimal> perYear = distributeDaysAcrossYears(ul.getStartDate(), ul.getEndDate(), totalDays);
        perYear.forEach((year, daysForYear) ->
                userLeaveBalanceService.adjustBalance(ul.getUserId(), ul.getPolicyId(), year, daysForYear)
        );

        return toDto(ul);
    }

    /**
     * Update a leave. Computes per-year deltas and applies adjustments.
     * If a leave's policy changes, adjustments are applied to both old and new policy rows.
     */
    @Transactional
    public UserLeaveDTO updateLeave(Long userLeaveId, UserLeaveDTO dto) {
        log.info("updateLeave() userLeaveId={} userId={} policyId={}", userLeaveId, dto.getUserId(), dto.getPolicyId());
        UserLeave existing = userLeaveRepo.findById(userLeaveId)
                .orElseThrow(() -> new EntityNotFoundException("UserLeave not found id: " + userLeaveId));

        // Keep copies of old values to compute deltas
        Long oldPolicyId = existing.getPolicyId();
        LocalDate oldStart = existing.getStartDate();
        LocalDate oldEnd = existing.getEndDate();
        BigDecimal oldDays = existing.getDays() == null ? BigDecimal.ZERO : existing.getDays();
        DayPart oldDayPart = existing.getDayPart();

        // Apply updates to the entity
        if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) existing.setEndDate(dto.getEndDate());
        if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
        if (dto.getDayPart() != null) existing.setDayPart(parseDayPart(dto.getDayPart()));
        if (dto.getDays() != null) existing.setDays(dto.getDays());
        if (dto.getPolicyId() != null) existing.setPolicyId(dto.getPolicyId());

        // Validate updated entity
        if (existing.getStartDate() == null || existing.getEndDate() == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (existing.getEndDate().isBefore(existing.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }

        // Recompute totalDays for updated entity
        BigDecimal newTotalDays = computeTotalDays(existing.getStartDate(), existing.getEndDate(), existing.getDays(), existing.getDayPart());

        // Save updated leave
        existing.setDays(newTotalDays);
        userLeaveRepo.save(existing);

        // Compute per-year maps for old and new
        Map<Integer, BigDecimal> oldMap = distributeDaysAcrossYears(oldStart, oldEnd, oldDays);
        Map<Integer, BigDecimal> newMap = distributeDaysAcrossYears(existing.getStartDate(), existing.getEndDate(), newTotalDays);

        // Compute deltas per year and apply adjustments
        // Case A: policy unchanged -> delta applies to same policy
        // Case B: policy changed -> subtract old from oldPolicy, add new to newPolicy
        if (Objects.equals(oldPolicyId, existing.getPolicyId())) {
            // same policy -> for each year in union of keys, apply new-old
            Set<Integer> years = new HashSet<>();
            years.addAll(oldMap.keySet());
            years.addAll(newMap.keySet());
            for (Integer year : years) {
                BigDecimal oldVal = oldMap.getOrDefault(year, BigDecimal.ZERO);
                BigDecimal newVal = newMap.getOrDefault(year, BigDecimal.ZERO);
                BigDecimal delta = newVal.subtract(oldVal);
                if (delta.compareTo(BigDecimal.ZERO) != 0) {
                    userLeaveBalanceService.adjustBalance(existing.getUserId(), existing.getPolicyId(), year, delta);
                }
            }
        } else {
            // policy changed: remove old allocations
            for (Map.Entry<Integer, BigDecimal> e : oldMap.entrySet()) {
                Integer year = e.getKey();
                BigDecimal val = e.getValue();
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    userLeaveBalanceService.adjustBalance(existing.getUserId(), oldPolicyId, year, val.negate());
                }
            }
            // add new allocations
            for (Map.Entry<Integer, BigDecimal> e : newMap.entrySet()) {
                Integer year = e.getKey();
                BigDecimal val = e.getValue();
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    userLeaveBalanceService.adjustBalance(existing.getUserId(), existing.getPolicyId(), year, val);
                }
            }
        }

        return toDto(existing);
    }

    /**
     * Delete leave and subtract its days from user leave balances.
     */
    @Transactional
    public void deleteLeave(Long userLeaveId) {
        log.info("deleteLeave() userLeaveId={}", userLeaveId);
        UserLeave ul = userLeaveRepo.findById(userLeaveId)
                .orElseThrow(() -> new EntityNotFoundException("UserLeave not found id: " + userLeaveId));

        // compute per-year distribution from the stored entity
        BigDecimal totalDays = ul.getDays() == null ? computeTotalDays(ul.getStartDate(), ul.getEndDate(), null, ul.getDayPart()) : ul.getDays();
        Map<Integer, BigDecimal> perYear = distributeDaysAcrossYears(ul.getStartDate(), ul.getEndDate(), totalDays);

        // subtract days (negate)
        perYear.forEach((year, daysForYear) ->
                userLeaveBalanceService.adjustBalance(ul.getUserId(), ul.getPolicyId(), year, daysForYear.negate())
        );

        userLeaveRepo.deleteById(userLeaveId);
    }

    // -------- Helpers --------

    private void validateBasicCreateRequest(UserLeaveDTO dto) {
        if (dto.getUserId() == null || dto.getPolicyId() == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("userId, policyId, startDate and endDate are required");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
    }

    private DayPart parseDayPart(String dayPartStr) {
        if (dayPartStr == null) return null;
        try {
            return DayPart.valueOf(dayPartStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid dayPart: " + dayPartStr);
        }
    }

    /**
     * Compute effective total days for a leave record.
     * - If explicit days provided -> use it.
     * - If dayPart MORNING/AFTERNOON -> 0.5
     * - Otherwise -> span in days (inclusive)
     */
    private BigDecimal computeTotalDays(LocalDate start, LocalDate end, BigDecimal providedDays, DayPart dayPart) {
        if (providedDays != null) {
            if (providedDays.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("days must be > 0");
            }
            return providedDays;
        }
        if (dayPart == DayPart.MORNING || dayPart == DayPart.AFTERNOON) {
            return BigDecimal.valueOf(0.5);
        }
        long span = ChronoUnit.DAYS.between(start, end) + 1;
        if (span <= 0) throw new IllegalArgumentException("Invalid date range");
        return BigDecimal.valueOf(span);
    }

    /**
     * Distribute totalDays across calendar days between start..end (inclusive) and return a map year -> sumOfDays.
     * per-day = totalDays / span
     */
    private Map<Integer, BigDecimal> distributeDaysAcrossYears(LocalDate start, LocalDate end, BigDecimal totalDays) {
        long span = ChronoUnit.DAYS.between(start, end) + 1;
        if (span <= 0) return Collections.emptyMap();

        BigDecimal perDay = totalDays.divide(BigDecimal.valueOf(span), 8, BigDecimal.ROUND_HALF_UP);
        Map<Integer, BigDecimal> map = new LinkedHashMap<>();

        for (long i = 0; i < span; i++) {
            LocalDate d = start.plusDays(i);
            int y = d.getYear();
            map.merge(y, perDay, BigDecimal::add);
        }

        // normalize scale to avoid tiny rounding residues
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().setScale(6, BigDecimal.ROUND_HALF_UP),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private UserLeaveDTO toDto(UserLeave ul) {
        return UserLeaveDTO.builder()
                .userLeaveId(ul.getUserLeaveId())
                .userId(ul.getUserId())
                .policyId(ul.getPolicyId())
                .startDate(ul.getStartDate())
                .endDate(ul.getEndDate())
                .days(ul.getDays())
                .dayPart(ul.getDayPart() == null ? null : ul.getDayPart().name())
                .notes(ul.getNotes())
                .build();
    }
}
