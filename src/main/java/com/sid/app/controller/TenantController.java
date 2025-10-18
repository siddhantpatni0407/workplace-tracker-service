package com.sid.app.controller;

import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.*;
import com.sid.app.service.TenantService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller for managing tenants.
 * All endpoints are restricted to PLATFORM_USER role only.
 * Provides full CRUD operations for tenant management.
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * Create a new tenant
     */
    @PostMapping(EndpointConstants.TENANT_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        log.info("createTenant() : Received request to create tenant: {}", ApplicationUtils.getJSONString(request));

        try {
            TenantDTO createdTenant = tenantService.createTenant(request);
            log.info("createTenant() : Tenant created successfully with ID: {}", createdTenant.getTenantId());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_SUCCESS,
                            AppConstants.SUCCESS_TENANT_CREATED_MESSAGE,
                            createdTenant
                    )
            );
        } catch (IllegalArgumentException ex) {
            log.warn("createTenant() : Validation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("createTenant() : Error creating tenant: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_CREATION_FAILED_MESSAGE,
                            null
                    )
            );
        }
    }

    /**
     * Get all tenants with pagination
     */
    @GetMapping(EndpointConstants.TENANTS_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<Page<TenantDTO>>> getAllTenants(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(defaultValue = "tenantId") String sortBy,
                                                                      @RequestParam(defaultValue = AppConstants.SORT_DIRECTION_DESC) String sortDir) {

        log.info("getAllTenants() : Fetching tenants - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        try {
            Sort sort = sortDir.equalsIgnoreCase(AppConstants.SORT_DIRECTION_DESC)
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TenantDTO> tenants = tenantService.getAllTenants(pageable);

            if (tenants.isEmpty()) {
                log.info("getAllTenants() : No tenants found");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        AppConstants.INFO_NO_TENANTS_FOUND,
                        tenants
                ));
            }

            log.info("getAllTenants() : Retrieved {} tenants", tenants.getTotalElements());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANTS_RETRIEVED_MESSAGE,
                    tenants
            ));
        } catch (Exception ex) {
            log.error("getAllTenants() : Error fetching tenants: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANTS_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Get all active tenants
     */
    @GetMapping(EndpointConstants.ACTIVE_TENANTS_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<List<TenantDTO>>> getActiveTenants() {
        log.info("getActiveTenants() : Fetching all active tenants");

        try {
            List<TenantDTO> activeTenants = tenantService.getActiveTenants();

            if (activeTenants.isEmpty()) {
                log.info("getActiveTenants() : No active tenants found");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        AppConstants.INFO_NO_ACTIVE_TENANTS_FOUND,
                        Collections.emptyList()
                ));
            }

            log.info("getActiveTenants() : Retrieved {} active tenants", activeTenants.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_ACTIVE_TENANTS_RETRIEVED_MESSAGE,
                    activeTenants
            ));
        } catch (Exception ex) {
            log.error("getActiveTenants() : Error fetching active tenants: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_ACTIVE_TENANTS_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Get tenant by ID
     */
    @GetMapping(EndpointConstants.TENANT_BY_ID_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> getTenantById(@RequestParam Long tenantId) {
        log.info("getTenantById() : Fetching tenant with ID: {}", tenantId);

        try {
            TenantDTO tenant = tenantService.getTenantById(tenantId);
            log.info("getTenantById() : Tenant retrieved successfully with ID: {}", tenantId);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_RETRIEVED_MESSAGE,
                    tenant
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("getTenantById() : Tenant not found with ID: {}", tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("getTenantById() : Error fetching tenant with ID {}: {}", tenantId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Get tenant by code
     */
    @GetMapping(EndpointConstants.TENANT_BY_CODE_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> getTenantByCode(@RequestParam String tenantCode) {
        log.info("getTenantByCode() : Fetching tenant with code: {}", tenantCode);

        try {
            TenantDTO tenant = tenantService.getTenantByCode(tenantCode);
            log.info("getTenantByCode() : Tenant retrieved successfully with code: {}", tenantCode);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_RETRIEVED_MESSAGE,
                    tenant
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("getTenantByCode() : Tenant not found with code: {}", tenantCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("getTenantByCode() : Error fetching tenant with code {}: {}", tenantCode, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Update tenant
     */
    @PutMapping(value = EndpointConstants.TENANT_UPDATE_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> updateTenant(@RequestParam Long tenantId,
                                                               @Valid @RequestBody TenantUpdateRequest request) {

        log.info("updateTenant() : Received request to update tenant ID {}: {}",
                tenantId, ApplicationUtils.getJSONString(request));

        try {
            TenantDTO updatedTenant = tenantService.updateTenant(tenantId, request);
            log.info("updateTenant() : Tenant updated successfully with ID: {}", tenantId);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_UPDATED_MESSAGE,
                    updatedTenant
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("updateTenant() : Tenant not found with ID: {}", tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (IllegalArgumentException ex) {
            log.warn("updateTenant() : Validation failed for tenant ID {}: {}", tenantId, ex.getMessage());
            return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("updateTenant() : Error updating tenant ID {}: {}", tenantId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_UPDATE_FAILED_MESSAGE,
                            null
                    )
            );
        }
    }

    /**
     * Update tenant status (activate/deactivate)
     */
    @PatchMapping(value = EndpointConstants.TENANT_STATUS_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> updateTenantStatus(
            @Valid @RequestBody TenantStatusUpdateRequest request) {

        log.info("updateTenantStatus() : Received request to update status: {}",
                ApplicationUtils.getJSONString(request));

        try {
            TenantDTO updatedTenant = tenantService.updateTenantStatus(request);
            log.info("updateTenantStatus() : Tenant status updated successfully for ID: {}",
                    request.getTenantId());

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_STATUS_UPDATED_MESSAGE,
                    updatedTenant
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("updateTenantStatus() : Tenant not found with ID: {}", request.getTenantId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("updateTenantStatus() : Error updating tenant status for ID {}: {}",
                    request.getTenantId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_STATUS_UPDATE_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Delete tenant (soft delete by deactivation)
     */
    @DeleteMapping(EndpointConstants.TENANT_DELETE_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<Void>> deleteTenant(@RequestParam Long tenantId) {
        log.info("deleteTenant() : Received request to delete tenant with ID: {}", tenantId);

        try {
            tenantService.deleteTenant(tenantId);
            log.info("deleteTenant() : Tenant deleted successfully with ID: {}", tenantId);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_DELETED_MESSAGE,
                    null
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("deleteTenant() : Tenant not found with ID: {}", tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("deleteTenant() : Error deleting tenant with ID {}: {}", tenantId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_DELETE_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Search tenants by name
     */
    @GetMapping(EndpointConstants.TENANT_SEARCH_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<List<TenantDTO>>> searchTenants(@RequestParam String searchTerm) {
        log.info("searchTenants() : Searching tenants with term: {}", searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResponseDTO<>(
                                AppConstants.STATUS_FAILED,
                                AppConstants.ERROR_SEARCH_TERM_EMPTY,
                                null
                        )
                );
            }

            List<TenantDTO> tenants = tenantService.searchTenantsByName(searchTerm.trim());

            if (tenants.isEmpty()) {
                log.info("searchTenants() : No tenants found for search term: {}", searchTerm);
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        AppConstants.INFO_NO_TENANTS_FOUND_FOR_SEARCH,
                        Collections.emptyList()
                ));
            }

            log.info("searchTenants() : Found {} tenants for search term: {}", tenants.size(), searchTerm);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANTS_FOUND,
                    tenants
            ));
        } catch (Exception ex) {
            log.error("searchTenants() : Error searching tenants with term {}: {}", searchTerm, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANTS_SEARCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Get tenant statistics
     */
    @GetMapping(EndpointConstants.TENANT_STATS_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> getTenantStats(@RequestParam Long tenantId) {
        log.info("getTenantStats() : Fetching statistics for tenant ID: {}", tenantId);

        try {
            TenantDTO tenantStats = tenantService.getTenantStats(tenantId);
            log.info("getTenantStats() : Statistics retrieved for tenant ID: {}", tenantId);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_STATS_RETRIEVED_MESSAGE,
                    tenantStats
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("getTenantStats() : Tenant not found with ID: {}", tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("getTenantStats() : Error fetching statistics for tenant ID {}: {}", tenantId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_STATS_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Get users for a specific tenant
     */
    @GetMapping(EndpointConstants.TENANT_USERS_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<List<Object>>> getTenantUsers(@RequestParam Long tenantId) {
        log.info("getTenantUsers() : Fetching users for tenant ID: {}", tenantId);

        try {
            List<Object> tenantUsers = tenantService.getTenantUsers(tenantId);

            if (tenantUsers.isEmpty()) {
                log.info("getTenantUsers() : No users found for tenant ID: {}", tenantId);
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        AppConstants.INFO_NO_USERS_FOUND_FOR_TENANT,
                        Collections.emptyList()
                ));
            }

            log.info("getTenantUsers() : Found {} users for tenant ID: {}", tenantUsers.size(), tenantId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_TENANT_USERS_RETRIEVED_MESSAGE,
                    tenantUsers
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("getTenantUsers() : Tenant not found with ID: {}", tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("getTenantUsers() : Error fetching users for tenant ID {}: {}", tenantId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            AppConstants.ERROR_TENANT_USERS_FETCH_FAILED,
                            null
                    )
            );
        }
    }

    /**
     * Update tenant's subscription plan
     */
    @PatchMapping(EndpointConstants.TENANT_SUBSCRIPTION_UPDATE_ENDPOINT)
    @RequiredRole({UserRole.PLATFORM_USER})
    public ResponseEntity<ResponseDTO<TenantDTO>> updateTenantSubscription(@RequestParam String tenantCode,
                                                                           @RequestParam String subscriptionCode) {
        log.info("updateTenantSubscription() : Updating subscription for tenant: {} to subscription: {}",
                tenantCode, subscriptionCode);

        try {
            // Validate input parameters
            if (tenantCode == null || tenantCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResponseDTO<>(
                                AppConstants.STATUS_FAILED,
                                "Tenant code cannot be empty",
                                null
                        )
                );
            }

            if (subscriptionCode == null || subscriptionCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResponseDTO<>(
                                AppConstants.STATUS_FAILED,
                                "Subscription code cannot be empty",
                                null
                        )
                );
            }

            TenantDTO updatedTenant = tenantService.updateTenantSubscription(
                    tenantCode.trim(), subscriptionCode.trim());

            log.info("updateTenantSubscription() : Tenant subscription updated successfully for tenant: {}",
                    tenantCode);

            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Tenant subscription updated successfully",
                    updatedTenant
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("updateTenantSubscription() : Entity not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (IllegalArgumentException ex) {
            log.warn("updateTenantSubscription() : Validation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("updateTenantSubscription() : Error updating tenant subscription for tenant {}: {}",
                    tenantCode, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Failed to update tenant subscription",
                            null
                    )
            );
        }
    }
}
