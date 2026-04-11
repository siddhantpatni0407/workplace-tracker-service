package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.PlatformStatsDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.PlatformStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sid.app.annotation.CorrelationId;

/**
 * Controller for platform statistics operations.
 * Provides endpoints for PLATFORM_USER to view comprehensive platform statistics.
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
@Tag(name = "Platform Stats", description = "High-level platform-wide statistics")
@SecurityRequirement(name = "bearerAuth")
public class PlatformStatsController {

    @Autowired
    private PlatformStatsService platformStatsService;

    /**
     * Get comprehensive platform statistics including tenant counts, user role distributions, etc.
     * Only accessible by PLATFORM_USER role.
     *
     * @return ResponseEntity with platform statistics
     */
    @Operation(summary = "Get platform statistics", description = "Retrieve platform-wide stats including tenant counts and user role distributions. Requires PLATFORM_USER role.")
    @GetMapping(EndpointConstants.PLATFORM_STATS_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    @CorrelationId
    public ResponseEntity<ResponseDTO<PlatformStatsDTO>> getPlatformStats() {
        log.info("getPlatformStats() : Received request to fetch platform statistics");

        try {
            PlatformStatsDTO stats = platformStatsService.getPlatformStats();

            log.info("getPlatformStats() : Successfully retrieved platform statistics - " +
                    "Tenants: {}, Super Admins: {}, Admins: {}, Users: {}, Total: {}",
                    stats.getTotalTenants(), stats.getTotalSuperAdmins(),
                    stats.getTotalAdmins(), stats.getTotalUsers(), stats.getTotalTenantUsers());

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_PLATFORM_STATS_RETRIEVED,
                    stats
            ));

        } catch (Exception e) {
            log.error("getPlatformStats() : Error retrieving platform statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_PLATFORM_STATS_FETCH_FAILED + ": " + e.getMessage(),
                            null
                    ));
        }
    }
}
