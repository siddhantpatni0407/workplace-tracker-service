package com.sid.app.service;

import com.sid.app.entity.Holiday;
import com.sid.app.entity.LeavePolicy;
import com.sid.app.entity.UserLeave;
import com.sid.app.entity.OfficeVisit;
import com.sid.app.model.DailyViewRecordsDTO;
import com.sid.app.enums.DayLabel;
import com.sid.app.model.DateRange;
import com.sid.app.repository.HolidayRepository;
import com.sid.app.repository.UserLeaveRepository;
import com.sid.app.repository.OfficeVisitRepository;
import com.sid.app.repository.LeavePolicyRepository;
import com.sid.app.service.validation.DailyViewRequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyViewRecordsService {

    private final HolidayRepository holidayRepo;
    private final UserLeaveRepository userLeaveRepo;
    private final OfficeVisitRepository officeVisitRepo;
    private final LeavePolicyRepository leavePolicyRepo;
    private final DailyViewRequestValidator validator;

    /**
     * Public method accepts the raw request parameters (userId, year/month or from/to).
     * Validation & date-range resolution is delegated to DailyViewRequestValidator.
     */
    public List<DailyViewRecordsDTO> getDailyView(Long userId,
                                                  Integer year,
                                                  Integer month,
                                                  LocalDate from,
                                                  LocalDate to,
                                                  boolean showAll) {

        // validate and resolve dates
        DateRange range = validator.validateAndResolve(userId, year, month, from, to);
        LocalDate resolvedFrom = range.getFrom();
        LocalDate resolvedTo = range.getTo();

        log.debug("getDailyView() userId={} resolvedFrom={} resolvedTo={} showAll={}", userId, resolvedFrom, resolvedTo, showAll);

        // fetch DB data
        List<Holiday> holidays = holidayRepo.findByHolidayDateBetween(resolvedFrom, resolvedTo);
        List<UserLeave> leaves = userLeaveRepo.findOverlappingLeaves(userId, resolvedFrom, resolvedTo);
        List<OfficeVisit> visits = officeVisitRepo.findByUserIdAndVisitDateBetweenOrderByVisitDate(userId, resolvedFrom, resolvedTo);

        // build policyId -> policyCode map in one call
        Set<Long> policyIds = leaves.stream()
                .map(UserLeave::getPolicyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> policyMap = policyIds.isEmpty()
                ? Collections.emptyMap()
                : leavePolicyRepo.findAllById(policyIds).stream()
                .collect(Collectors.toMap(LeavePolicy::getPolicyId, LeavePolicy::getPolicyCode));

        // date skeleton
        long days = ChronoUnit.DAYS.between(resolvedFrom, resolvedTo) + 1;
        Map<LocalDate, DailyViewRecordsDTO> map = new LinkedHashMap<>((int) Math.min(days, 1024));
        for (int i = 0; i < days; i++) {
            LocalDate d = resolvedFrom.plusDays(i);
            map.put(d, DailyViewRecordsDTO.builder()
                    .date(d)
                    .dayOfWeek(d.getDayOfWeek().getValue())
                    .label(DayLabel.NONE)
                    .build());
        }

        // apply holidays (highest precedence)
        holidays.forEach(h -> {
            DailyViewRecordsDTO dto = map.get(h.getHolidayDate());
            if (dto != null) {
                dto.setHolidayName(h.getName());
                dto.setHolidayType(String.valueOf(h.getHolidayType()));
                dto.setLabel(DayLabel.HOLIDAY);
            }
        });

        // apply leaves
        leaves.forEach(l -> {
            LocalDate start = l.getStartDate().isBefore(resolvedFrom) ? resolvedFrom : l.getStartDate();
            LocalDate end = l.getEndDate().isAfter(resolvedTo) ? resolvedTo : l.getEndDate();
            long span = ChronoUnit.DAYS.between(start, end) + 1;
            for (int i = 0; i < span; i++) {
                LocalDate d = start.plusDays(i);
                DailyViewRecordsDTO dto = map.get(d);
                if (dto == null) continue;
                dto.setLeavePolicyCode(policyMap.get(l.getPolicyId()));
                dto.setLeaveDays(l.getDays() == null ? null : l.getDays().toPlainString());
                dto.setLeaveDayPart(l.getDayPart() == null ? null : l.getDayPart().name());
                dto.setLeaveNotes(l.getNotes());
                if (dto.getLabel() == DayLabel.NONE) dto.setLabel(DayLabel.LEAVE);
            }
        });

        // apply visits
        visits.forEach(v -> {
            DailyViewRecordsDTO dto = map.get(v.getVisitDate());
            if (dto == null) return;
            dto.setVisitType(v.getVisitType() == null ? null : v.getVisitType().name());
            dto.setVisitNotes(v.getNotes());
            if (dto.getLabel() == DayLabel.NONE) dto.setLabel(DayLabel.VISIT);
        });

        return new ArrayList<>(map.values());
    }
}
