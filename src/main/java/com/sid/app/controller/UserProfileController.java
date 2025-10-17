package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserProfileDTO;
import com.sid.app.service.UserProfileService;
import com.sid.app.exception.UserProfileValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Get user profile
     * GET /user-profile
     */
    @GetMapping(value = EndpointConstants.USER_PROFILE_ENDPOINT, produces = "application/json")
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<UserProfileDTO>> getProfile() {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("GET /user/profile called - userId={}", userId);
        try {
            UserProfileDTO dto = userProfileService.getProfile(userId);
            log.info("User profile retrieved successfully for userId={}", userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "User profile retrieved", dto));
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            log.warn("getProfile: user not found userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("getProfile error for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve user profile", null));
        }
    }

    /**
     * Create or update user profile
     * PUT /user-profile
     */
    @PutMapping(value = EndpointConstants.USER_PROFILE_ENDPOINT, produces = "application/json")
    @RequiredRole({UserRole.USER, UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
    public ResponseEntity<ResponseDTO<?>> upsertProfile(@Valid @RequestBody UserProfileDTO dto) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();

        log.info("PUT /user/profile called - userIdParam={}, userIdBody={}", userId, dto.getUserId());

        if (!userId.equals(dto.getUserId())) {
            log.info("Request param userId and body userId mismatch (param={}, body={})", userId, dto.getUserId());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "userId in request param and body must match.", null));
        }

        try {
            UserProfileDTO saved = userProfileService.upsertProfile(dto);
            log.info("User profile saved successfully for userId={}, userProfileId={}", userId, saved.getUserProfileId());
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "User profile saved", saved));
        } catch (UserProfileValidationException ve) {
            // structured validation errors (field -> message)
            Map<String, String> fieldErrors = ve.getFieldErrors();
            log.warn("Validation failed for userId={} errors={}", userId, fieldErrors);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Validation failed", fieldErrors));
        } catch (IllegalArgumentException iae) {
            log.warn("Bad request when saving profile for userId={}: {}", userId, iae.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, iae.getMessage(), null));
        } catch (jakarta.persistence.EntityNotFoundException enfe) {
            log.warn("Entity not found when saving profile for userId={}: {}", userId, enfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(AppConstants.STATUS_FAILED, enfe.getMessage(), null));
        } catch (Exception ex) {
            log.error("upsertProfile error for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to save user profile", null));
        }
    }
}
