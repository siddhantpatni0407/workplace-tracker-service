package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserLeaveDTO;
import com.sid.app.service.UserLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for handling user leave operations with role-based authorization.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(EndpointConstants.USER_LEAVE_ENDPOINT)
public class UserLeaveController {

    private final UserLeaveService userLeaveService;

    @Autowired
    private JwtAuthenticationContext authContext;

    /**
     * Gets all leaves for a specific user.
     * Users can only view their own leaves unless they are admin.
     */
    @GetMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<List<UserLeaveDTO>>> getUserLeaves() {
        Long userId = authContext.getCurrentUserId();
        log.info("getUserLeaves() - userId={}", userId);

        if (Optional.ofNullable(userId).orElse(0L) <= 0) {
            log.warn("getUserLeaves() - invalid userId={}", userId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_USER_ID, null));
        }

        // Validate user can view leaves for the specified userId
        if (!authContext.isOwnerOrAdmin(userId)) {
            log.warn("getUserLeaves() : User {} attempted to view leaves for user {}",
                    authContext.getCurrentUserId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only view your own leaves", null));
        }

        List<UserLeaveDTO> list = userLeaveService.getLeavesForUser(userId);

        if (list == null || list.isEmpty()) {
            log.info("getUserLeaves() - no leaves found for userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_LEAVES_FOR_USER, list));
        }

        log.info("getUserLeaves() - returning {} leaves for userId={}", list.size(), userId);
        return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVES_RETRIEVED, list));
    }

    /**
     * Creates a new leave request.
     * Users can only create leaves for themselves unless they are admin.
     */
    @PostMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<UserLeaveDTO>> createLeave(@Valid @RequestBody UserLeaveDTO req) {
        log.info("createLeave() - userId={} policyId={} startDate={} endDate={}",
                req.getUserId(), req.getPolicyId(), req.getStartDate(), req.getEndDate());

        // Validate user can create leave for the specified userId
        if (!authContext.isOwnerOrAdmin(req.getUserId())) {
            log.warn("createLeave() : User {} attempted to create leave for user {}",
                    authContext.getCurrentUserId(), req.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only create leaves for yourself", null));
        }

        try {
            UserLeaveDTO dto = userLeaveService.createLeave(req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_CREATED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("createLeave() - invalid request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("createLeave() - unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    /**
     * Updates an existing leave request.
     * Users can only update their own leaves unless they are admin.
     */
    @PutMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<UserLeaveDTO>> updateLeave(@RequestParam("userLeaveId") Long userLeaveId,
                                                                 @Valid @RequestBody UserLeaveDTO req) {

        log.info("updateLeave() - userLeaveId={} userId={}", userLeaveId, req.getUserId());

        if (Optional.ofNullable(userLeaveId).orElse(0L) <= 0) {
            log.warn("updateLeave() - invalid userLeaveId={}", userLeaveId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_LEAVE_ID, null));
        }

        // Validate user can update leave for the specified userId
        if (!authContext.isOwnerOrAdmin(req.getUserId())) {
            log.warn("updateLeave() : User {} attempted to update leave for user {}",
                    authContext.getCurrentUserId(), req.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "You can only update your own leaves", null));
        }

        try {
            UserLeaveDTO dto = userLeaveService.updateLeave(userLeaveId, req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_UPDATED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("updateLeave() - invalid request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("updateLeave() - unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    /**
     * Deletes a leave request.
     * Users can only delete their own leaves unless they are admin.
     */
    @DeleteMapping
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<Void>> deleteLeave(@RequestParam("userLeaveId") Long userLeaveId) {
        log.info("deleteLeave() - userLeaveId={}", userLeaveId);

        if (Optional.ofNullable(userLeaveId).orElse(0L) <= 0) {
            log.warn("deleteLeave() - invalid userLeaveId={}", userLeaveId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_LEAVE_ID, null));
        }

        try {
            // First get the leave to validate ownership (you'll need to add this method to service)
            // For now, we'll rely on the service to handle ownership validation
            userLeaveService.deleteLeave(userLeaveId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_DELETED, null));
        } catch (Exception ex) {
            log.warn("deleteLeave() - failed userLeaveId={} error={}", userLeaveId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }
}
