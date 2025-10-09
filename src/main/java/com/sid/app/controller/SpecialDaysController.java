package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.CurrentMonthSpecialDaysDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.SpecialDaysDataDTO;
import com.sid.app.service.SpecialDaysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Special Days API endpoints with role-based authorization (birthdays and work anniversaries)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpecialDaysController {

    private final SpecialDaysService specialDaysService;

    /**
     * Get special days with filtering and pagination.
     * All authenticated users can view special days.
     */
    @GetMapping(value = AppConstants.SPECIAL_DAYS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<SpecialDaysDataDTO>> getSpecialDays(@RequestParam(required = false) Integer month,
                                                                          @RequestParam(required = false) Integer year,
                                                                          @RequestParam(required = false) Integer page,
                                                                          @RequestParam(required = false) Integer limit,
                                                                          @RequestParam(required = false) String type,
                                                                          @RequestParam(required = false) String department,
                                                                          @RequestParam(required = false) String location) {

        log.info("GET {} - month: {}, year: {}, page: {}, limit: {}, type: {}, department: {}, location: {}",
                AppConstants.SPECIAL_DAYS_ENDPOINT, month, year, page, limit, type, department, location);

        ResponseDTO<SpecialDaysDataDTO> response = specialDaysService.getSpecialDays(
                month, year, page, limit, type, department, location);

        return ResponseEntity.ok(response);
    }

    /**
     * Get current month special days for dashboard.
     * All authenticated users can view special days.
     */
    @GetMapping(value = AppConstants.SPECIAL_DAYS_CURRENT_MONTH_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<CurrentMonthSpecialDaysDTO>> getCurrentMonthSpecialDays(@RequestParam(required = false) Integer month,
                                                                                              @RequestParam(required = false) Integer year,
                                                                                              @RequestParam(required = false) Integer limit) {

        log.info("GET {} - month: {}, year: {}, limit: {}", AppConstants.SPECIAL_DAYS_CURRENT_MONTH_ENDPOINT, month, year, limit);

        ResponseDTO<CurrentMonthSpecialDaysDTO> response = specialDaysService.getCurrentMonthSpecialDays(month, year, limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Get birthdays with filtering.
     * All authenticated users can view birthdays.
     */
    @GetMapping(value = AppConstants.SPECIAL_DAYS_BIRTHDAYS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<SpecialDaysDataDTO>> getBirthdays(@RequestParam(required = false) Integer month,
                                                                        @RequestParam(required = false) Integer year,
                                                                        @RequestParam(required = false) Integer page,
                                                                        @RequestParam(required = false) Integer limit,
                                                                        @RequestParam(required = false) String department,
                                                                        @RequestParam(required = false) String location) {

        log.info("GET {} - month: {}, year: {}, page: {}, limit: {}, department: {}, location: {}",
                AppConstants.SPECIAL_DAYS_BIRTHDAYS_ENDPOINT, month, year, page, limit, department, location);

        ResponseDTO<SpecialDaysDataDTO> response = specialDaysService.getSpecialDays(
                month, year, page, limit, "BIRTHDAY", department, location);

        return ResponseEntity.ok(response);
    }

    /**
     * Get work anniversaries with filtering.
     * All authenticated users can view anniversaries.
     */
    @GetMapping(value = AppConstants.SPECIAL_DAYS_ANNIVERSARIES_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<SpecialDaysDataDTO>> getAnniversaries(@RequestParam(required = false) Integer month,
                                                                            @RequestParam(required = false) Integer year,
                                                                            @RequestParam(required = false) Integer page,
                                                                            @RequestParam(required = false) Integer limit,
                                                                            @RequestParam(required = false) String department,
                                                                            @RequestParam(required = false) String location) {

        log.info("GET {} - month: {}, year: {}, page: {}, limit: {}, department: {}, location: {}",
                AppConstants.SPECIAL_DAYS_ANNIVERSARIES_ENDPOINT, month, year, page, limit, department, location);

        ResponseDTO<SpecialDaysDataDTO> response = specialDaysService.getSpecialDays(
                month, year, page, limit, "ANNIVERSARY", department, location);

        return ResponseEntity.ok(response);
    }
}
