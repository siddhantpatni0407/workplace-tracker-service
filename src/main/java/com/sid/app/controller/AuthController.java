package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.PasswordChangeRequest;
import com.sid.app.exception.UserNotFoundException;
import com.sid.app.service.AuthService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    /**
     * Enhanced register endpoint with role-based code validation:
     * - SUPER_ADMIN: requires platformUserCode + tenantCode
     * - ADMIN: requires tenantCode
     * - USER/MANAGER: requires tenantUserCode
     */
    @PostMapping(AppConstants.USER_REGISTER_ENDPOINT)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request -> {}", ApplicationUtils.getJSONString(request));

        // Validate basic request fields
        AuthResponse validationResponse = validateRegisterRequest(request);
        if (!AppConstants.STATUS_SUCCESS.equals(validationResponse.getStatus())) {
            return ResponseEntity.badRequest().body(validationResponse);
        }

        // Process registration through service
        AuthResponse response = authService.register(request);

        if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validate register request based on role requirements
     */
    private AuthResponse validateRegisterRequest(RegisterRequest request) {
        // Basic field validation
        if (isBlank(request.getName())) {
            return createErrorResponse("Name is required");
        }
        if (isBlank(request.getEmail())) {
            return createErrorResponse("Email is required");
        }
        if (isBlank(request.getPassword())) {
            return createErrorResponse("Password is required");
        }
        if (isBlank(request.getRole())) {
            return createErrorResponse("Role is required");
        }

        // Email format validation (basic)
        if (!request.getEmail().contains("@")) {
            return createErrorResponse("Invalid email format");
        }

        // Password length validation
        if (request.getPassword().length() < 8) {
            return createErrorResponse("Password must be at least 8 characters long");
        }

        // Role-specific code validation
        String role = request.getRole().toUpperCase();
        switch (role) {
            case "SUPER_ADMIN":
                if (isBlank(request.getPlatformUserCode())) {
                    return createErrorResponse("Platform user code is required for SUPER_ADMIN role");
                }
                if (isBlank(request.getTenantCode())) {
                    return createErrorResponse("Tenant code is required for SUPER_ADMIN role");
                }
                break;

            case "ADMIN":
                if (isBlank(request.getTenantUserCode())) {
                    return createErrorResponse("Tenant user code is required for ADMIN role");
                }
                // Note: No tenantCode validation needed since we derive tenant info from SUPER_ADMIN
                break;

            case "USER":
            case "MANAGER":
                if (isBlank(request.getTenantUserCode())) {
                    return createErrorResponse("Tenant user code is required for " + role + " role");
                }
                break;

            default:
                return createErrorResponse("Invalid role: " + request.getRole() + ". Supported roles: SUPER_ADMIN, ADMIN, USER, MANAGER");
        }

        return new AuthResponse(null, null, null, null, AppConstants.STATUS_SUCCESS, null, null, null, null, null);
    }

    @PostMapping(AppConstants.USER_LOGIN_ENDPOINT)
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        log.info("Login request -> {}", ApplicationUtils.getJSONString(request));
        AuthResponse response = authService.login(request);

        if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
            // create initial refresh token cookie for this user and set on response
            try {
                // use email from response if available, otherwise from request
                String email = response.getName() != null ? response.getName() : request.getEmail();
                authService.createRefreshCookieForUser(email, servletResponse);
            } catch (Exception e) {
                log.warn("Failed to create refresh cookie on login: {}", e.getMessage());
                // proceed to return access token anyway; cookie rotation/refresh will still work later
            }
            return ResponseEntity.ok(response);
        } else {
            HttpStatus status = (Boolean.TRUE.equals(response.getAccountLocked()))
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(response);
        }
    }


    @PostMapping(AppConstants.FORGOT_PASSWORD_RESET_ENDPOINT)
    public ResponseEntity<ResponseDTO<Void>> resetPassword(@Valid @RequestBody ForgotPasswordResetRequest request) {
        log.info("Reset password for email: {}", request.getEmail());
        return authService.resetPassword(request);
    }

    @PostMapping(AppConstants.AUTH_REFRESH_ENDPOINT)
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletResponse servletResponse) {

        // pick token from cookie first, then header
        String token = null;
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            token = refreshTokenCookie;
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        if (token == null) {
            AuthResponse resp = new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Missing refresh token.", null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        try {
            AuthResponse authResp = authService.refreshToken(token, servletResponse);

            if (AppConstants.STATUS_SUCCESS.equals(authResp.getStatus())) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authResp);
            } else {
                // refresh failed - return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResp);
            }
        } catch (Exception ex) {
            log.warn("Refresh token failed: {}", ex.getMessage());
            AuthResponse resp = new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Refresh failed.", null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    /**
     * Change password for a user (partial update).
     * Accepts userId in body (optional) or derive from JWT in production.
     */
    @PatchMapping(value = AppConstants.USER_CHANGE_PASSWORD_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequiredRole({"USER", "ADMIN", "SUPER_ADMIN"})
    public ResponseEntity<ResponseDTO<Void>> changePassword(@RequestBody @Valid PasswordChangeRequest request,
                                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();

        log.info("changePassword() : Received request for userId={}",
                request != null ? userId : null);

        try {
            assert request != null;
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED,
                                "userId is required. In production derive userId from auth token.", null));
            }

            authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            log.info("changePassword() : Password changed successfully for userId={}", userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Password changed successfully.", null));

        } catch (UserNotFoundException ex) {
            log.warn("changePassword() : User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));

        } catch (IllegalArgumentException ex) {
            log.info("changePassword() : Validation failed for userId={}: {}",
                    userId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));

        } catch (Exception ex) {
            log.error("changePassword() error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to change password.", null));
        }
    }

    // Helper methods
    private AuthResponse createErrorResponse(String message) {
        return new AuthResponse(null, null, null, null, AppConstants.STATUS_FAILED, message, null, null, null, null);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
