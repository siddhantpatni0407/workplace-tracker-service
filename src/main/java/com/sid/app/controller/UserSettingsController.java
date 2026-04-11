package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserSettingsDTO;
import com.sid.app.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sid.app.annotation.CorrelationId;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Settings", description = "Manage personal user settings and preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Get user settings
     */
    @Operation(summary = "Get user settings", description = "Retrieve settings for the authenticated user.")
    @GetMapping(EndpointConstants.USER_SETTINGS_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    @CorrelationId
    public ResponseEntity<ResponseDTO<UserSettingsDTO>> getUserSettings() {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getUserSettings() -> userId={}", userId);
        try {
            UserSettingsDTO dto = userSettingsService.getSettings(userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_MESSAGE_USER_SETTINGS_RETRIEVED, dto));
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("getUserSettings error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_RETRIEVE_USER_SETTINGS, null));
        }
    }

    /**
     * Create or update user settings
     */
    @Operation(summary = "Create or update user settings", description = "Create or update settings for the authenticated user.")
    @PutMapping(EndpointConstants.USER_SETTINGS_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    @CorrelationId
    public ResponseEntity<ResponseDTO<UserSettingsDTO>> upsertUserSettings(
            @Valid @RequestBody UserSettingsDTO dto) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();

        if (!userId.equals(dto.getUserId())) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "userId in request param and body must match.", null));
        }

        try {
            UserSettingsDTO saved = userSettingsService.upsertSettings(dto);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_MESSAGE_USER_SETTINGS_SAVED, saved));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, iae.getMessage(), null));
        } catch (jakarta.persistence.EntityNotFoundException enfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(AppConstants.STATUS_FAILED, enfe.getMessage(), null));
        } catch (Exception ex) {
            log.error("upsertUserSettings error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_SAVE_USER_SETTINGS, null));
        }
    }

    /**
     * Delete user settings
     */
    @Operation(summary = "Delete user settings", description = "Delete settings for the authenticated user.")
    @DeleteMapping(EndpointConstants.USER_SETTINGS_ENDPOINT)
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    @CorrelationId
    public ResponseEntity<ResponseDTO<Void>> deleteUserSettings() {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        try {
            userSettingsService.deleteSettings(userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_MESSAGE_USER_SETTINGS_DELETED, null));
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("deleteUserSettings error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_DELETE_USER_SETTINGS, null));
        }
    }
}
