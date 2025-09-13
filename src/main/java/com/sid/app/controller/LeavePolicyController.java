package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.LeavePolicyDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService policyService;

    @GetMapping(AppConstants.LEAVE_POLICY_ENDPOINT)
    public ResponseEntity<ResponseDTO<List<LeavePolicyDTO>>> getAllPolicies() {
        log.info("getAllPolicies() - request");
        List<LeavePolicyDTO> list = policyService.getAllPolicies();

        if (list == null || list.isEmpty()) {
            log.warn("getAllPolicies() - no policies found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NO_LEAVE_POLICIES_FOUND, list));
        }

        log.info("getAllPolicies() - returning {} policies", list.size());
        return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_POLICY_RETRIEVED, list));
    }

    @GetMapping(AppConstants.EXACT_LEAVE_POLICY_ENDPOINT)
    public ResponseEntity<ResponseDTO<LeavePolicyDTO>> getPolicy(@RequestParam("policyId") Long policyId) {
        log.info("getPolicy() - policyId={}", policyId);

        if (Optional.ofNullable(policyId).orElse(0L) <= 0) {
            log.warn("getPolicy() - invalid policyId={}", policyId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_POLICY_ID, null));
        }

        try {
            LeavePolicyDTO dto = policyService.getPolicy(policyId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_POLICY_RETRIEVED, dto));
        } catch (IllegalArgumentException ex) {
            log.warn("getPolicy() - bad request policyId={} error={}", policyId, ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.warn("getPolicy() - not found policyId={} error={}", policyId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }

    @PostMapping(AppConstants.LEAVE_POLICY_ENDPOINT)
    public ResponseEntity<ResponseDTO<LeavePolicyDTO>> createPolicy(@Valid @RequestBody LeavePolicyDTO req) {
        log.info("createPolicy() - code={}", req.getPolicyCode());
        try {
            LeavePolicyDTO created = policyService.createPolicy(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_POLICY_CREATED, created));
        } catch (IllegalArgumentException ex) {
            log.warn("createPolicy() - invalid payload code={} error={}", req.getPolicyCode(), ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("createPolicy() - unexpected error code={} error={}", req.getPolicyCode(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }

    @PutMapping(AppConstants.LEAVE_POLICY_ENDPOINT)
    public ResponseEntity<ResponseDTO<LeavePolicyDTO>> updatePolicy(@RequestParam("policyId") Long policyId,
                                                                    @Valid @RequestBody LeavePolicyDTO req) {
        log.info("updatePolicy() - policyId={} code={}", policyId, req.getPolicyCode());

        if (Optional.ofNullable(policyId).orElse(0L) <= 0) {
            log.warn("updatePolicy() - invalid policyId={}", policyId);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_POLICY_ID, null));
        }

        try {
            LeavePolicyDTO updated = policyService.updatePolicy(policyId, req);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_POLICY_UPDATED, updated));
        } catch (IllegalArgumentException ex) {
            log.warn("updatePolicy() - bad request policyId={} error={}", policyId, ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.warn("updatePolicy() - not found policyId={} error={}", policyId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        }
    }
}
