package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.DailyViewRecordsDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.DailyViewRecordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstants.FETCH_DAILY_VIEW_ENDPOINT)
public class DailyViewRecordsController {

    private final DailyViewRecordsService service;

    @GetMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<List<DailyViewRecordsDTO>>> fetchDailyViewRecords(@RequestParam("userId") Long userId,
                                                                                        @RequestParam(value = "year", required = false) Integer year,
                                                                                        @RequestParam(value = "month", required = false) Integer month,
                                                                                        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                                        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                                                        @RequestParam(value = "showAll", required = false, defaultValue = "false") boolean showAll) {
        log.info("fetchDailyViewRecords() userId={} year={} month={} from={} to={} showAll={}",
                userId, year, month, from, to, showAll);

        try {
            List<DailyViewRecordsDTO> list = service.getDailyView(userId, year, month, from, to, showAll);
            if (list == null || list.isEmpty()) {
                log.info("fetchDailyViewRecords() - no data for userId={} from={} to={}", from, to, userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_DAILY_VIEW_FOUND, list));
            }
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_DAILY_VIEW_RETRIEVED, list));
        } catch (IllegalArgumentException ex) {
            log.warn("fetchDailyViewRecords() - validation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("fetchDailyViewRecords() - unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }
}
