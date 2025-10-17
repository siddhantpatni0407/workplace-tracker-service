package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.model.AggregatePeriodDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(EndpointConstants.ANALYTICS_VISITS_LEAVES_AGG_ENDPOINT)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * GET /analytics/visits-leaves-aggregate
     * <p>
     * Params:
     * - userId (optional)
     * - from (yyyy-MM-dd) required
     * - to   (yyyy-MM-dd) required
     * - groupBy = month | year | week
     */
    @GetMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<List<AggregatePeriodDTO>>> getVisitsLeavesAggregate(@RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                                          @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                                                          @RequestParam(value = "groupBy") String groupBy) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();

        log.info("getVisitsLeavesAggregate() userId={} from={} to={} groupBy={}", userId, from, to, groupBy);

        if (from == null || to == null || groupBy == null || groupBy.trim().isEmpty()) {
            log.warn("getVisitsLeavesAggregate() invalid params");
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_ANALYTICS_PARAMS, null));
        }

        String normalized = groupBy.trim().toLowerCase(Locale.ROOT);
        if (!("month".equals(normalized) || "year".equals(normalized) || "week".equals(normalized))) {
            log.warn("getVisitsLeavesAggregate() invalid groupBy={}", groupBy);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_ANALYTICS_PARAMS, null));
        }

        try {
            List<AggregatePeriodDTO> data = analyticsService.aggregateByPeriod(userId, from, to, normalized);
            if (data == null || data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.SUCCESS_ANALYTICS_RETRIEVED, data));
            }
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_ANALYTICS_RETRIEVED, data));
        } catch (IllegalArgumentException ex) {
            log.warn("getVisitsLeavesAggregate() bad request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("getVisitsLeavesAggregate() error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Internal server error", null));
        }
    }
}
