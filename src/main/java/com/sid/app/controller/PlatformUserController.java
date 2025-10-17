package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.dto.request.PlatformUserLoginRequest;
import com.sid.app.dto.request.PlatformUserSignupRequest;
import com.sid.app.dto.response.PlatformUserAuthResponse;
import com.sid.app.dto.response.PlatformUserResponse;
import com.sid.app.service.PlatformUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    @PostMapping(EndpointConstants.PLATFORM_AUTH_SIGNUP_ENDPOINT)
    public ResponseEntity<PlatformUserAuthResponse> signup(@Valid @RequestBody PlatformUserSignupRequest request) {
        log.info("Platform user signup request received for email: {}", request.getEmail());

        try {
            PlatformUserAuthResponse response = platformUserService.signup(request);

            if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error in platform user signup: {}", e.getMessage(), e);
            PlatformUserAuthResponse errorResponse = PlatformUserAuthResponse.builder()
                    .status(AppConstants.STATUS_FAILED)
                    .message(AppConstants.ERROR_PLATFORM_SIGNUP_FAILED)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(EndpointConstants.PLATFORM_AUTH_LOGIN_ENDPOINT)
    public ResponseEntity<PlatformUserAuthResponse> login(@Valid @RequestBody PlatformUserLoginRequest request) {
        log.info("Platform user login request received for: {}", request.getEmailOrMobile());

        try {
            PlatformUserAuthResponse response = platformUserService.login(request);

            if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("Error in platform user login: {}", e.getMessage(), e);
            PlatformUserAuthResponse errorResponse = PlatformUserAuthResponse.builder()
                    .status(AppConstants.STATUS_FAILED)
                    .message(AppConstants.ERROR_PLATFORM_LOGIN_FAILED)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(EndpointConstants.PLATFORM_AUTH_REFRESH_ENDPOINT)
    public ResponseEntity<PlatformUserAuthResponse> refreshToken(@RequestBody String refreshToken) {
        log.info("Platform user token refresh request received");

        try {
            PlatformUserAuthResponse response = platformUserService.refreshToken(refreshToken);

            if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("Error in platform user token refresh: {}", e.getMessage(), e);
            PlatformUserAuthResponse errorResponse = PlatformUserAuthResponse.builder()
                    .status(AppConstants.STATUS_FAILED)
                    .message(AppConstants.ERROR_PLATFORM_TOKEN_REFRESH_FAILED)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(EndpointConstants.PLATFORM_AUTH_PROFILE_ENDPOINT)
    public ResponseEntity<PlatformUserResponse> getProfile(@RequestParam Long platformUserId) {
        log.info("Platform user profile request for ID: {}", platformUserId);

        try {
            PlatformUserResponse response = platformUserService.getPlatformUserProfile(platformUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching platform user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
