package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserLeaveDTO;
import com.sid.app.service.UserLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstants.USER_LEAVE_ENDPOINT)
public class UserLeaveController {

    private final UserLeaveService userLeaveService;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<UserLeaveDTO>>> getUserLeaves(@RequestParam("userId") Long userId) {
        log.info("getUserLeaves() - userId={}", userId);

        if (Optional.ofNullable(userId).orElse(0L) <= 0) {
            log.warn("getUserLeaves() - invalid userId={}", userId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_USER_ID, null));
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

    @PostMapping
    public ResponseEntity<ResponseDTO<UserLeaveDTO>> createLeave(@Valid @RequestBody UserLeaveDTO req) {
        log.info("createLeave() - userId={} policyId={} startDate={} endDate={}",
                req.getUserId(), req.getPolicyId(), req.getStartDate(), req.getEndDate());

        try {
            UserLeaveDTO created = userLeaveService.createLeave(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_CREATED, created));
        } catch (IllegalArgumentException ex) {
            log.warn("createLeave() - invalid request userId={} error={}", req.getUserId(), ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("createLeave() - unexpected error userId={} error={}", req.getUserId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    @PutMapping
    public ResponseEntity<ResponseDTO<UserLeaveDTO>> updateLeave(@RequestParam("userLeaveId") Long userLeaveId,
                                                                 @Valid @RequestBody UserLeaveDTO req) {
        log.info("updateLeave() - userLeaveId={} userId={}", userLeaveId, req.getUserId());

        if (Optional.ofNullable(userLeaveId).orElse(0L) <= 0) {
            log.warn("updateLeave() - invalid userLeaveId={}", userLeaveId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_LEAVE_ID, null));
        }

        try {
            UserLeaveDTO updated = userLeaveService.updateLeave(userLeaveId, req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_UPDATED, updated));
        } catch (IllegalArgumentException ex) {
            log.warn("updateLeave() - bad request userLeaveId={} error={}", userLeaveId, ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.warn("updateLeave() - not found userLeaveId={} error={}", userLeaveId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO<Void>> deleteLeave(@RequestParam("userLeaveId") Long userLeaveId) {
        log.info("deleteLeave() - userLeaveId={}", userLeaveId);

        if (Optional.ofNullable(userLeaveId).orElse(0L) <= 0) {
            log.warn("deleteLeave() - invalid userLeaveId={}", userLeaveId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_LEAVE_ID, null));
        }

        try {
            userLeaveService.deleteLeave(userLeaveId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_LEAVE_DELETED, null));
        } catch (Exception ex) {
            log.warn("deleteLeave() - failed userLeaveId={} error={}", userLeaveId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }
}
