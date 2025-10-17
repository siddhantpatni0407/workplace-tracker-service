package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserLeaveBalanceDTO;
import com.sid.app.repository.LeavePolicyRepository;
import com.sid.app.repository.UserLeaveRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.service.UserLeaveBalanceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserLeaveBalanceController {

    private final UserLeaveBalanceService balanceService;
    private final LeavePolicyRepository policyRepo;
    private final UserLeaveRepository userLeaveRepo;
    private final UserRepository userRepo;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * GET /user-leave-balance?policyId=..&year=..
     */
    @GetMapping(EndpointConstants.USER_LEAVE_BALANCE_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<UserLeaveBalanceDTO>> getBalance(@RequestParam("policyId") Long policyId,
                                                                       @RequestParam("year") Integer year) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getBalance() userId={} policyId={} year={}", userId, policyId, year);

        if (Optional.ofNullable(userId).orElse(0L) <= 0 ||
                Optional.ofNullable(policyId).orElse(0L) <= 0 ||
                Optional.ofNullable(year).orElse(0) < 1900) {
            log.warn("getBalance() - invalid params userId={} policyId={} year={}", userId, policyId, year);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_BALANCE_PARAMS, null));
        }

        try {
            UserLeaveBalanceDTO dto = balanceService.getBalance(userId, policyId, year);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_BALANCE_RETRIEVED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("getBalance() bad request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.warn("getBalance() - not found or error userId={} policyId={} year={} error={}", userId, policyId, year, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }

    /**
     * Admin override: Upsert balance manually.
     * Keep this restricted to ADMIN role to avoid accidental edits.
     */
    @PostMapping(EndpointConstants.USER_LEAVE_BALANCE_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<UserLeaveBalanceDTO>> upsertBalance(@Valid @RequestBody UserLeaveBalanceDTO req) {
        log.info("upsertBalance() (ADMIN) userId={} policyId={} year={}", req.getUserId(), req.getPolicyId(), req.getYear());

        if (req.getUserId() == null || req.getPolicyId() == null || req.getYear() == null) {
            throw new IllegalArgumentException("userId, policyId and year are required");
        }

        // validate user exists
        if (!userRepo.existsById(req.getUserId())) {
            throw new EntityNotFoundException("User not found with id: " + req.getUserId());
        }

        // validate policy exists
        if (!policyRepo.existsById(req.getPolicyId())) {
            throw new EntityNotFoundException("Leave policy not found with id: " + req.getPolicyId());
        }

        try {
            UserLeaveBalanceDTO dto = balanceService.upsertBalance(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_BALANCE_UPSERTED, dto));
        } catch (EntityNotFoundException ex) {
            log.warn("upsertBalance() - not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (IllegalArgumentException ex) {
            log.warn("upsertBalance() - invalid request userId={} policyId={} year={} error={}",
                    req.getUserId(), req.getPolicyId(), req.getYear(), ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("upsertBalance() - unexpected error userId={} policyId={} year={} error={}",
                    req.getUserId(), req.getPolicyId(), req.getYear(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    /**
     * Adjust balance programmatically.
     * This endpoint is optional — preferred pattern is to call balanceService.adjustBalance()
     * from inside the leave create/update/delete flows.
     * <p>
     * Example:
     * POST /user-leave-balance/adjust?policyId=1&year=2025&delta=1.0
     * <p>
     * This endpoint is useful for adhoc adjustments / testing and should be protected.
     */
    @PostMapping(EndpointConstants.USER_LEAVE_BALANCE_ADJUST_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<UserLeaveBalanceDTO>> adjustBalance(@RequestParam("policyId") Long policyId,
                                                                          @RequestParam("year") Integer year,
                                                                          @RequestParam("delta") BigDecimal delta) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("adjustBalance() (ADMIN) userId={} policyId={} year={} delta={}", userId, policyId, year, delta);

        if (!userRepo.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        if (!policyRepo.existsById(policyId)) {
            throw new EntityNotFoundException("Leave policy not found with id: " + policyId);
        }

        try {
            UserLeaveBalanceDTO dto = balanceService.adjustBalance(userId, policyId, year, delta);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_BALANCE_ADJUSTED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("adjustBalance() - bad request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("adjustBalance() - error userId={} policyId={} year={} delta={} error={}",
                    userId, policyId, year, delta, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    /**
     * Recalculate balance from user_leave records for a user+policy+year (admin).
     * Useful for reconciliation after a bug or historical import.
     */
    @PostMapping(EndpointConstants.USER_LEAVE_BALANCE_RECALCULATE_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<UserLeaveBalanceDTO>> recalculate(@RequestParam("policyId") Long policyId,
                                                                        @RequestParam("year") Integer year) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("recalculateBalance() (ADMIN) userId={} policyId={} year={}", userId, policyId, year);

        if (!userRepo.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        if (!policyRepo.existsById(policyId)) {
            throw new EntityNotFoundException("Leave policy not found with id: " + policyId);
        }

        try {
            UserLeaveBalanceDTO dto = balanceService.recalculateBalanceFromLeaves(userId, policyId, year);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_BALANCE_RECALCULATED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("recalculateBalance() - bad request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("recalculateBalance() - error userId={} policyId={} year={} error={}", userId, policyId, year, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }
}
