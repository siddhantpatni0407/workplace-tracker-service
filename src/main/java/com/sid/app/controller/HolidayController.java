package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.HolidayDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(EndpointConstants.HOLIDAYS_ENDPOINT)
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @RequiredRole({UserRole.USER, UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.MANAGER})
    public ResponseEntity<ResponseDTO<List<HolidayDTO>>> getHolidays(@RequestParam(value = "from", required = false) String from,
                                                                     @RequestParam(value = "to", required = false) String to) {

        log.info("getHolidays() called with from='{}' to='{}'", from, to);

        if (Optional.ofNullable(from).orElse("").trim().isEmpty() || Optional.ofNullable(to).orElse("").trim().isEmpty()) {
            List<HolidayDTO> all = holidayService.getAllHolidays();
            if (all.isEmpty()) {
                log.info("getHolidays() - no holidays found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_HOLIDAYS_FOUND, all));
            }
            log.info("getHolidays() - returning {} holidays", all.size());
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_HOLIDAYS_RETRIEVED, all));
        }

        try {
            LocalDate f = LocalDate.parse(from.trim());
            LocalDate t = LocalDate.parse(to.trim());

            if (f.isAfter(t)) {
                log.warn("getHolidays() - invalid range: from > to ({} > {})", f, t);
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_DATE_RANGE, null));
            }

            List<HolidayDTO> list = holidayService.getHolidaysBetween(f, t);

            if (list.isEmpty()) {
                log.info("getHolidays() - no holidays found in range {} to {}", f, t);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_HOLIDAYS_IN_RANGE, list));
            }

            log.info("getHolidays() - returning {} holidays for range {} to {}", list.size(), f, t);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_HOLIDAYS_RETRIEVED, list));
        } catch (DateTimeParseException ex) {
            log.warn("getHolidays() - date parse error from='{}' to='{}' error={}", from, to, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_DATE_RANGE, null));
        }
    }

    @PostMapping
    @RequiredRole({UserRole.ADMIN})
    public ResponseEntity<ResponseDTO<HolidayDTO>> createHoliday(@Valid @RequestBody HolidayDTO req) {
        log.info("createHoliday() name='{}' date='{}'", req.getName(), req.getHolidayDate());
        HolidayDTO created = holidayService.createHoliday(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_HOLIDAY_CREATED, created));
    }

    /**
     * Update existing holiday.
     * Accepts: PUT /holidays?holidayId={id}  OR  (if you prefer path param, you can change to @PutMapping("/{holidayId}"))
     */
    @PutMapping
    @RequiredRole({UserRole.ADMIN})
    public ResponseEntity<ResponseDTO<HolidayDTO>> updateHoliday(@RequestParam("holidayId") Long holidayId,
                                                                 @Valid @RequestBody HolidayDTO req) {
        log.info("updateHoliday() holidayId={} name={} date={}", holidayId, req.getName(), req.getHolidayDate());
        try {
            HolidayDTO updated = holidayService.updateHoliday(holidayId, req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_HOLIDAY_UPDATED, updated));
        } catch (IllegalArgumentException ex) {
            log.warn("updateHoliday() - bad request holidayId={} error={}", holidayId, ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.warn("updateHoliday() - not found or error holidayId={} error={}", holidayId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }

    @DeleteMapping
    @RequiredRole({UserRole.ADMIN})
    public ResponseEntity<ResponseDTO<Void>> deleteHoliday(@RequestParam("holidayId") Long holidayId) {
        log.info("deleteHoliday() holidayId={}", holidayId);
        try {
            holidayService.deleteHoliday(holidayId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_HOLIDAY_DELETED, null));
        } catch (Exception ex) {
            log.warn("deleteHoliday() failed holidayId={} error={}", holidayId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }
}
