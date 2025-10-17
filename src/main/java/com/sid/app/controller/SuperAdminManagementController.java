package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
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
 * Controller for Super Admin to manage Admins
 * Provides endpoints for viewing, updating, and managing Admin users within the same tenant
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class SuperAdminManagementController {

    @Autowired
    private TenantUserService tenantUserService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Get all Admins in the current Super Admin's tenant (tenant-isolated)
     */
    @GetMapping(EndpointConstants.ADMIN_MANAGEMENT_ENDPOINT)
    @RequiredRole({UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> getAllAdmins() {
        log.info("getAllAdmins() : Super Admin requesting all Admins in their tenant");

        try {
            Long superAdminId = jwtAuthenticationContext.getCurrentUserId();
            // Use getAdminsByTenant instead of getAdminsBySuperAdmin to see all Admins in the tenant
            List<TenantUserDTO> admins = tenantUserService.getAdminsByTenant(superAdminId);

            if (admins.isEmpty()) {
                log.info("getAllAdmins() : No Admins found in tenant for Super Admin ID: {}", superAdminId);
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No Admins found in your tenant.",
                        Collections.emptyList()
                ));
            }

            log.info("getAllAdmins() : Successfully retrieved {} Admins in tenant for Super Admin ID: {}", admins.size(), superAdminId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Admins retrieved successfully.",
                    admins
            ));
        } catch (Exception e) {
            log.error("getAllAdmins() : Error retrieving Admins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving Admins.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Get all Admins in the same tenant as Super Admin
     */
    @GetMapping(EndpointConstants.ADMIN_BY_TENANT_ENDPOINT)
    @RequiredRole({UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> getAdminsByTenant() {
        log.info("getAdminsByTenant() : Super Admin requesting all Admins in tenant");

        try {
            Long superAdminId = jwtAuthenticationContext.getCurrentUserId();
            List<TenantUserDTO> admins = tenantUserService.getAdminsByTenant(superAdminId);

            if (admins.isEmpty()) {
                log.info("getAdminsByTenant() : No Admins found in tenant for Super Admin ID: {}", superAdminId);
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No Admins found in your tenant.",
                        Collections.emptyList()
                ));
            }

            log.info("getAdminsByTenant() : Successfully retrieved {} Admins in tenant for Super Admin ID: {}", admins.size(), superAdminId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Admins retrieved successfully for tenant.",
                    admins
            ));
        } catch (Exception e) {
            log.error("getAdminsByTenant() : Error retrieving Admins by tenant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving Admins for tenant.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Search Admins in Super Admin's tenant by name or email
     */
    @GetMapping(EndpointConstants.ADMIN_SEARCH_ENDPOINT)
    @RequiredRole({UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<List<TenantUserDTO>>> searchAdmins(@RequestParam String searchTerm) {
        log.info("searchAdmins() : Super Admin searching Admins with term: {}", searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Search term cannot be empty.",
                        Collections.emptyList()
                ));
            }

            Long superAdminId = jwtAuthenticationContext.getCurrentUserId();
            List<TenantUserDTO> admins = tenantUserService.searchAdminsByTenant(superAdminId, searchTerm.trim());

            log.info("searchAdmins() : Found {} Admins matching search term for Super Admin ID: {}", admins.size(), superAdminId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Search completed successfully.",
                    admins
            ));
        } catch (Exception e) {
            log.error("searchAdmins() : Error searching Admins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while searching Admins.",
                            Collections.emptyList()
                    ));
        }
    }

    /**
     * Get Admin by ID (must be in same tenant as Super Admin)
     */
    @GetMapping(EndpointConstants.ADMIN_DETAILS_ENDPOINT)
    @RequiredRole({UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<TenantUserDTO>> getAdminById(@RequestParam Long adminId) {
        log.info("getAdminById() : Super Admin requesting Admin with ID: {}", adminId);

        try {
            if (adminId == null || adminId <= 0) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Valid Admin ID is required.",
                        null
                ));
            }

            Long superAdminId = jwtAuthenticationContext.getCurrentUserId();
            TenantUserDTO admin = tenantUserService.getAdminById(superAdminId, adminId);

            log.info("getAdminById() : Admin with ID {} retrieved successfully for Super Admin ID: {}", adminId, superAdminId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Admin retrieved successfully.",
                    admin
            ));
        } catch (EntityNotFoundException e) {
            log.warn("getAdminById() : Admin with ID {} not found or not accessible", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("getAdminById() : Error retrieving Admin with ID {}: {}", adminId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving the Admin.",
                            null
                    ));
        }
    }

    /**
     * Update Admin status (activate/deactivate) - Super Admin access
     */
    @PutMapping(EndpointConstants.ADMIN_STATUS_ENDPOINT)
    @RequiredRole({UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<TenantUserDTO>> updateAdminStatus(@RequestParam Long adminId,
                                                                        @RequestBody @Valid UserStatusUpdateRequest statusRequest) {
        log.info("updateAdminStatus() : Super Admin updating status for Admin ID: {}", adminId);

        try {
            if (adminId == null || adminId <= 0) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>(
                        AppConstants.STATUS_FAILED,
                        "Valid Admin ID is required.",
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

            Long superAdminId = jwtAuthenticationContext.getCurrentUserId();
            TenantUserDTO updatedAdmin = tenantUserService.updateAdminStatus(superAdminId, adminId, statusRequest);

            String action = Boolean.TRUE.equals(statusRequest.getIsActive()) ? "activated" : "deactivated";
            log.info("updateAdminStatus() : Admin with ID {} {} successfully by Super Admin ID: {}", adminId, action, superAdminId);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Admin status updated successfully.",
                    updatedAdmin
            ));
        } catch (EntityNotFoundException e) {
            log.warn("updateAdminStatus() : Admin with ID {} not found or not accessible", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("updateAdminStatus() : Error updating Admin status for ID {}: {}", adminId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while updating Admin status.",
                            null
                    ));
        }
    }
}
