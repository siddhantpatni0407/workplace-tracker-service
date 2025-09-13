package com.sid.app.service.validation;

import com.sid.app.config.AppProperties;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.DateRange;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Validates and resolves daily-view request params into a concrete date range.
 */
@Component
@Slf4j
public class DailyViewRequestValidator {

    private final AppProperties appProperties;

    public DailyViewRequestValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Validate params and return resolved DateRange.
     * Throws IllegalArgumentException with messages from AppConstants if invalid.
     */
    public DateRange validateAndResolve(Long userId,
                                        Integer year,
                                        Integer month,
                                        LocalDate from,
                                        LocalDate to) {

        if (Optional.ofNullable(userId).orElse(0L) <= 0) {
            log.warn("DailyViewRequestValidator: invalid userId={}", userId);
            throw new IllegalArgumentException(AppConstants.ERROR_INVALID_DAILY_VIEW_PARAMS);
        }

        // If neither range nor year/month provided, default to current month
        if ((from == null || to == null) && (year == null || month == null)) {
            YearMonth ym = YearMonth.now();
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        } else if (from == null || to == null) {
            // year/month provided - validate month
            if (month == null || month < 1 || month > 12) {
                log.warn("DailyViewRequestValidator: invalid month={}", month);
                throw new IllegalArgumentException(AppConstants.ERROR_INVALID_DAILY_VIEW_PARAMS);
            }
            YearMonth ym;
            try {
                ym = YearMonth.of(year, month);
            } catch (Exception ex) {
                log.warn("DailyViewRequestValidator: invalid year/month {}-{} ex={}", year, month, ex.getMessage());
                throw new IllegalArgumentException(AppConstants.ERROR_INVALID_DAILY_VIEW_PARAMS);
            }
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        }

        // sanity: from <= to
        if (from.isAfter(to)) {
            log.warn("DailyViewRequestValidator: from > to ({} > {})", from, to);
            throw new IllegalArgumentException(AppConstants.ERROR_INVALID_DAILY_VIEW_PARAMS);
        }

        long daysRange = ChronoUnit.DAYS.between(from, to) + 1;
        if (daysRange <= 0) {
            log.warn("DailyViewRequestValidator: invalid computed range days={}", daysRange);
            throw new IllegalArgumentException(AppConstants.ERROR_INVALID_DAILY_VIEW_PARAMS);
        }
        if (daysRange > appProperties.getDailyViewMaxRangeDays()) {
            log.warn("DailyViewRequestValidator: range too large days={} max={}", daysRange, appProperties.getDailyViewMaxRangeDays());
            throw new IllegalArgumentException(AppConstants.ERROR_DATE_RANGE_TOO_LARGE);
        }

        return new DateRange(from, to);
    }
}
