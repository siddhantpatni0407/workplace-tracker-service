package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserSettingsDTO;
import com.sid.app.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    /**
     * Get user settings
     */
    @GetMapping(AppConstants.USER_SETTINGS_ENDPOINT)
    public ResponseEntity<ResponseDTO<UserSettingsDTO>> getUserSettings(@RequestParam("userId") Long userId) {
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
    @PutMapping(AppConstants.USER_SETTINGS_ENDPOINT)
    public ResponseEntity<ResponseDTO<UserSettingsDTO>> upsertUserSettings(
            @RequestParam("userId") Long userId,
            @Valid @RequestBody UserSettingsDTO dto) {

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
    @DeleteMapping(AppConstants.USER_SETTINGS_ENDPOINT)
    public ResponseEntity<ResponseDTO<Void>> deleteUserSettings(@RequestParam("userId") Long userId) {
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
