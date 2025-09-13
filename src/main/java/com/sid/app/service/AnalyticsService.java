package com.sid.app.service;

import com.sid.app.entity.Holiday;
import com.sid.app.entity.OfficeVisit;
import com.sid.app.entity.UserLeave;
import com.sid.app.enums.VisitType;
import com.sid.app.model.AggregatePeriodDTO;
import com.sid.app.repository.HolidayRepository;
import com.sid.app.repository.OfficeVisitRepository;
import com.sid.app.repository.UserLeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Analytics service: aggregates visits / leaves / holidays by period (month|year|week).
 *
 * Important: leave aggregation uses the same per-day distribution logic as balance calculation:
 *   perDay = (leave.days != null ? leave.days : span) / span
 * and portion for overlap = perDay * overlapSpan.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OfficeVisitRepository officeVisitRepo;
    private final UserLeaveRepository userLeaveRepo;
    private final HolidayRepository holidayRepo;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    // scale & rounding when dividing days
    private static final int DIVIDE_SCALE = 8;

    public List<AggregatePeriodDTO> aggregateByPeriod(Long userId, LocalDate from, LocalDate to, String groupBy) {
        log.debug("aggregateByPeriod() userId={} from={} to={} groupBy={}", userId, from, to, groupBy);

        // fetch fresh data from DB (reflects creates/updates/deletes)
        List<OfficeVisit> visits = (userId == null)
                ? officeVisitRepo.findByVisitDateBetween(from, to)
                : officeVisitRepo.findByUserIdAndVisitDateBetween(userId, from, to);

        List<UserLeave> leaves = (userId == null)
                ? userLeaveRepo.findByDateRangeOverlapAllUsers(from, to) // implement this if needed
                : userLeaveRepo.findOverlappingLeaves(userId, from, to);

        List<Holiday> holidays = holidayRepo.findByHolidayDateBetween(from, to);

        // build function mapping date -> period key
        Function<LocalDate, String> periodKeyFn = buildPeriodKeyFn(groupBy);

        // visits aggregation (counts by visit type per period)
        Map<String, Map<String, Long>> visitAgg = visits.stream()
                .collect(Collectors.groupingBy(
                        v -> periodKeyFn.apply(v.getVisitDate()),
                        Collectors.groupingBy(
                                v -> v.getVisitType() == null ? "OTHERS" : v.getVisitType().name(),
                                Collectors.counting()
                        )
                ));

        // leaves aggregation: compute fractional leave-days per period (BigDecimal)
        Map<String, BigDecimal> leaveAgg = leaves.stream()
                .flatMap(l -> {
                    // compute overlap between leave and requested [from..to]
                    LocalDate s = l.getStartDate().isBefore(from) ? from : l.getStartDate();
                    LocalDate e = l.getEndDate().isAfter(to) ? to : l.getEndDate();
                    long overlapSpan = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
                    if (overlapSpan <= 0) {
                        return LongStream.empty().mapToObj(i -> new AbstractMap.SimpleEntry<String, BigDecimal>("", BigDecimal.ZERO));
                    }

                    long span = java.time.temporal.ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
                    if (span <= 0) {
                        return LongStream.empty().mapToObj(i -> new AbstractMap.SimpleEntry<String, BigDecimal>("", BigDecimal.ZERO));
                    }

                    // totalDays: explicit fractional days OR full-day-per-day
                    BigDecimal totalDays = l.getDays() == null ? BigDecimal.valueOf(span) : l.getDays();

                    // per-day share
                    BigDecimal perDay = totalDays.divide(BigDecimal.valueOf(span), DIVIDE_SCALE, BigDecimal.ROUND_HALF_UP);

                    // We'll add perDay for each overlapped date and tag it with the period key.
                    // This preserves fractional days correctly (and allows a leave spanning months to distribute proportionally).
                    return LongStream.range(0, overlapSpan)
                            .mapToObj(i -> {
                                LocalDate d = s.plusDays(i);
                                String periodKey = periodKeyFn.apply(d);
                                return new AbstractMap.SimpleEntry<>(periodKey, perDay);
                            });
                })
                // filter out any empty keys (defensive)
                .filter(e -> e.getKey() != null && !e.getKey().isEmpty())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // holiday aggregation
        Map<String, Long> holidayAgg = holidays.stream()
                .collect(Collectors.groupingBy(
                        h -> periodKeyFn.apply(h.getHolidayDate()),
                        Collectors.counting()
                ));

        // build set of ordered periods
        List<String> orderedPeriods = buildOrderedPeriods(from, to, groupBy);

        // build DTOs (round leave BigDecimal to long - use Math.round to nearest)
        return orderedPeriods.stream().map(period -> {
            Map<String, Long> visitTypeMap = visitAgg.getOrDefault(period, Collections.emptyMap());
            long wfo = visitTypeMap.getOrDefault(VisitType.WFO.name(), 0L);
            long wfh = visitTypeMap.getOrDefault(VisitType.WFH.name(), 0L);
            long hybrid = visitTypeMap.getOrDefault(VisitType.HYBRID.name(), 0L);
            long others = visitTypeMap.getOrDefault(VisitType.OTHERS.name(), 0L);

            BigDecimal leaveBd = leaveAgg.getOrDefault(period, BigDecimal.ZERO);
            long leave = leaveBd == null ? 0L : Math.round(leaveBd.doubleValue());

            long holiday = holidayAgg.getOrDefault(period, 0L);

            return AggregatePeriodDTO.builder()
                    .period(period)
                    .wfo(wfo)
                    .wfh(wfh)
                    .hybrid(hybrid)
                    .others(others)
                    .leave(leave)
                    .holiday(holiday)
                    .build();
        }).collect(Collectors.toList());
    }

    private Function<LocalDate, String> buildPeriodKeyFn(String groupBy) {
        String g = groupBy.toLowerCase();
        return switch (g) {
            case "month" -> MONTH_FORMAT::format;
            case "year" -> YEAR_FORMAT::format;
            case "week" -> d -> {
                int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int weekYear = d.get(IsoFields.WEEK_BASED_YEAR);
                return String.format("%d-W%02d", weekYear, week);
            };
            default -> throw new IllegalArgumentException("Unsupported groupBy: " + groupBy);
        };
    }

    private List<String> buildOrderedPeriods(LocalDate from, LocalDate to, String groupBy) {
        List<String> periods = new ArrayList<>();
        switch (groupBy) {
            case "month": {
                LocalDate start = LocalDate.of(from.getYear(), from.getMonth(), 1);
                LocalDate end = LocalDate.of(to.getYear(), to.getMonth(), 1);
                while (!start.isAfter(end)) {
                    periods.add(MONTH_FORMAT.format(start));
                    start = start.plusMonths(1);
                }
                break;
            }
            case "year": {
                int startYear = from.getYear();
                int endYear = to.getYear();
                for (int y = startYear; y <= endYear; y++) periods.add(String.format("%04d", y));
                break;
            }
            case "week": {
                LocalDate cursor = from;
                while (!cursor.isAfter(to)) {
                    int week = cursor.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int weekYear = cursor.get(IsoFields.WEEK_BASED_YEAR);
                    String key = String.format("%d-W%02d", weekYear, week);
                    if (!periods.contains(key)) periods.add(key);
                    cursor = cursor.plusWeeks(1);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported groupBy: " + groupBy);
        }
        return periods;
    }
}
