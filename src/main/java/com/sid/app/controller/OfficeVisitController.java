package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.OfficeVisitDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.OfficeVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstants.VISITS_ENDPOINT)
public class OfficeVisitController {

    private final OfficeVisitService visitService;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<OfficeVisitDTO>>> getVisitsForMonth(
            @RequestParam("userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        log.info("getVisitsForMonth() userId={} year={} month={}", userId, year, month);

        if (Optional.ofNullable(userId).orElse(0L) <= 0 || month < 1 || month > 12) {
            log.warn("getVisitsForMonth() - invalid params userId={} month={}", userId, month);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_VISIT_PARAMS, null));
        }

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<OfficeVisitDTO> list = visitService.getVisitsForUserBetween(userId, from, to);

        if (list.isEmpty()) {
            log.info("getVisitsForMonth() - no visits found userId={} year={} month={}", userId, year, month);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_VISITS_FOUND, list));
        }

        log.info("getVisitsForMonth() - returning {} visits userId={} year={} month={}", list.size(), userId, year, month);
        return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_VISITS_RETRIEVED, list));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<OfficeVisitDTO>> upsertVisit(@Valid @RequestBody OfficeVisitDTO req) {
        log.info("upsertVisit() userId={} visitDate={}", req.getUserId(), req.getVisitDate());

        try {
            OfficeVisitDTO dto = visitService.createOrUpdateVisit(req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_VISIT_SAVED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("upsertVisit() - invalid request userId={} date={} error={}", req.getUserId(), req.getVisitDate(), ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("upsertVisit() - unexpected error userId={} date={} error={}", req.getUserId(), req.getVisitDate(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO<Void>> deleteVisit(@RequestParam("officeVisitId") Long officeVisitId) {
        log.info("deleteVisit() officeVisitId={}", officeVisitId);

        if (Optional.ofNullable(officeVisitId).orElse(0L) <= 0) {
            log.warn("deleteVisit() - invalid officeVisitId={}", officeVisitId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_VISIT_ID, null));
        }

        try {
            visitService.deleteVisit(officeVisitId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_VISIT_DELETED, null));
        } catch (Exception ex) {
            log.warn("deleteVisit() - failed officeVisitId={} error={}", officeVisitId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }
}
