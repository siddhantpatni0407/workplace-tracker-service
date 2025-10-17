package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserDTO;
import com.sid.app.model.UserStatusUpdateRequest;
import com.sid.app.service.UserService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller for handling user-related operations.
 * Provides endpoints for fetching, updating, and deleting users.
 * Updated to support multi-tenant architecture.
 *
 * <p>Author: Siddhant Patni</p>
 */
@RestController
@Slf4j
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Fetches all users from the system.
     * Updated to return tenant-specific users for ADMIN role, all users for SUPER_ADMIN and PLATFORM_USER.
     *
     * @return ResponseEntity with a ResponseDTO containing a list of UserDTOs.
     */
    @GetMapping(AppConstants.FETCH_ALL_USERS_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<List<UserDTO>>> getAllUsers() {
        String currentUserRole = jwtAuthenticationContext.getCurrentUserRole();
        log.info("getAllUsers() : Received request to fetch users. Current user role: {}", currentUserRole);

        List<UserDTO> users;

        // Role-based filtering logic
        if ("ADMIN".equals(currentUserRole)) {
            // For ADMIN users, get current user's tenant_user_id and fetch only users from same tenant
            Long currentUserId = jwtAuthenticationContext.getCurrentUserId();
            log.info("getAllUsers() : Current user ID from JWT: {}", currentUserId);

            Long currentUserTenantUserId = jwtAuthenticationContext.getCurrentUserTenantUserId();
            log.info("getAllUsers() : Current user tenant_user_id: {}", currentUserTenantUserId);

            if (currentUserTenantUserId == null) {
                log.warn("getAllUsers() : Current ADMIN user's tenant_user_id is null. Cannot fetch tenant-specific users.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(
                                AppConstants.STATUS_FAILED,
                                "Unable to determine user's tenant. Access denied.",
                                Collections.emptyList()
                        ));
            }

            log.info("getAllUsers() : Fetching users for ADMIN user's tenant_user_id: {}", currentUserTenantUserId);
            users = userService.getUsersByCurrentUserTenant(currentUserTenantUserId);

            if (users.isEmpty()) {
                log.info("getAllUsers() : No users found in the ADMIN user's tenant.");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No users found in your tenant.",
                        Collections.emptyList()
                ));
            }

            log.info("getAllUsers() : Successfully retrieved {} users for ADMIN user's tenant.", users.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Users retrieved successfully for your tenant.",
                    users
            ));
        } else {
            // For SUPER_ADMIN and PLATFORM_USER, fetch all users
            log.info("getAllUsers() : Fetching all users for {} role.", currentUserRole);
            users = userService.getAllUsers();

            if (users.isEmpty()) {
                log.info("getAllUsers() : No users found in the system.");
                return ResponseEntity.ok(new ResponseDTO<>(
                        AppConstants.STATUS_SUCCESS,
                        "No users found.",
                        Collections.emptyList()
                ));
            }

            log.info("getAllUsers() : Successfully retrieved {} users.", users.size());
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "Users retrieved successfully.",
                    users
            ));
        }
    }

    /**
     * Fetches users by tenant ID.
     */
    @GetMapping(AppConstants.USERS_BY_TENANT_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<UserDTO>>> getUsersByTenant(@RequestParam Long tenantId) {
        log.info("getUsersByTenant() : Received request to fetch users for tenant ID: {}", tenantId);

        List<UserDTO> users = userService.getUsersByTenantId(tenantId);

        if (users.isEmpty()) {
            log.info("getUsersByTenant() : No users found for tenant ID: {}", tenantId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "No users found for this tenant.",
                    Collections.emptyList()
            ));
        }

        log.info("getUsersByTenant() : Successfully retrieved {} users for tenant ID: {}", users.size(), tenantId);
        return ResponseEntity.ok(new ResponseDTO<>(
                AppConstants.STATUS_SUCCESS,
                "Users retrieved successfully for tenant.",
                users
        ));
    }

    /**
     * Fetches active users by tenant ID.
     */
    @GetMapping(AppConstants.ACTIVE_USERS_BY_TENANT_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<UserDTO>>> getActiveUsersByTenant(@RequestParam Long tenantId) {
        log.info("getActiveUsersByTenant() : Received request to fetch active users for tenant ID: {}", tenantId);

        List<UserDTO> users = userService.getActiveUsersByTenantId(tenantId);

        log.info("getActiveUsersByTenant() : Successfully retrieved {} active users for tenant ID: {}", users.size(), tenantId);
        return ResponseEntity.ok(new ResponseDTO<>(
                AppConstants.STATUS_SUCCESS,
                "Active users retrieved successfully for tenant.",
                users
        ));
    }

    /**
     * Search users within a tenant.
     */
    @GetMapping(AppConstants.SEARCH_USERS_BY_TENANT_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER", "MANAGER"})
    public ResponseEntity<ResponseDTO<List<UserDTO>>> searchUsersByTenant(
            @RequestParam Long tenantId,
            @RequestParam String searchTerm) {
        log.info("searchUsersByTenant() : Searching users in tenant {} with term: {}", tenantId, searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(
                    AppConstants.STATUS_FAILED,
                    "Search term cannot be empty.",
                    Collections.emptyList()
            ));
        }

        List<UserDTO> users = userService.searchUsersByTenant(tenantId, searchTerm.trim());

        log.info("searchUsersByTenant() : Found {} users matching search term in tenant {}", users.size(), tenantId);
        return ResponseEntity.ok(new ResponseDTO<>(
                AppConstants.STATUS_SUCCESS,
                "Search completed successfully.",
                users
        ));
    }

    /**
     * Fetches a user by their ID using JWT context or request parameter.
     *
     * @return ResponseEntity with a ResponseDTO containing the UserDTO.
     */
    @GetMapping(AppConstants.USER_ENDPOINT)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN", "PLATFORM_USER", "MANAGER"})
    public ResponseEntity<ResponseDTO<UserDTO>> getUserById(@RequestParam(required = false) Long userId) {
        // If userId not provided in request param, get from JWT context
        if (userId == null) {
            userId = jwtAuthenticationContext.getCurrentUserId();
        }

        log.info("getUserById() : Received request to fetch user with ID: {}", userId);

        try {
            UserDTO user = userService.getUserById(userId);
            log.info("getUserById() : User with ID {} retrieved successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "User retrieved successfully.",
                    user
            ));
        } catch (EntityNotFoundException e) {
            log.warn("getUserById() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("getUserById() : Error retrieving user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while retrieving the user.",
                            null
                    ));
        }
    }

    /**
     * Updates an existing user's details.
     *
     * @param updatedUserDTO The updated user details.
     * @return ResponseEntity with a ResponseDTO indicating the update status.
     */
    @PutMapping(AppConstants.USER_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(
            @RequestParam(required = false) Long userId,
            @RequestBody @Valid UserDTO updatedUserDTO) {

        // If userId not provided in request param, get from JWT context
        if (userId == null) {
            userId = jwtAuthenticationContext.getCurrentUserId();
        }

        log.info("updateUser() : Received request to update user with ID: {}", userId);
        log.debug("updateUser() : Request payload: {}", ApplicationUtils.getJSONString(updatedUserDTO));

        try {
            UserDTO updatedUser = userService.updateUser(userId, updatedUserDTO);
            log.info("updateUser() : User with ID {} updated successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "User updated successfully.",
                    updatedUser
            ));
        } catch (EntityNotFoundException e) {
            log.warn("updateUser() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (IllegalArgumentException e) {
            log.warn("updateUser() : Validation failed for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("updateUser() : Error updating user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while updating the user.",
                            null
                    ));
        }
    }

    /**
     * Update user active / locked status in a single API call.
     */
    @PatchMapping(value = AppConstants.USER_STATUS_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<UserDTO>> updateUserStatus(@RequestBody UserStatusUpdateRequest req) {
        log.info("updateUserStatus() : Received request -> {}", ApplicationUtils.getJSONString(req));

        if (req == null || req.getUserId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "userId is required.",
                            null
                    ));
        }

        if (req.getIsActive() == null && req.getIsAccountLocked() == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Nothing to update. Provide isActive and/or isAccountLocked.",
                            null
                    ));
        }

        try {
            UserDTO updated = userService.updateUserStatus(req);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "User status updated successfully.",
                    updated
            ));
        } catch (EntityNotFoundException ex) {
            log.warn("updateUserStatus() : User not found id={}", req.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            ex.getMessage(),
                            null
                    ));
        } catch (Exception ex) {
            log.error("updateUserStatus() error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "Failed to update user status.",
                            null
                    ));
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @return ResponseEntity with a ResponseDTO indicating the deletion status.
     */
    @DeleteMapping(AppConstants.USER_ENDPOINT)
    @RequiredRole({"ADMIN", "SUPER_ADMIN", "PLATFORM_USER"})
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@RequestParam(required = false) Long userId) {
        // If userId not provided in request param, get from JWT context
        if (userId == null) {
            userId = jwtAuthenticationContext.getCurrentUserId();
        }

        log.info("deleteUser() : Received request to delete user with ID: {}", userId);

        try {
            userService.deleteUser(userId);
            log.info("deleteUser() : User with ID {} deleted successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>(
                    AppConstants.STATUS_SUCCESS,
                    "User deleted successfully.",
                    null
            ));
        } catch (EntityNotFoundException e) {
            log.warn("deleteUser() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            log.error("deleteUser() : Error deleting user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(
                            AppConstants.STATUS_FAILED,
                            "An error occurred while deleting the user.",
                            null
                    ));
        }
    }

}