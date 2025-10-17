package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.TenantUserDTO;
import com.sid.app.model.UserStatusUpdateRequest;
import com.sid.app.service.TenantUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller for Platform User to manage Super Admins
 * Provides endpoints for viewing, updating, and managing Super Admin users
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class PlatformUserManagementController {

    @Autowired
    private TenantUserService tenantUserService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Get all Super Admins in the system
     */
    @GetMapping(AppConstants.SUPER_ADMIN_MANAGEMENT_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> getAllSuperAdmins() {
        log.info("getAllSuperAdmins() : Platform User requesting all Super Admins");

        try {
            List<TenantUserDTO> superAdmins = tenantUserService.getAllSuperAdmins();

            if (superAdmins.isEmpty()) {
                log.info("getAllSuperAdmins() : No Super Admins found in the system");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No Super Admins found.",
                        Collections.emptyList()
                ));
            }

            log.info("getAllSuperAdmins() : Successfully retrieved {} Super Admins", superAdmins.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Super Admins retrieved successfully.",
                    superAdmins
            ));
        } catch (Exception e) {
            log.error("getAllSuperAdmins() : Error retrieving Super Admins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving Super Admins.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Get Super Admins by tenant ID
     */
    @GetMapping(AppConstants.SUPER_ADMIN_BY_TENANT_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> getSuperAdminsByTenant(@RequestParam Long tenantId) {
        log.info("getSuperAdminsByTenant() : Platform User requesting Super Admins for tenant ID: {}", tenantId);

        try {
            if (tenantId == null || tenantId <= 0) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Valid tenant ID is required.",
                        Collections.emptyList()
                ));
            }

            List<TenantUserDTO> superAdmins = tenantUserService.getSuperAdminsByTenantId(tenantId);

            if (superAdmins.isEmpty()) {
                log.info("getSuperAdminsByTenant() : No Super Admins found for tenant ID: {}", tenantId);
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No Super Admins found for this tenant.",
                        Collections.emptyList()
                ));
            }

            log.info("getSuperAdminsByTenant() : Successfully retrieved {} Super Admins for tenant ID: {}", superAdmins.size(), tenantId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Super Admins retrieved successfully for tenant.",
                    superAdmins
            ));
        } catch (Exception e) {
            log.error("getSuperAdminsByTenant() : Error retrieving Super Admins for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving Super Admins for tenant.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Search Super Admins by name or email
     */
    @GetMapping(AppConstants.SUPER_ADMIN_SEARCH_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> searchSuperAdmins(@RequestParam String searchTerm) {
        log.info("searchSuperAdmins() : Platform User searching Super Admins with term: {}", searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Search term cannot be empty.",
                        Collections.emptyList()
                ));
            }

            List<TenantUserDTO> superAdmins = tenantUserService.searchSuperAdmins(searchTerm.trim());

            log.info("searchSuperAdmins() : Found {} Super Admins matching search term", superAdmins.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Search completed successfully.",
                    superAdmins
            ));
        } catch (Exception e) {
            log.error("searchSuperAdmins() : Error searching Super Admins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while searching Super Admins.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Get Super Admin by ID
     */
    @GetMapping(AppConstants.SUPER_ADMIN_DETAILS_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<TenantUserDTO>> getSuperAdminById(@RequestParam Long tenantUserId) {
        log.info("getSuperAdminById() : Platform User requesting Super Admin with ID: {}", tenantUserId);

        try {
            if (tenantUserId == null || tenantUserId <= 0) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Valid Super Admin ID is required.",
                        null
                ));
            }

            TenantUserDTO superAdmin = tenantUserService.getSuperAdminById(tenantUserId);
            log.info("getSuperAdminById() : Super Admin with ID {} retrieved successfully", tenantUserId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Super Admin retrieved successfully.",
                    superAdmin
            ));
        } catch (EntityNotFoundException e) {
            log.warn("getSuperAdminById() : Super Admin with ID {} not found", tenantUserId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("getSuperAdminById() : Error retrieving Super Admin with ID {}: {}", tenantUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving the Super Admin.",
                            null
                    ));
        }
    }

    /**
     * Update Super Admin status (activate/deactivate)
     */
    @PutMapping(AppConstants.SUPER_ADMIN_STATUS_ENDPOINT)
    @RequiredRole({"PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<TenantUserDTO>> updateSuperAdminStatus(@RequestParam Long tenantUserId,
                                                                             @RequestBody @Valid UserStatusUpdateRequest statusRequest) {
        log.info("updateSuperAdminStatus() : Platform User updating status for Super Admin ID: {}", tenantUserId);

        try {
            if (tenantUserId == null || tenantUserId <= 0) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Valid Super Admin ID is required.",
                        null
                ));
            }

            if (statusRequest.getIsActive() == null) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Status (isActive) is required.",
                        null
                ));
            }

            TenantUserDTO updatedSuperAdmin = tenantUserService.updateSuperAdminStatus(tenantUserId, statusRequest);

            String action = Boolean.TRUE.equals(statusRequest.getIsActive()) ? "activated" : "deactivated";
            log.info("updateSuperAdminStatus() : Super Admin with ID {} {} successfully", tenantUserId, action);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Super Admin status updated successfully.",
                    updatedSuperAdmin
            ));
        } catch (EntityNotFoundException e) {
            log.warn("updateSuperAdminStatus() : Super Admin with ID {} not found", tenantUserId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("updateSuperAdminStatus() : Error updating Super Admin status for ID {}: {}", tenantUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while updating Super Admin status.",
                            null
                    ));
        }
    }
}
