package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
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

    @PostMapping(AppConstants.USER_REGISTER_ENDPOINT)
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Register request -> {}", ApplicationUtils.getJSONString(request));
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
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


}
