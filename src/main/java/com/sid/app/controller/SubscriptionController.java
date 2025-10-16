package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.SubscriptionDTO;
import com.sid.app.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller for managing subscriptions.
 * All endpoints are restricted to PLATFORM_USER role only.
 * Provides read operations for subscription management.
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Get all subscriptions
     */
    @GetMapping(AppConstants.SUBSCRIPTIONS_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<SubscriptionDTO>>> getAllSubscriptions() {
        log.info("getAllSubscriptions() : Fetching all subscriptions");

        try {
            List<SubscriptionDTO> subscriptions = subscriptionService.getAllSubscriptions();

            if (subscriptions.isEmpty()) {
                log.info("getAllSubscriptions() : No subscriptions found");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        AppConstants.ERROR_NO_SUBSCRIPTIONS_FOUND,
                        Collections.emptyList()
                ));
            }

            log.info("getAllSubscriptions() : Retrieved {} subscriptions", subscriptions.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_SUBSCRIPTIONS_RETRIEVED,
                    subscriptions
            ));
        } catch (Exception ex) {
            log.error("getAllSubscriptions() : Error fetching subscriptions: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Failed to fetch subscriptions",
                            null
                    )
            );
        }
    }

    /**
     * Get all active subscriptions
     */
    @GetMapping(AppConstants.ACTIVE_SUBSCRIPTIONS_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<SubscriptionDTO>>> getActiveSubscriptions() {
        log.info("getActiveSubscriptions() : Fetching all active subscriptions");

        try {
            List<SubscriptionDTO> activeSubscriptions = subscriptionService.getActiveSubscriptions();

            if (activeSubscriptions.isEmpty()) {
                log.info("getActiveSubscriptions() : No active subscriptions found");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No active subscriptions found",
                        Collections.emptyList()
                ));
            }

            log.info("getActiveSubscriptions() : Retrieved {} active subscriptions", activeSubscriptions.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_ACTIVE_SUBSCRIPTIONS_RETRIEVED,
                    activeSubscriptions
            ));
        } catch (Exception ex) {
            log.error("getActiveSubscriptions() : Error fetching active subscriptions: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Failed to fetch active subscriptions",
                            null
                    )
            );
        }
    }

    /**
     * Get subscription by code
     */
    @GetMapping(AppConstants.SUBSCRIPTION_BY_CODE_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<SubscriptionDTO>> getSubscriptionByCode(@RequestParam String subscriptionCode) {
        log.info("getSubscriptionByCode() : Fetching subscription with code: {}", subscriptionCode);

        try {
            if (subscriptionCode == null || subscriptionCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResponseDTO<>(
                                AppConstants.STATUS_FAILED,
                                "Subscription code cannot be empty",
                                null
                        )
                );
            }

            SubscriptionDTO subscription = subscriptionService.getSubscriptionByCode(subscriptionCode.trim());
            log.info("getSubscriptionByCode() : Subscription retrieved successfully with code: {}", subscriptionCode);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_SUBSCRIPTION_RETRIEVED,
                    subscription
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("getSubscriptionByCode() : Subscription not found with code: {}", subscriptionCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("getSubscriptionByCode() : Error fetching subscription with code {}: {}", subscriptionCode, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Failed to fetch subscription",
                            null
                    )
            );
        }
    }
}
